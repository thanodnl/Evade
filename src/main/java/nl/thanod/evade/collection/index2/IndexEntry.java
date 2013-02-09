package nl.thanod.evade.collection.index2;

import java.util.Iterator;

import nl.thanod.evade.collection.index.Search.Searchable;
import nl.thanod.evade.document.UUIDDocument;
import nl.thanod.evade.document.ValueDocument;

public class IndexEntry implements Iterable<UUIDDocument>, Searchable<UUIDDocument>
{
	/**
	 * Non-versioned (version is undefined) key as the value being indexed
	 */
	public final ValueDocument key;

	/**
	 * Provider for a sequence of {@link UUIDDocument}
	 */
	private final Iterable<UUIDDocument> iterable;

	private final Searchable<UUIDDocument> searchable;

	public final int index;

	public <T extends Iterable<UUIDDocument> & Searchable<UUIDDocument>> IndexEntry(int index, ValueDocument key, T t)
	{
		this(index, key, t, t);
	}

	public IndexEntry(int index, ValueDocument key, Iterable<UUIDDocument> iterable, Searchable<UUIDDocument> searchable)
	{
		this.index = index;
		this.key = key;
		this.iterable = iterable;
		this.searchable = searchable;
	}

	@Override
	public Iterator<UUIDDocument> iterator()
	{
		return this.iterable.iterator();
	}

	@Override
	public UUIDDocument get(int index)
	{
		return this.searchable.get(index);
	}

	@Override
	public int count()
	{
		return this.searchable.count();
	}

	public static Comparable<IndexEntry> compare(final ValueDocument doc)
	{
		return new Comparable<IndexEntry>() {
			@Override
			public int compareTo(IndexEntry ie)
			{
				return ValueDocument.VALUE_COMPARE.compare(doc, ie.key);
			}
		};
	}
}
