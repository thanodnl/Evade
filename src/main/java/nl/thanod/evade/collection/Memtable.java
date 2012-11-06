/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;

/**
 * @author nilsdijk
 */
public class Memtable extends DocumentCollection
{

	private final Map<UUID, Document> docs = new HashMap<UUID, Document>();
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	/**
	 * Indicating if this {@link Memtable} has already been locked for writing.
	 */
	private volatile boolean locked = false;

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

	/**
	 * @param id
	 *            the {@link UUID} to store the {@link Document} under
	 * @param doc
	 *            the {@link Document} to store
	 * @return A reference to this for daisy chainging
	 * @throws IllegalStateException
	 *             when the {@link Memtable} is locked before writing
	 */
	public Memtable update(UUID id, Document doc)
	{
		writeLock();
		try {
			Document prev = this.docs.put(id, doc);
			if (prev != null) {
				// if there was already an item in the store merge them and store the result
				this.docs.put(id, Document.merge(prev, doc));
			}
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
		final Queue<UUID> sortedKeys = uuids();

		// iterator to iterate the entries present while initiating the iteration
		return new Iterator<Document.Entry>() {

			@Override
			public boolean hasNext()
			{
				return !sortedKeys.isEmpty();
			}

			@Override
			public Entry next()
			{
				UUID id = sortedKeys.poll();
				return new Document.Entry(id, Memtable.this.get(id));
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
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

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public PriorityQueue<UUID> uuids()
	{
		this.rwl.readLock().lock();
		try {
			return new PriorityQueue<UUID>(this.docs.keySet());
		} finally {
			this.rwl.readLock().unlock();
		}
	}

	/**
	 * Lock this table for further writes. Writes will throw an
	 * {@link IllegalStateException} when occuring after {@link Memtable#lock()}
	 * is called
	 * @throws IllegalStateException
	 *             when the {@link Memtable} is already locked
	 */
	public void lock()
	{
		writeLock();
		try {
			this.locked = true;
		} finally {
			this.rwl.writeLock().unlock();
		}
	}

	/**
	 * Acquire the writelock and check if the {@link Memtable} is not locked
	 * during the acquiring
	 * @throws IllegalStateException
	 *             when the {@link Memtable} is already locked
	 */
	private void writeLock()
	{
		if (locked)
			throw new IllegalStateException("Memtable already closed");
		this.rwl.writeLock();
		// if the memtable is locked after acquiring the lock
		if (locked) {
			try {
				throw new IllegalStateException("Memtable already closed");
			} finally {
				this.rwl.writeLock().unlock();
			}
		}
	}
}
