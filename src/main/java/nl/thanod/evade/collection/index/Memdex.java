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

	public static void persistSortedIndex(Iterable<Document.Entry> c, List<String> path, Constraint constraint) throws IOException
	{
		Modifier mod = constraint.getModifier();

		File file = new File("data", "out0.index");

		final RandomAccessFile raf = new RandomAccessFile(file, "rw");

		List<Long> offsets = new ArrayList<Long>();

		DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(raf);
		// write the uuid index

		System.out.println("start persisting");
		for (Document.Entry e : c) {
			Document doc = e.doc.path(path);
			if (doc == null)
				continue;

			// do not index dict documents, instead make it a null index
			if (doc.type == Type.DICT)
				doc = new NullDocument(doc.version);

			// TODO modify the document

			// safe the index of the beginning of the entry
			offsets.add(raf.getFilePointer());

			// write the address of the indexed object
			raf.writeLong(e.id.getMostSignificantBits());
			raf.writeLong(e.id.getLeastSignificantBits());

			// write the contents of the document
			doc.accept(dsv);
		}
		System.out.println("started sorting");

		long took = System.nanoTime();

		// map data into memory for sorting
		final ByteBuffer map = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.getFilePointer());
		final ByteBufferDataInput di = new ByteBufferDataInput(map);

		// sort the indexlist
		Collections.sort(offsets, new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2)
			{
				Document d1;
				try {
					map.position(o1.intValue() + 16); // offset of 16 for the uuid
					d1 = DocumentSerializerVisitor.deserialize(di);
				} catch (Exception ball) {
					return -1;
				}

				Document d2;
				try {
					map.position(o2.intValue() + 16); // offset of 16 for the uuid
					d2 = DocumentSerializerVisitor.deserialize(di);
				} catch (Exception ball) {
					return 1;
				}

				return Document.VALUE_SORT.compare(d1, d2);
			}
		});
		took = System.nanoTime() - took;

		System.out.println("Sorted in " + took + "ns (" + took / 1000000 + "ms)");

		for (Long pos : offsets) {
			raf.writeInt(pos.intValue());
		}
		raf.close();
		System.out.println("data written");

		// write the sorted list
	}
}
