package nl.thanod.evade.collection.index2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.collection.index.OffsetTable;
import nl.thanod.evade.collection.index.OffsetTable.OffsetTableEntry;
import nl.thanod.evade.collection.index.Search;
import nl.thanod.evade.collection.index.Search.Searchable;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.UUIDDocument;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.iterator.Generator;

public class PersistedIndex implements Searchable<IndexEntry>
{
	public final IndexDescriptor desc;

	protected final ByteBuffer data;
	protected final OffsetTable keyoffsets;
	protected final OffsetTable idoffsets;

	public class ReverseIndex implements Searchable<IDEntry>
	{
		final ByteBuffer content = data.duplicate();
		final ByteBufferDataInput reader = new ByteBufferDataInput(content);

		@Override
		public IDEntry get(final int index)
		{
			content.position(PersistedIndex.this.idoffsets.offset(index));
			ValueDocument id = (ValueDocument) DocumentSerializerVisitor.VERSIONED.deserialize(reader);
			return new IDEntry(id) {
				@Override
				public IndexEntry getParent()
				{
					OffsetTableEntry r = Search.after(PersistedIndex.this.keyoffsets, OffsetTable.OffsetTableEntry.search(PersistedIndex.this.idoffsets.offset(index)));
					return PersistedIndex.this.get(r.index);
				}
			};
		}

		@Override
		public int count()
		{
			return PersistedIndex.this.idoffsets.count();
		}
	}

	public PersistedIndex(File file) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		Header header = Header.read2(raf);

		// read descriptor
		ByteBufferDataInput descin = new ByteBufferDataInput(header.map(raf, Header.Type.INDEX_DESC));
		this.desc = IndexDescriptor.deserialize(DocumentSerializerVisitor.NON_VERSIONED.deserialize(descin));

		this.keyoffsets = new OffsetTable(header.map(raf, Header.Type.SORTED_INDEX));
		this.idoffsets = new OffsetTable(header.map(raf, Header.Type.UUID_INDEX));
		this.data = header.map(raf, Header.Type.DATA);

		raf.close();
	}

	@Override
	public IndexEntry get(int index)
	{
		// prepare the content
		final ByteBuffer content = this.data.duplicate();
		content.position(this.keyoffsets.offset(index));

		// change the limit except for the last block
		if (index + 1 < this.keyoffsets.count())
			content.limit(this.keyoffsets.offset(index + 1));

		final ByteBufferDataInput reader = new ByteBufferDataInput(content);
		ValueDocument key = (ValueDocument) DocumentSerializerVisitor.NON_VERSIONED.deserialize(reader);

		// iterator to iterate over UUID's containing the indexed value
		final Iterable<UUIDDocument> it = new Iterable<UUIDDocument>() {
			@Override
			public Iterator<UUIDDocument> iterator()
			{
				final ByteBufferDataInput reader = new ByteBufferDataInput(content.duplicate());
				return new Generator<UUIDDocument>() {
					@Override
					protected UUIDDocument generate() throws NoSuchElementException
					{
						try {
							UUIDDocument id = null;
							while (id == null) {
								Document doc = DocumentSerializerVisitor.VERSIONED.deserialize(reader);
								if (doc instanceof UUIDDocument)
									id = (UUIDDocument) doc;
							}
							return id;
						} catch (BufferUnderflowException ball) {
							throw new NoSuchElementException();
						}
					}
				};
			}
		};

		return new IndexEntry(index, key, it, null);
	}

	@Override
	public int count()
	{
		return this.keyoffsets.count();
	}

	public static void main(String[] args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");
		Database db = conf.loadDatabase();
		Table t = db.getCollection("github_small");

		PersistedIndex pi = new PersistedIndex(new File("data/eva_4784925109453924832.idx"));
		IndexEntry result;

		//		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("koenbollen")));
		//		System.out.println(result.index + ": " + result.key);
		//
		//		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("thanodnl")));
		//		System.out.println(result.index + ": " + result.key);
		//
		//		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("floort")));
		//		System.out.println(result.index + ": " + result.key );

		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("jorisdormans")));
		System.out.println(result.index + ": " + result.key);

		UUIDDocument last = null;
		for (UUIDDocument uuiddoc : result) {
			last = uuiddoc;
			System.out.println(uuiddoc.value + ": " + t.get(uuiddoc.value));
		}

		if (last != null) {
			IDEntry r = Search.binsearch(pi.getReverseIndex(), IDEntry.search(last));
			System.out.println(r.id);

			result = r.getParent();
			System.out.println(result.index + ": " + result.key);
			for (UUIDDocument uuiddoc : result) {
				last = uuiddoc;
				System.out.println(uuiddoc.value + ": " + t.get(uuiddoc.value));
			}
		}
	}

	private ReverseIndex getReverseIndex()
	{
		return new ReverseIndex();
	}
}
