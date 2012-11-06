/**
 * 
 */
package nl.thanod.evade.document.util;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.util.iterator.Unduplicator;

/**
 * @author nilsdijk
 */
public class DocumentUnduplicator implements Unduplicator.Predicate<Document.Entry>
{

	DocumentUnduplicator()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.util.iterator.Unduplicator.Predicate#shouldMerge(java
	 * .lang.Object, java.lang.Object)
	 */
	@Override
	public boolean shouldMerge(Entry t1, Entry t2)
	{
		return t1.id.equals(t2.id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.util.iterator.Unduplicator.Predicate#merge(java.lang.
	 * Object, java.lang.Object)
	 */
	@Override
	public Entry merge(Entry t1, Entry t2)
	{
		return new Document.Entry(t1.id, Document.merge(t1.doc, t2.doc));
	}

	private static final class Singleton
	{
		public static final DocumentUnduplicator INSTANCE = new DocumentUnduplicator();
	}

	public static DocumentUnduplicator get()
	{
		return Singleton.INSTANCE;
	}

}
