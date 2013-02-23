package nl.thanod.evade.collection.index2;

import nl.thanod.evade.document.ValueDocument;

public abstract class IDEntry
{
	public final ValueDocument id;

	public IDEntry(ValueDocument id)
	{
		this.id = id;
	}

	public abstract IndexEntry getParent();

	public static Comparable<IDEntry> search(final ValueDocument id)
	{
		return new Comparable<IDEntry>() {
			@Override
			public int compareTo(IDEntry that)
			{
				return ValueDocument.VALUE_COMPARE.compare(id, that.id);
			}
		};
	}
}
