/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.Comparator;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public class MemIndex extends Index
{

	public static class MemEntry extends Index.Entry
	{

		protected MemEntry next;
		protected MemEntry prev;

		public MemEntry(UUID id, ValueDocument match)
		{
			super(id, match);
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#next()
		 */
		@Override
		public Entry next()
		{
			return this.next;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#previous()
		 */
		@Override
		public Entry previous()
		{
			return this.prev;
		}
	}

	public final DocumentPath path;
	public final Modifier modifier;

	private MemEntry[] data;
	private int count;

	public MemIndex(DocumentPath path, Modifier modifier, int size)
	{
		this.path = path;
		this.modifier = modifier;

		this.data = new MemEntry[size];
		this.count = 0;
	}

	public void update(UUID id, Document doc)
	{
		doc = doc.get(this.path);

		// the path this index is for is not in the update
		if (doc == null)
			return;

		ensureSpace();

		doc = Modifier.safeModify(this.modifier, doc);

		final ValueDocument search;
		if (doc instanceof ValueDocument)
			search = (ValueDocument) doc;
		else
			search = new NullDocument(doc.version);

		int putIn = Search.before(this.data, 0, this.count, new Comparable<MemIndex.MemEntry>() {
			@Override
			public int compareTo(MemEntry o)
			{
				return ValueDocument.VALUE_COMPARE.compare(search, o.match);
			}
		});

		System.arraycopy(this.data, putIn, this.data, putIn + 1, this.count - putIn);
		this.data[putIn] = new MemEntry(id, search);
		if (putIn > 0) {
			this.data[putIn].prev = this.data[putIn - 1];
			this.data[putIn - 1].next = this.data[putIn];
		}
		if (putIn < this.count) {
			this.data[putIn].next = this.data[putIn + 1];
			this.data[putIn + 1].prev = this.data[putIn];
		}
		this.count++;
	}

	/**
	 * 
	 */
	private void ensureSpace()
	{
		if (count < this.data.length)
			return;
		MemEntry[] data = new MemEntry[this.data.length * 2];
		System.arraycopy(this.data, 0, data, 0, this.count);
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.index.Search.Searchable#get(int)
	 */
	@Override
	public Entry get(int index)
	{
		if (index < 0 || index >= this.count)
			throw new IndexOutOfBoundsException();
		return this.data[index];
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.index.Search.Searchable#count()
	 */
	@Override
	public int count()
	{
		return this.count;
	}

}
