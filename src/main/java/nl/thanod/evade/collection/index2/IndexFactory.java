package nl.thanod.evade.collection.index2;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.UUIDDocument;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.ByteBufferOutputStream;
import nl.thanod.evade.util.ConvertedComparator;

public class IndexFactory
{

	/**
	 * Size of blocks to be allocated (Direct) in Mb
	 */
	private int blocksize = 64 * 1024 * 1024;

	public IndexFactory()
	{

	}

	public void createIndex(IndexDescriptor desc, Iterable<? extends Document.Entry> data) throws IOException
	{
		List<Integer> offsets = new ArrayList<Integer>();
		ByteBuffer dataTable = ByteBufferFactory.getDefault().create(blocksize);

		DataOutputStream dosDataTable = new DataOutputStream(new ByteBufferOutputStream(dataTable));

		for (Document.Entry e : data) {
			ValueDocument value = ValueDocument.from(e.doc.get(desc.path));
			if (value == null)
				continue;

			// write the offset
			offsets.add(dataTable.position());

			// FORMAT: value - version - uuid
			value.accept(DocumentSerializerVisitor.VERSIONED, dosDataTable);
			dosDataTable.writeLong(e.id.getMostSignificantBits());
			dosDataTable.writeLong(e.id.getLeastSignificantBits());

			dosDataTable.flush();
		}

		System.out.println("data: " + dataTable.position() + "b");
		System.out.println("indx: " + offsets.size() + " entries");

		// sort the offsets
		final ByteBuffer view = (ByteBuffer) dataTable.duplicate().flip();
		Collections.sort(offsets, new ConvertedComparator<Integer, ValueDocument>(ValueDocument.VALUE_COMPARE) {
			ByteBufferDataInput reader = new ByteBufferDataInput(view);

			@Override
			protected ValueDocument convert(Integer from)
			{
				view.position(from.intValue());

				Document doc = DocumentSerializerVisitor.VERSIONED.deserialize(reader);

				if (doc instanceof ValueDocument)
					return (ValueDocument) doc;
				else
					return new NullDocument(doc.version);
			}
		});
		System.out.println("Sorted!");

		// create final data
		dataTable = ByteBufferFactory.getDefault().create(blocksize);
		dosDataTable = new DataOutputStream(new ByteBufferOutputStream(dataTable));

		List<Integer> finalOffsets = new ArrayList<Integer>();

		ByteBufferDataInput reader = new ByteBufferDataInput(view);
		ValueDocument key = null;
		for (int i : offsets) {
			view.position(i);

			ValueDocument doc = (ValueDocument) DocumentSerializerVisitor.VERSIONED.deserialize(reader);
			UUID id = new UUID(reader.readLong(), reader.readLong());

			if (ValueDocument.VALUE_COMPARE.compare(doc, key) != 0) {
				// new key
				key = doc;

				// write the indexed value
				finalOffsets.add(dataTable.position());
				key.accept(DocumentSerializerVisitor.NON_VERSIONED, dosDataTable);
			}

			ValueDocument vd = new UUIDDocument(doc.version, id);
			vd.accept(DocumentSerializerVisitor.VERSIONED, dosDataTable);
		}

		System.out.println("data: " + dataTable.position() + "b");
		System.out.println("indx: " + finalOffsets.size() + " entries");

		// persist to file

		File tmp = File.createTempFile("eva_", ".idx");
		System.out.println("flushing to " + tmp);
		RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
		long start, took;

		Header header = new Header(2);

		// Index body
		header.put(Header.Type.DATA, raf.getFilePointer());
		start = System.nanoTime();
		raf.getChannel().write((ByteBuffer) dataTable.flip());
		took = System.nanoTime() - start;
		System.out.println("Flushing data took " + took + "ns (" + (took / 1000000f) + "ms)");

		System.out.println("table starts at " + raf.getFilePointer());

		// Offset table
		header.put(Header.Type.SORTED_INDEX, raf.getFilePointer());
		start = System.nanoTime();
		flushOffsetTable(finalOffsets, raf);
		took = System.nanoTime() - start;
		System.out.println("Flushing offset table took " + took + "ns (" + (took / 1000000f) + "ms)");

		// Index Descriptor
		header.put(Header.Type.INDEX_DESC, raf.getFilePointer());
		start = System.nanoTime();
		desc.serialize().accept(DocumentSerializerVisitor.NON_VERSIONED, raf);
		took = System.nanoTime() - start;
		System.out.println("Flushing index descriptor took " + took + "ns (" + (took / 1000000f) + "ms)");

		header.put(Header.Type.EOF, raf.getFilePointer());

		start = System.nanoTime();
		header.write2(raf);
		took = System.nanoTime() - start;
		System.out.println("Flushing header table took " + took + "ns (" + (took / 1000000f) + "ms)");

		raf.close();
	}

	private static void flushOffsetTable(Iterable<Integer> offsets, DataOutput target) throws IOException
	{
		for (int offset : offsets)
			target.writeInt(offset);
	}

	public static void main(String[] args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		Table t = db.getCollection("github_small");

		IndexFactory idxf = new IndexFactory();
		IndexDescriptor desc = new IndexDescriptor(new DocumentPath("actor"));
		idxf.createIndex(desc, t);
	}
}
