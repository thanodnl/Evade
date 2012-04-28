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

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Type;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.ConvertedComparator;

/**
 * @author nilsdijk
 */
public class Memdex
{

	public static void persistSortedIndex(Iterable<Document.Entry> data, List<String> path, Constraint constraint) throws IOException
	{
		Modifier mod = constraint.getModifier();

		File temp = File.createTempFile("eva_", ".idx");
		System.out.println(temp);

		RandomAccessFile tempraf = new RandomAccessFile(temp, "rw");

		// write the temporary index to temp
		// and get an offset of entries from it
		List<Long> offsets = writeTempIndex(tempraf, data, path);

		// map data into memory for sorting
		final ByteBuffer tmap = tempraf.getChannel().map(MapMode.READ_ONLY, 0, tempraf.getFilePointer());

		// sort the indexlist
		Collections.sort(offsets, new ConvertedComparator<Long, Document>() {
			final ByteBuffer map = tmap;

			@Override
			protected Document convert(Long from)
			{
				this.map.position(from.intValue() + 16);
				return DocumentSerializerVisitor.deserialize(new ByteBufferDataInput(this.map));
			}
		});

		// list to hold the starts of every index item
		List<Long> sindex = new LinkedList<Long>();

		// write the final index
		File file = new File("data", "out0.idx");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		IndexHeader indexHeader = new IndexHeader();

		// reserve some room for the index table
		indexHeader.write(raf);

		indexHeader.setDataPosition((int) raf.getFilePointer());

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

		// put the sorted index starting position in the header
		indexHeader.setSortedIndexPosition((int) raf.getFilePointer());
		// write the sindex to file
		for (Long pos : sindex)
			raf.writeInt(pos.intValue());

		// put the uuid index starting poisition in the header
		indexHeader.setUUIDIndexPosition((int) raf.getFilePointer());

		// map the contents to memory
		final ByteBuffer map = indexHeader.mapData(raf);

		// sort the sindex to be used as uuid index
		Collections.sort(sindex, new ConvertedComparator<Long, UUID>() {
			@Override
			public UUID convert(Long from)
			{
				map.position(from.intValue());
				return new UUID(map.getLong(), map.getLong());
			}
		});

		// write the uuid's index
		for (Long pos : sindex)
			raf.writeInt(pos.intValue());

		indexHeader.setEOFPosition((int) raf.getFilePointer());

		// update the index table
		raf.seek(0); // stored at the begin of the file
		indexHeader.write(raf);

		// remove temp files
		try {
			raf.close();
		} catch (IOException ball) {
		}
		try {
			tempraf.close();
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

	private static List<Long> writeTempIndex(RandomAccessFile temp, Iterable<Document.Entry> data, List<String> path) throws IOException
	{
		List<Long> offsets = new ArrayList<Long>();
		DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(temp);
		for (Document.Entry e : data) {
			Document doc = e.doc.path(path);
			if (doc == null)
				continue;

			// do not index dict documents, instead make it a null index
			if (doc.type == Type.DICT)
				doc = new NullDocument(doc.version);

			// TODO modify the document

			// safe the index of the beginning of the entry
			offsets.add(temp.getFilePointer());

			// write the address of the indexed object
			temp.writeLong(e.id.getMostSignificantBits());
			temp.writeLong(e.id.getLeastSignificantBits());

			// write the contents of the document
			doc.accept(dsv);
		}
		return offsets;
	}
}
