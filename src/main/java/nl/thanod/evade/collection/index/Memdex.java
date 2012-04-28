/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.Document.Type;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.Linked;

/**
 * @author nilsdijk
 */
public class Memdex implements Index
{
	private final Map<StringDocument, Linked<UUID>> map;
	private final Modifier modifier;

	private Memdex(Map<StringDocument, Linked<UUID>> map, Modifier modifier)
	{
		this.map = map;
		this.modifier = modifier;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.collection.index.Index#before(nl.thanod.evade.document
	 * .Document)
	 */
	@Override
	public Iterable<Entry> before(Document doc)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private static class DocumentSorter implements Comparator<Long>
	{

		private final ByteBuffer map;
		private final ByteBufferDataInput di;
		private final int documentOffset;

		public DocumentSorter(ByteBuffer map, int documentOffset)
		{
			this.map = map;
			this.di = new ByteBufferDataInput(this.map);

			this.documentOffset = documentOffset;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Long o1, Long o2)
		{
			Document d1;
			try {
				this.map.position(o1.intValue() + this.documentOffset); // offset of 16 for the uuid
				d1 = DocumentSerializerVisitor.deserialize(this.di);
			} catch (Exception ball) {
				return -1;
			}

			Document d2;
			try {
				this.map.position(o2.intValue() + this.documentOffset); // offset of 16 for the uuid
				d2 = DocumentSerializerVisitor.deserialize(this.di);
			} catch (Exception ball) {
				return 1;
			}

			return d1.compareTo(d2);
		}

	}

	public static void persistSortedIndex(Iterable<Document.Entry> data, List<String> path, Constraint constraint) throws IOException
	{
		Modifier mod = constraint.getModifier();

		File temp = File.createTempFile("eva_", ".idx");
		System.out.println(temp);

		RandomAccessFile traf = new RandomAccessFile(temp, "rw");

		System.out.println("start persisting");
		List<Long> offsets = writeTempIndex(traf, data, path);
		System.out.println("started sorting");

		// map data into memory for sorting
		ByteBuffer tmap = traf.getChannel().map(MapMode.READ_ONLY, 0, traf.getFilePointer());
		//final ByteBufferDataInput tdi = new ByteBufferDataInput(tmap);

		// sort the indexlist
		Collections.sort(offsets, new DocumentSorter(tmap, 16));

		// list to hold the starts of every index item
		List<Long> sindex = new LinkedList<Long>();

		// write the final index
		File file = new File("data", "out0.idx");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		// reserve some room for the index table
		raf.writeInt(0); // placeholder for the start of the sorted data
		raf.writeInt(0); // placeholder for the start of the sindex
		raf.writeInt(0); // placeholder for the start of the uuidindex
		raf.writeInt(0); // placeholder for the eof

		int datapos = (int) raf.getFilePointer();

		DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(raf);
		ByteBufferDataInput tdi = new ByteBufferDataInput(tmap);
		for (Long pos : offsets) {
			tmap.position(pos.intValue());

			// put the offset in the sindex
			sindex.add(raf.getFilePointer());

			// copy the uuid
			raf.writeLong(tmap.getLong());
			raf.writeLong(tmap.getLong());

			// copy the document
			Document doc = DocumentSerializerVisitor.deserialize(tdi);
			doc.accept(dsv);
		}

		// write the sindex to file
		int sindexpos = (int) raf.getFilePointer();
		for (Long pos : sindex)
			raf.writeInt(pos.intValue());

		int uuidpos = (int) raf.getFilePointer();

		// TODO write an offset table for the ordered uuid's
		// map the contents to memory
		final ByteBuffer map = raf.getChannel().map(MapMode.READ_ONLY, datapos, sindexpos - datapos);

		// sort the sindex to uuid
		Collections.sort(sindex, new Comparator<Long>() {

			@Override
			public int compare(Long o1, Long o2)
			{
				map.position(o1.intValue());
				UUID uuid1 = new UUID(map.getLong(), map.getLong());

				map.position(o2.intValue());
				UUID uuid2 = new UUID(map.getLong(), map.getLong());

				return uuid1.compareTo(uuid2);
			}
		});

		// write the uuid's offsettable
		for (Long pos : sindex)
			raf.writeInt(pos.intValue());

		int eofpos = (int) raf.getFilePointer();

		// update the index table
		raf.seek(0);
		raf.writeInt(datapos); // the start of the sorted data
		raf.writeInt(sindexpos); // the start of the sindex
		raf.writeInt(uuidpos); // the start of the uuidindex
		raf.writeInt(eofpos); // the eof

		// remove temp files
		try {
			raf.close();
		} catch (IOException ball) {
		}
		try {
			traf.close();
		} catch (IOException ball) {
		}
		try {
			temp.delete();
		} catch (Exception ball) {
			// file could not be removed
			ball.printStackTrace(); // TODO make better debugging for this
			temp.deleteOnExit(); // be sure to remove the file when the jvm quits
		}
	}

	private static List<Long> writeTempIndex(RandomAccessFile traf, Iterable<Document.Entry> data, List<String> path) throws IOException
	{
		List<Long> offsets = new ArrayList<Long>();
		DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(traf);
		for (Document.Entry e : data) {
			Document doc = e.doc.path(path);
			if (doc == null)
				continue;

			// do not index dict documents, instead make it a null index
			if (doc.type == Type.DICT)
				doc = new NullDocument(doc.version);

			// TODO modify the document

			// safe the index of the beginning of the entry
			offsets.add(traf.getFilePointer());

			// write the address of the indexed object
			traf.writeLong(e.id.getMostSignificantBits());
			traf.writeLong(e.id.getLeastSignificantBits());

			// write the contents of the document
			doc.accept(dsv);
		}
		return offsets;
	}
}
