/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import nl.thanod.evade.util.Generator;

/**
 * @author nilsdijk
 */
public class UUIDPositionIndex implements Search.Searchable<UUIDPositionIndex.Pointer>
{
	public static class Pointer
	{
		public final UUID id;
		public final int pos;
		public final int ordinal;

		protected Pointer(UUID id, int pos, int ordinal)
		{
			this.id = id;
			this.pos = pos;
			this.ordinal = ordinal;
		}

		@Override
		public String toString()
		{
			return this.id.toString() + ": " + this.pos;
		}

	}

	public static final int INDEXSIZE = 16 + 4;
	private final MappedByteBuffer buffer;
	private final Iterable<UUID> iterable = new Iterable<UUID>() {
		@Override
		public Iterator<UUID> iterator()
		{
			return new Generator<UUID>() {
				int index = 0;
				@Override
				protected UUID generate() throws NoSuchElementException
				{
					if (index >= count())
						throw new NoSuchElementException();
					return get(index++).id;
				}
			};
		}
	};

	public UUIDPositionIndex(MappedByteBuffer buffer)
	{
		this.buffer = buffer;
		//		this.validate();
	}

	@Override
	public UUIDPositionIndex.Pointer get(int ordinal)
	{
		int start = ordinal * INDEXSIZE;

		long msb = buffer.getLong(start);
		long lsb = buffer.getLong(start + 8);
		int pos = buffer.getInt(start + 16);

		return new Pointer(new UUID(msb, lsb), pos, ordinal);
	}

	@Override
	public int count()
	{
		return this.buffer.capacity() / INDEXSIZE;
	}

	public UUIDPositionIndex.Pointer before(final UUID id)
	{
		return Search.binsearch(this, new Comparable<Pointer>() {

			@Override
			public int compareTo(Pointer o)
			{
				return id.compareTo(o.id);
			}
		});
	}

	public void validate()
	{
		Pointer p = get(0);
		Pointer p2;
		for (int i = 1; i < count(); i++) {
			p2 = get(i);
			if (p.id.compareTo(p2.id) >= 0)
				throw new IllegalStateException("not ordered at index " + i);
			p = p2;
		}
	}

	public Iterable<UUID> uuids()
	{
		return this.iterable;
	}
}
