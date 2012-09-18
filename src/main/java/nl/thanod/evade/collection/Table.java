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
import nl.thanod.evade.util.iterator.Generator;
import nl.thanod.evade.util.iterator.Sorterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class Table extends Collection
{

	protected static final Logger log = LoggerFactory.getLogger(Table.class);

	// SSTables should be unique on the file they are based on
	protected Set<SSTable> sstables;

	protected volatile Memtable memtable;

	public final File directory;
	public final String name;

	private Table(File directory, String name)
	{
		this.directory = directory;
		this.name = name;

		this.sstables = new HashSet<SSTable>();
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
			public void compacted(Iterable<SSTable> removed, Iterable<SSTable> created)
			{
				// add the newly created tables
				for (SSTable ss : created)
					Table.this.sstables.add(ss);

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
		getMemtable().update(id, doc);
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
		compact(old);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		return new Documenter(getMemtable(), sstables).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id)
	{
		Bloom<UUID> bloom = new Bloom<UUID>(id, BloomHasher.UUID);
		for (SSTable ss : this.sstables)
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
		Bloom<UUID> bloom = new Bloom<UUID>(id, BloomHasher.UUID);
		Document doc = null;
		for (SSTable ss : this.sstables) {
			//			if (ss.earlySkip(bloom))
			//				continue;
			doc = Document.merge(doc, ss.get(id));
		}
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
		List<Iterable<UUID>> ids = new ArrayList<Iterable<UUID>>(Table.this.sstables.size());
		for (SSTable sstable : this.sstables)
			ids.add(sstable.uuids());
		//TODO: add memtable to the iterables
		// this memtable-iterable should be automaticly updating
		// when a write to it occures to give accurate counts

		final Sorterator<UUID> sorted = new Sorterator<UUID>(ids, new Comparator<UUID>() {
			@Override
			public int compare(UUID o1, UUID o2)
			{
				return o1.compareTo(o2);
			}
		});
		return new Generator<UUID>() {
			UUID last = null;

			@Override
			protected UUID generate() throws NoSuchElementException
			{
				while (sorted.hasNext()) {
					UUID current = sorted.next();
					if (current.equals(last))
						continue;
					return this.last = current;
				}
				throw new NoSuchElementException();
			}
		};
	}

	public void accept(SSTable.Visitor visitor)
	{
		for (SSTable sstable:this.sstables)
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
