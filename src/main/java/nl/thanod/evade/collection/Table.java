/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import nl.thanod.evade.collection.index.IndexSerializer;
import nl.thanod.evade.collection.index.MemIndex;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.store.bloom.Bloom;
import nl.thanod.evade.store.bloom.BloomHasher;
import nl.thanod.evade.util.Documenter;

/**
 * @author nilsdijk
 */
public class Table extends Collection
{

	protected List<SSTable> sstables;
	protected volatile Memtable memtable;
	protected volatile MemIndex memindex;

	private final File directory;
	private final String name;

	private Table(File directory, String name)
	{
		this.directory = directory;
		this.name = name;

		this.sstables = new LinkedList<SSTable>();
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

	public void maintainIndex(DocumentPath path, Modifier modifier)
	{
		this.memindex = new MemIndex(path, modifier, 50000);
	}

	public void update(UUID id, Document doc)
	{
		getMemtable().update(id, doc);
		if (this.memindex != null)
			this.memindex.update(id, doc);
		if (getMemtable().size() >= 50000) { // persist the table
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
				System.out.println(sstable);
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

		if (this.memindex != null) {
			MemIndex index = this.memindex;
			this.memindex = new MemIndex(index.path, index.modifier, 50000);
			try {
				System.out.println("Saving index");
				IndexSerializer.compactIndices(directory, name, Collections.singletonList(index));
				System.out.println("Saved");
			} catch (IOException ball) {
				ball.printStackTrace();
			}
		}
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
			if (ss.earlySkip(bloom))
				continue;
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
		// not yet implemented
		throw new UnsupportedOperationException();
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

		for (File f : files) {
			try {
				t.addSSTable(new SSTable(f));
			} catch (IOException ball) {
				ball.printStackTrace();
			}
		}
		return t;
	}
}
