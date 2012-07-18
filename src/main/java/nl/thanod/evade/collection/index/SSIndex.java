/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;

/**
 * @author nilsdijk
 */
public class SSIndex extends Index
{
	private class SSIndexEntry extends Index.Entry
	{
		public final int index;

		public SSIndexEntry(int index, UUID id, ValueDocument match)
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

	public final File file;

	public SSIndex(File file) throws IOException
	{
		this(file, new RandomAccessFile(file, "r"));
	}

	private SSIndex(File file, RandomAccessFile raf) throws IOException
	{
		this(file, raf, Header.read(raf));
	}

	private SSIndex(File file, RandomAccessFile raf, Header indexHeader) throws IOException
	{
		super(loadDescriptor(raf, indexHeader));
		this.file = file;
		this.raf = raf;

		datamap = indexHeader.map(raf, Header.Type.DATA);
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
			return new SSIndexEntry(index, id, (ValueDocument) match);
		} catch (Exception ball) {
			// something went terribly wrong
			// mainly index out of bounds
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

	private static IndexDescriptor loadDescriptor(RandomAccessFile raf, Header header) throws IOException
	{
		long pos = header.position(Header.Type.INDEX_DESC);

		if (pos < 0) // no descriptor available
			return null;

		// go to the place where the index descriptor is written
		raf.seek(pos);

		return IndexDescriptor.deserialize(DocumentSerializerVisitor.deserialize(raf));
	}
}
