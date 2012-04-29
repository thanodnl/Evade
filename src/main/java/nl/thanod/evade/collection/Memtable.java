/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public class Memtable extends Collection
{

	private final Map<UUID, Document> docs = new TreeMap<UUID, Document>();
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	public Memtable()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id)
	{
		this.rwl.readLock().lock();
		try {
			return this.docs.containsKey(id);
		} finally {
			this.rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id)
	{
		this.rwl.readLock().lock();
		try {
			return this.docs.get(id);
		} finally {
			this.rwl.readLock().unlock();
		}
	}

	public Memtable update(UUID id, Document doc)
	{
		this.rwl.writeLock().lock();
		try {
			if (this.docs.containsKey(id))
				doc = Document.merge(this.docs.get(id), doc);
			this.docs.put(id, doc);
		} finally {
			this.rwl.writeLock().unlock();
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Document.Entry> iterator()
	{
		this.rwl.readLock().lock();
		try {
			List<Document.Entry> list = new ArrayList<Document.Entry>(this.docs.size());
			for (Map.Entry<UUID, Document> e : this.docs.entrySet())
				list.add(new Document.Entry(e.getKey(), e.getValue()));
			return list.iterator();
		} finally {
			this.rwl.readLock().unlock();
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Document.Entry e : this) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(e);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#count()
	 */
	@Override
	public int size()
	{
		return this.docs.size();
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public Iterable<UUID> uuids()
	{
		return Collections.unmodifiableSet(this.docs.keySet());
	}
}
