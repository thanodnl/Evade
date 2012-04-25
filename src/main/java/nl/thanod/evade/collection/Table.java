/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.util.Documenter;

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

	/**
	 * uses dubble checked locking to get the {@link Memtable} belonging to this
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

}
