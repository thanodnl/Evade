/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import nl.thanod.evade.collection.index.Search.Searchable;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author nilsdijk
 */
public abstract class Index implements Iterable<Index.Entry>, Searchable<Index.Entry>
{
	public static abstract class Entry
	{
		public static final Comparator<Index.Entry> VALUE_COMPARE = new Comparator<Index.Entry>() {
			@Override
			public int compare(Entry o1, Entry o2)
			{
				return ValueDocument.VALUE_COMPARE.compare(o1.match, o2.match);
			}
		};

		public final UUID id;
		public final ValueDocument match;

		public Entry(Entry source)
		{
			this.id = source.id;
			this.match = source.match;
		}

		public Entry(UUID id, ValueDocument match)
		{
			this.id = id;
			this.match = match;
		}

		public abstract Entry next();

		public abstract Entry previous();

		@Override
		public String toString()
		{
			return this.id + ": " + this.match;
		}
	}
	
	public final IndexDescriptor desc;

	public Index(IndexDescriptor desc)
	{
		this.desc = desc;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Index.Entry> iterator()
	{
		return new Generator<Index.Entry>() {
			Index.Entry cursor = get(0);

			@Override
			protected Entry generate() throws NoSuchElementException
			{
				if (this.cursor == null)
					throw new NoSuchElementException();
				Index.Entry ret = this.cursor;
				this.cursor = this.cursor.next();
				return ret;
			}
		};
	}
}
