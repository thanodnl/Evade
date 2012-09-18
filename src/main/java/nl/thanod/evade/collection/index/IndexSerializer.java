/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.document.*;
import nl.thanod.evade.document.Document.Type;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.ConvertedComparator;
import nl.thanod.evade.util.iterator.Sorterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class IndexSerializer
{

	private static final Logger log = LoggerFactory.getLogger(IndexSerializer.class);

	public static void compactIndices(File dir, String name, IndexDescriptor desc, Iterable<? extends Index> indices) throws IOException
	{
		Iterator<Index.Entry> index = new Sorterator<Index.Entry>(indices, Index.Entry.VALUE_COMPARE);

		// write the final index
		File file;
		int i = 0;
		do {
			file = new File(dir, name + i++ + ".idx");
		} while (file.exists());
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		Header indexHeader = new Header(-1);

		// reserve some room for the index table
		Header.reserve(raf, 5);

		// store the start of the data blob of the index
		int offset = (int) raf.getFilePointer();
		indexHeader.put(Header.Type.DATA, offset);

		// list to store all offsets in the data blob for entries
		List<Long> sindex = new ArrayList<Long>();

		// optimize the writing of documents by serializing to these memory buffers
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);

		// write the data
		while (index.hasNext()) {
			Index.Entry e = index.next();
			sindex.add(raf.getFilePointer() - offset);
			if (sindex.size() % 100000 == 0)
				System.out.println(sindex.size() + ": " + e.match);

			// write the uuid
			dos.writeLong(e.id.getMostSignificantBits());
			dos.writeLong(e.id.getLeastSignificantBits());

			// write the document to cache for performace
			e.match.accept(DocumentSerializerVisitor.VISITOR, dos);

			// write the cached document to file
			raf.write(bos.toByteArray());
			bos.reset();
		}
		System.out.println(sindex.size());

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

		// write the index descriptor
		indexHeader.put(Header.Type.INDEX_DESC, (int) raf.getFilePointer());
		desc.serialize().accept(DocumentSerializerVisitor.VISITOR, dos);
		if (bos.size() > 0)
			raf.write(bos.toByteArray());
		bos.reset();

		indexHeader.put(Header.Type.EOF, (int) raf.getFilePointer());
		raf.seek(0);

		indexHeader.write(raf);

		raf.close();
	}

	public static File getIdxFile(Table table)
	{
		// write the final index
		File file;
		int i = 0;
		do {
			file = new File(table.directory, table.name + i++ + ".idx");
		} while (file.exists());
		return file;
	}

	public static List<File> createIndex(Table table, IndexDescriptor desc, int batchSize)
	{
		if (table == null)
			return Collections.emptyList();
		ArrayList<File> files = new ArrayList<File>();
		Iterator<Document.Entry> it = table.iterator();

		while (it.hasNext()) {
			File idxFile = getIdxFile(table);
			try {
				writeIndexFile(idxFile, it, desc, batchSize);
				files.add(idxFile);
				log.debug("written partial index for table {} to {}", table.name, idxFile);
			} catch (IOException ball) {
				log.error("Error while writing " + batchSize + " items from table " + table.name + " to index for '" + desc + "'", ball);
			}
		}

		files.trimToSize();
		return files;
	}

	private static File writeIndexFile(File file, Iterator<? extends Document.Entry> entries, IndexDescriptor desc, int count) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		Header indexHeader = new Header(-1);

		// reserve some room for the index table
		Header.reserve(raf, 5);

		// store the start of the data blob of the index
		int offset = (int) raf.getFilePointer();
		indexHeader.put(Header.Type.DATA, offset);

		// list to store all offsets in the data blob for entries
		List<Long> sindex = new ArrayList<Long>();

		// optimize the writing of documents by serializing to these memory buffers
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);

		// write the data
		while (entries.hasNext() && count != 0) {
			Document.Entry de = entries.next();

			Document doc = de.doc.get(desc.path);
			if (doc == null)
				continue;

			UUID id = de.id;
			ValueDocument idx;
			if (doc instanceof ValueDocument)
				idx = (ValueDocument) doc;
			else
				idx = new NullDocument(doc.version);

			sindex.add(raf.getFilePointer() - offset);

			// write the uuid
			dos.writeLong(id.getMostSignificantBits());
			dos.writeLong(id.getLeastSignificantBits());

			// write the document to cache
			idx.accept(DocumentSerializerVisitor.VISITOR, dos);

			// write the cached document to file
			raf.write(bos.toByteArray());
			bos.reset();

			if (count > 0)
				count--;
		}

		// put the sorted index starting position in the header
		indexHeader.put(Header.Type.SORTED_INDEX, (int) raf.getFilePointer());

		final ByteBuffer map = indexHeader.map(raf, Header.Type.DATA);
		// sort the offset table on the entries
		Collections.sort(sindex, new ConvertedComparator<Long, ValueDocument>(ValueDocument.VALUE_COMPARE) {
			ByteBufferDataInput reader = new ByteBufferDataInput(map);

			@Override
			protected ValueDocument convert(Long from)
			{
				map.position(from.intValue() + 16); // skip 16 bytes because of the uuid

				Document doc = DocumentSerializerVisitor.deserialize(reader);

				if (doc instanceof ValueDocument)
					return (ValueDocument) doc;
				else
					return new NullDocument(doc.version);

			}
		});

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

		// write the index descriptor
		indexHeader.put(Header.Type.INDEX_DESC, (int) raf.getFilePointer());
		desc.serialize().accept(DocumentSerializerVisitor.VISITOR, dos);
		if (bos.size() > 0)
			raf.write(bos.toByteArray());
		bos.reset();

		indexHeader.put(Header.Type.EOF, (int) raf.getFilePointer());
		raf.seek(0);

		indexHeader.write(raf);

		raf.close();

		return file;
	}

	public static List<File> persistSortedIndex(Iterable<Document.Entry> data, IndexDescriptor desc, File dir, String collectionName) throws IOException
	{
		List<File> files = new ArrayList<File>();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);

		File temp = File.createTempFile("eva_", ".idx");
		log.debug("writing first step of index on {} to {}", desc, temp);

		RandomAccessFile tempraf = new RandomAccessFile(temp, "rw");
		temp.deleteOnExit(); // be sure to remove the file when the jvm quits

		// write the temporary index to temp
		// and get an offset of entries from it
		List<Long> offsets = writeTempIndex(tempraf, data, desc.path, null);

		// map data into memory for sorting
		final ByteBuffer tmap = tempraf.getChannel().map(MapMode.READ_ONLY, 0, tempraf.getFilePointer());

		// sort the indexlist
		Collections.sort(offsets, new ConvertedComparator<Long, ValueDocument>(ValueDocument.VALUE_COMPARE) {
			final ByteBuffer map = tmap;

			@Override
			protected ValueDocument convert(Long from)
			{
				this.map.position(from.intValue() + 16);
				return (ValueDocument) DocumentSerializerVisitor.deserialize(new ByteBufferDataInput(this.map));
			}
		});

		// list to hold the starts of every index item
		List<Long> sindex = new LinkedList<Long>();

		// write the final index
		File file;
		int i = 0;
		do {
			file = new File(dir, collectionName + i++ + ".idx");
		} while (file.exists());

		// save the filename for later use
		files.add(file);

		// open the file for writing
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		Header indexHeader = new Header(-1);

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

		// write the index descriptor
		indexHeader.put(Header.Type.INDEX_DESC, (int) raf.getFilePointer());
		desc.serialize().accept(DocumentSerializerVisitor.VISITOR, dos);
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
		return files;
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

			doc = Modifier.safeModify(modifier, doc);

			// safe the index of the beginning of the entry
			offsets.add(temp.getFilePointer());

			// write the address of the indexed object
			dos.writeLong(e.id.getMostSignificantBits());
			dos.writeLong(e.id.getLeastSignificantBits());

			// write the contents of the document
			doc.accept(DocumentSerializerVisitor.VISITOR, dos);
			temp.write(bos.toByteArray());
			bos.reset();
		}
		return offsets;
	}
}
