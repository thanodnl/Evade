/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import nl.thanod.evade.collection.index.Memdex;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.util.Documenter;
import nl.thanod.evade.util.Generator;

/**
 * @author nilsdijk
 */
public class Table extends Collection
{

	protected List<SSTable> sstables;
	protected volatile Memtable memtable;

	public Table()
	{
		sstables = new LinkedList<SSTable>();
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

	public void update(UUID id, Document doc)
	{
		getMemtable().update(id, doc);
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
		for (SSTable ss : this.sstables)
			if (ss.contains(id))
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

	private static class LinkedEntry<T>
	{
		public final LinkedEntry<T> next;
		public final T t;

		public LinkedEntry(LinkedEntry<T> next, T t)
		{
			this.next = next;
			this.t = t;
		}
	}

	public void ensureStringIndex(List<String> path)
	{
		long time = System.nanoTime();
		Map<String, LinkedEntry<UUID>> index = new TreeMap<String, LinkedEntry<UUID>>();
		for (Document.Entry e : this) {
			Document d = e.doc.path(path);
			if (d == null)
				continue;
			if (d instanceof StringDocument) {
				String value = ((StringDocument) d).value;
				value = value.toLowerCase();
				index.put(value, new LinkedEntry<UUID>(index.get(value), e.id));
			}
		}
		time = System.nanoTime() - time;
		
		System.out.println("sorting the index took: " + time + "ns (" + (time/1000000) + "ms)");
		System.out.println(index.size());
	}

	public static Table load(final File dir, final String name)
	{
		Table t = new Table();
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

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public Iterable<UUID> uuids()
	{
		// not yet implemented
		throw new UnsupportedOperationException();
	}
}
