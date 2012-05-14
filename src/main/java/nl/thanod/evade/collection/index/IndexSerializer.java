/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;

import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Type;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.document.visitor.ParamDocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.ConvertedComparator;
import nl.thanod.evade.util.iterator.Sorterator;

/**
 * @author nilsdijk
 */
public class IndexSerializer
{

	public static void compactIndices(Iterable<? extends Index> indices) throws IOException
	{
		Iterator<Index.Entry> index = new Sorterator<Index.Entry>(indices, new Comparator<Index.Entry>() {
			@Override
			public int compare(Entry o1, Entry o2)
			{
				return o1.match.compareTo(o2.match);
			}
		});

		// write the final index
		File file;
		int i = 0;
		do {
			file = new File("data", "out" + i++ + ".idx");
		} while (file.exists());
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		Header indexHeader = new Header();

		// reserve some room for the index table
		Header.reserve(raf, 3);

		// store the start of the data blob of the index
		int offset = (int) raf.getFilePointer();
		indexHeader.put(Header.Type.DATA, offset);

		// list to store all offsets in the data blob for entries
		List<Long> sindex = new ArrayList<Long>();

		// optimize the writing of documents by serializing to these memory buffers
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);

		// write the data
		while(index.hasNext()){
			Index.Entry e = index.next();
			sindex.add(raf.getFilePointer() - offset);
			if (sindex.size() % 100000 == 0)
				System.out.println(sindex.size() + ": " + e.match);

			// write the uuid
			raf.writeLong(e.id.getMostSignificantBits());
			raf.writeLong(e.id.getLeastSignificantBits());

			// write the document to cache for performace
			e.match.accept(ParamDocumentSerializerVisitor.VISITOR, dos);

			// write the cached document to file
			raf.write(bos.toByteArray());
			bos.reset();
		}

		// put the sorted index starting position in the header
		indexHeader.put(Header.Type.SORTED_INDEX, (int) raf.getFilePointer());

		// write the sorted index to file
		for (Long pos : sindex) {
			dos.writeInt(pos.intValue());

			// if buffer is bigger than 10 megs flush it to file
			if (bos.size() > 10 * 1024 * 1024) {
				raf.write(bos.toByteArray());
				bos.reset();
			}
		}
		// write last part of the sorted index
		if (bos.size() > 0)
			raf.write(bos.toByteArray());
		bos.reset();

		indexHeader.put(Header.Type.EOF, (int) raf.getFilePointer());
		raf.seek(0);

		indexHeader.write(raf);

		raf.close();
	}

	public static void persistSortedIndex(Iterable<Document.Entry> data, DocumentPath path, Modifier modifier) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);

		File temp = File.createTempFile("eva_", ".idx");
		System.out.println(temp);

		RandomAccessFile tempraf = new RandomAccessFile(temp, "rw");
		temp.deleteOnExit(); // be sure to remove the file when the jvm quits

		// write the temporary index to temp
		// and get an offset of entries from it
		List<Long> offsets = writeTempIndex(tempraf, data, path, modifier);

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
		File file;
		int i = 0;
		do {
			file = new File("data", "out" + i++ + ".idx");
		} while (file.exists());
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		Header indexHeader = new Header();

		// reserve some room for the index table
		Header.reserve(raf, 4);

		int offset = (int) raf.getFilePointer();
		indexHeader.put(Header.Type.DATA, offset);

		ByteBufferDataInput tdi = new ByteBufferDataInput(tmap);
		for (Long pos : offsets) {
			tmap.position(pos.intValue());

			// put the offset in the sindex
			sindex.add(raf.getFilePointer() - offset);

			// copy the uuid
			dos.writeLong(tmap.getLong());
			dos.writeLong(tmap.getLong());

			// copy the document to cache
			DocumentSerializerVisitor.move(tdi, dos);

			// flush data from cache
			raf.write(bos.toByteArray());
			bos.reset();
		}

		// put the sorted index starting position in the header
		indexHeader.put(Header.Type.SORTED_INDEX, (int) raf.getFilePointer());
		
		// write the sindex to file
		for (Long pos : sindex) {
			dos.writeInt(pos.intValue());
			if (bos.size() > 10 * 1024 * 1024) {
				raf.write(bos.toByteArray());
				bos.reset();
			}
		}
		if (bos.size() > 0)
			raf.write(bos.toByteArray());
		bos.reset();

		// put the uuid index starting poisition in the header
		indexHeader.put(Header.Type.UUID_INDEX, (int) raf.getFilePointer());

		// map the contents to memory
		final ByteBuffer map = indexHeader.map(raf, Header.Type.DATA);

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
		for (Long pos : sindex) {
			dos.writeInt(pos.intValue());
			if (bos.size() > 10 * 1024 * 1024) {
				raf.write(bos.toByteArray());
				bos.reset();
			}
		}
		if (bos.size() > 0)
			raf.write(bos.toByteArray());
		bos.reset();

		indexHeader.put(Header.Type.EOF, (int) raf.getFilePointer());

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
		}
	}

	private static List<Long> writeTempIndex(RandomAccessFile temp, Iterable<Document.Entry> data, DocumentPath path, Modifier modifier) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);
		List<Long> offsets = new ArrayList<Long>();
		for (Document.Entry e : data) {
			Document doc = e.doc.get(path);
			if (doc == null)
				continue;

			// do not index dict documents, instead make it a null index
			if (doc.type == Type.DICT)
				doc = new NullDocument(doc.version);

			doc = doc.modify(modifier);

			// safe the index of the beginning of the entry
			offsets.add(temp.getFilePointer());

			// write the address of the indexed object
			dos.writeLong(e.id.getMostSignificantBits());
			dos.writeLong(e.id.getLeastSignificantBits());

			// write the contents of the document
			doc.accept(ParamDocumentSerializerVisitor.VISITOR, dos);
			temp.write(bos.toByteArray());
			bos.reset();
		}
		return offsets;
	}
}
