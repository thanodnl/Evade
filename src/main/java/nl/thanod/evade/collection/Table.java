/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.store.bloom.Bloom;
import nl.thanod.evade.store.bloom.BloomHasher;
import nl.thanod.evade.util.Documenter;
import nl.thanod.evade.util.iterator.Sorterator;
import nl.thanod.evade.util.iterator.Unduplicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class Table extends DocumentCollection
{

	protected static final Logger log = LoggerFactory.getLogger(Table.class);

	/**
	 * Set for all available {@link SSTable} objects for this table
	 */
	protected final Set<SSTable> sstables = Collections.synchronizedSet(new HashSet<SSTable>());

	/**
	 * Contains all the {@link Memtable} objects currently written to file.
	 * While writing the {@link Table} should still be able to provide the
	 * information stored in the Memtable
	 */
	protected final Set<Memtable> writing = Collections.synchronizedSet(new HashSet<Memtable>());

	/**
	 * Short term store for {@link Document.Entry} before persisting to file
	 */
	protected volatile Memtable memtable;

	public final File directory;
	public final String name;

	private Table(File directory, String name)
	{
		this.directory = directory;
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	private void addSSTable(SSTable ss)
	{
		this.sstables.add(ss);
	}

	/**
	 * uses double checked locking to get the {@link Memtable} belonging to this
	 * {@link Table}
	 * @return an instance of the memtable to write to
	 */
	protected Memtable getMemtable()
	{
		Memtable result = this.memtable;
		if (result == null) {
			synchronized (this) {
				result = this.memtable;
				if (result == null) {
					this.memtable = result = new Memtable();
				}
			}
		}
		return result;
	}

	public void majorCompact(Executor exec)
	{
		TableCompacter compacter = new TableCompacter(this);

		// add and remove sstables after compacting
		compacter.addListener(new TableCompacter.CompacterListener() {
			@Override
			public void compacted(java.util.Collection<? extends SSTable> removed, java.util.Collection<? extends SSTable> created)
			{
				// add the newly created tables
				Table.this.sstables.addAll(created);

				// remove old tables
				for (SSTable ss : removed) {
					if (Table.this.sstables.remove(ss)) {
						Table.log.info("Closed SSTable {} after compacting", ss.file);
						ss.remove();
					} else {
						Table.log.error("Could not remove {} from the tables", ss.file);
					}
				}
			}
		});

		// run the compaction
		if (exec != null)
			exec.execute(compacter);
		else
			compacter.run();
	}

	public void update(UUID id, Document doc)
	{
		// store the new document information in the memtable
		boolean stored = false;
		do {
			try {
				getMemtable().update(id, doc);
				stored = true;
			} catch (IllegalStateException ball) {
				// Can happen when you try to write to an already locked Memtable. It should be replaces when you retry
				log.error("Memtable was already locked while tried to write to it in sstable: {}", this.name);
			}
		} while (!stored);

		if (getMemtable().size() >= 10000) { // persist the table
			persist();
		}
	}

	/**
	 * @param old
	 */
	private void compact(Memtable old)
	{
		try {
			// save the tables
			for (File sstable : SSTable.save(this.directory, this.name, old)) {
				// and add all created tables for resolving
				this.addSSTable(new SSTable(sstable));
				log.info("Saved memtable to sstable: {}", sstable);
			}
		} catch (FileNotFoundException ball) {
			ball.printStackTrace();
		} catch (IOException ball) {
			ball.printStackTrace();
		}
	}

	public void persist()
	{
		Memtable old = getMemtable();
		// put a new memtable in place for further writes
		this.memtable = new Memtable();

		// lock for further writes;
		old.lock();

		// add it to the set of memtables being persisted
		this.writing.add(old);

		// now the memtable is safe to persist to file
		//TODO compaction should run on an other thread
		compact(old);

		// remove it from the set of memtables being persisted
		this.writing.remove(old);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		return new Documenter(getMemtable(), this.sstables, this.writing).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id)
	{
		Bloom<UUID> bloom = new Bloom<UUID>(id, BloomHasher.UUID);
		Collection<SSTable> tables = new ArrayList<SSTable>(this.sstables);
		for (SSTable ss : tables)
			if (!ss.earlySkip(bloom) && ss.contains(id))
				return true;
		return getMemtable().contains(id);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id)
	{
		Document doc = null;
		for (SSTable ss : this.sstables)
			doc = Document.merge(doc, ss.get(id));
		doc = Document.merge(doc, getMemtable().get(id));
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#size()
	 */
	@Override
	public int size()
	{
		int count = 0;
		for (SSTable ss : this.sstables)
			count += ss.size();
		count += getMemtable().size();
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public Iterable<UUID> uuids()
	{
		List<Iterable<UUID>> ids = new ArrayList<Iterable<UUID>>(Table.this.sstables.size() + this.writing.size() + 1);
		// TODO prevent raceconditions with ConcurrentModificationExceptions on the iterators. These could happen when iterating over them when a compacter is finished compacting
		synchronized (this.sstables) {
			for (SSTable table : this.sstables)
				ids.add(table.uuids());
		}
		synchronized (this.writing) {
			for (Memtable table : this.writing)
				ids.add(table.uuids());
		}
		ids.add(getMemtable().uuids());

		// some generic magic happens which causes a warning
		@SuppressWarnings("unchecked")
		Sorterator<UUID> sorted = new Sorterator<UUID>(ids);

		return new Unduplicator<UUID>(sorted);
	}

	public void accept(SSTable.Visitor visitor)
	{
		for (SSTable sstable : this.sstables)
			sstable.accept(visitor);
	}

	public static Table load(final File dir, final String name)
	{
		Table t = new Table(dir, name);
		File[] files = dir.listFiles(new FileFilter() {
			final Pattern p = Pattern.compile(name + "\\d+\\.sstable");

			@Override
			public boolean accept(File f)
			{
				if (!f.isFile())
					return false;
				return p.matcher(f.getName()).matches();
			}
		});

		if (files != null) {
			for (File f : files) {
				try {
					t.addSSTable(new SSTable(f));
				} catch (IOException ball) {
					ball.printStackTrace();
				}
			}
		}
		return t;
	}
}
