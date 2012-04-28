/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.UUID;

import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.collection.index.Search.Searchable;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;

/**
 * @author nilsdijk
 */
public class SSIndex implements Searchable<Index.Entry>
{
	private class SSIndexEntry extends Index.Entry
	{
		public final int index;

		public SSIndexEntry(int index, UUID id, Document match)
		{
			super(id, match);
			this.index = index;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#next()
		 */
		@Override
		public Entry next()
		{
			return SSIndex.this.get(index + 1);
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#previous()
		 */
		@Override
		public Entry previous()
		{
			return SSIndex.this.get(index - 1);
		}
	}

	private final RandomAccessFile raf;

	private final ByteBuffer datamap;
	private final OffsetTable sortedIndex;
	private final OffsetTable uuidIndex;

	public SSIndex(File file) throws IOException
	{
		raf = new RandomAccessFile(file, "r");

		Header indexHeader = Header.read(raf);

		datamap = indexHeader.map(raf,Header. Type.DATA);
		sortedIndex = new OffsetTable(indexHeader.map(raf, Header.Type.SORTED_INDEX));
		uuidIndex = new OffsetTable(indexHeader.map(raf, Header.Type.UUID_INDEX));
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.index.Search.Searchable#get(int)
	 */
	@Override
	public Index.Entry get(int index)
	{
		try {
			ByteBuffer buffer = this.datamap.duplicate();
			buffer.position(sortedIndex.offset(index));
			DataInput in = new ByteBufferDataInput(buffer);
			UUID id = new UUID(in.readLong(), in.readLong());
			Document match = DocumentSerializerVisitor.deserialize(in);
			return new SSIndexEntry(index, id, match);
		} catch (Exception ball) {
			// something went terribly wrong
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.index.Search.Searchable#count()
	 */
	@Override
	public int count()
	{
		return this.sortedIndex.count();
	}
}
