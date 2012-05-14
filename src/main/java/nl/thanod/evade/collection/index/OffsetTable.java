/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.nio.ByteBuffer;

/**
 * @author nilsdijk
 */
public class OffsetTable
{
	private final ByteBuffer table;

	public OffsetTable(ByteBuffer table)
	{
		this.table = table;
	}

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
}
