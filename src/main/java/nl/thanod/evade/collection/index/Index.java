/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import nl.thanod.evade.collection.index.Search.Searchable;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author nilsdijk
 */
public abstract class Index implements Iterable<Index.Entry>, Searchable<Index.Entry>
{
	public static abstract class Entry
	{
		public final UUID id;
		public final Document match;

		public Entry(UUID id, Document match)
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
