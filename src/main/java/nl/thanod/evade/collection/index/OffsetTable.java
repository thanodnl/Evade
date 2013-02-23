/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.nio.ByteBuffer;

import nl.thanod.evade.collection.index.Search.Searchable;

/**
 * @author nilsdijk
 */
public class OffsetTable implements Searchable<OffsetTable.OffsetTableEntry>
{
	public static class OffsetTableEntry
	{
		public final int index;
		public final int offset;

		OffsetTableEntry(int index, int offset)
		{
			this.index = index;
			this.offset = offset;
		}

		public static Comparable<OffsetTableEntry> search(final int offset)
		{
			return new Comparable<OffsetTable.OffsetTableEntry>() {

				@Override
				public int compareTo(OffsetTableEntry o)
				{
					return offset - o.offset;
				}
			};
		}
	}

	private final ByteBuffer table;

	public OffsetTable(ByteBuffer table)
	{
		this.table = table;
	}

	@Override
	public int count()
	{
		return this.table.capacity() / 4;
	}

	public int offset(int index)
	{
		if (index >= count() || index < 0)
			throw new IndexOutOfBoundsException();
		return this.table.getInt(index * 4);
	}

	@Override
	public OffsetTableEntry get(int index)
	{
		return new OffsetTableEntry(index, offset(index));
	}
}
