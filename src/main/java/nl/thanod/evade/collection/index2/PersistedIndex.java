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

	private final ByteBuffer data;
	private final OffsetTable offsets;

	public PersistedIndex(File file) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		Header header = Header.read2(raf);

		// read descriptor
		ByteBufferDataInput descin = new ByteBufferDataInput(header.map(raf, Header.Type.INDEX_DESC));
		this.desc = IndexDescriptor.deserialize(DocumentSerializerVisitor.NON_VERSIONED.deserialize(descin));

		this.offsets = new OffsetTable(header.map(raf, Header.Type.SORTED_INDEX));
		this.data = header.map(raf, Header.Type.DATA);

		raf.close();
	}

	@Override
	public IndexEntry get(int index)
	{
		// prepare the content
		final ByteBuffer content = this.data.duplicate();
		content.position(this.offsets.offset(index));

		// change the limit except for the last block
		if (index + 1 < this.offsets.count())
			content.limit(this.offsets.offset(index + 1));

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
		return this.offsets.count();
	}

	public static void main(String[] args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");
		Database db = conf.loadDatabase();
		Table t = db.getCollection("github_small");

		PersistedIndex pi = new PersistedIndex(new File("/Users/nilsdijk/Documents/workspace/Evade/data/github_small_name.idx"));
		IndexEntry result;

		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("koenbollen")));
		System.out.println(result.index + ": " + result.key);

		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("thanodnl")));
		System.out.println(result.index + ": " + result.key);

		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("floort")));
		System.out.println(result.index + ": " + result.key);

		result = Search.binsearch(pi, IndexEntry.compare(new StringDocument("jorisdormans")));
		System.out.println(result.index + ": " + result.key);

		for (UUIDDocument uuiddoc : result) {
			System.out.println(t.get(uuiddoc.value));
		}
	}
}
