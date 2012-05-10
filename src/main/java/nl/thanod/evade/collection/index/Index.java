/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.UUID;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public interface Index
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

	Iterable<Document.Entry> before(Document doc);
}
