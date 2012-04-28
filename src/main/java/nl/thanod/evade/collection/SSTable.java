/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;

import nl.thanod.evade.collection.index.UUIDPositionIndex;
import nl.thanod.evade.collection.index.UUIDPositionIndex.Pointer;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.CountingOutputStream;
import nl.thanod.evade.util.Generator;

/**
 * @author nilsdijk
 */
public class SSTable extends Collection
{

	private static final File DATA_DIR = new File("data");
	private static final int DEF_DATA_SIZE = 64 * 1024 * 1024;
	private static final int MAX_DATA_SIZE = 512 * 1024 * 1024;

	public final File file;

	protected final UUIDPositionIndex index;
	private final MappedByteBuffer datamap;

	private final UUID min;
	private final UUID max;

	public SSTable(File file) throws IOException
	{
		this.file = file;

		FileInputStream fin = new FileInputStream(file);
		FileChannel ch = fin.getChannel();
		ch.position(ch.size() - 4);

		DataInputStream din = new DataInputStream(fin);
		int indexstart = din.readInt();

		// the index is located at start index until the end of the file minus the 4 bytes
		// indicating the start of the index
		index = new UUIDPositionIndex(ch.map(MapMode.READ_ONLY, indexstart, ch.size() - indexstart - 4));

		this.min = this.index.get(0).id;
		this.max = this.index.get(this.index.count() - 1).id;

		// the beginning of the file to the start of the index is the datapart of the file
		datamap = ch.map(MapMode.READ_ONLY, 0, indexstart);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id)
	{
		if (this.min.compareTo(id) > 0 || this.max.compareTo(id) < 0)
			return false;
		return this.index.before(id).id.equals(id);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id)
	{
		Pointer p = this.index.before(id);
		if (!p.id.equals(id))
			return null;
		return get(p);
	}

	public Document get(UUIDPositionIndex.Pointer pointer)
	{
		return DocumentSerializerVisitor.deserialize(new ByteBufferDataInput((ByteBuffer) this.datamap.duplicate().position(pointer.pos)));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		return new Generator<Document.Entry>() {
			int ordinal = 0;

			@Override
			protected Entry generate() throws NoSuchElementException
			{
				if (ordinal >= SSTable.this.index.count())
					throw new NoSuchElementException();
				Pointer p = SSTable.this.index.get(ordinal);
				++ordinal;
				return new Entry(p.id, SSTable.this.get(p));
			}
		};
	}

	public static List<File> save(Iterable<Document.Entry> data) throws FileNotFoundException, IOException
	{
		return SSTable.save(data, SSTable.DEF_DATA_SIZE); // save in blobs of 512 megabyte
	}

	public static List<File> save(Iterable<Document.Entry> data, int maxdatasize) throws FileNotFoundException, IOException
	{
		// be sure that the max datasize is not exceeding the system setting
		maxdatasize = Math.min(maxdatasize, MAX_DATA_SIZE);

		List<File> files = new LinkedList<File>();
		Iterator<Document.Entry> it = data.iterator();
		while (it.hasNext()) {
			int idx = 0;
			File f;
			do {
				f = new File(DATA_DIR, "out" + idx++ + ".sstable");
			} while (f.exists());

			Map<UUID, Integer> index = new TreeMap<UUID, Integer>();

			FileOutputStream fos = new FileOutputStream(f);
			CountingOutputStream cos = new CountingOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(cos);
			DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(dos);

			// write the datablob
			while (it.hasNext() && cos.getCount() < maxdatasize) {
				Document.Entry e = it.next();
				index.put(e.id, cos.getCount());

				// serialize document
				e.doc.accept(dsv);
			}

			int indexstart = cos.getCount();

			// write the index
			for (Map.Entry<UUID, Integer> e : index.entrySet()) {
				dos.writeLong(e.getKey().getMostSignificantBits());
				dos.writeLong(e.getKey().getLeastSignificantBits());
				dos.writeInt(e.getValue());
			}

			// the offset where the index starts
			dos.writeInt(indexstart);

			// close the file
			dos.close();
			files.add(f);
		}
		return files;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#size()
	 */
	@Override
	public int size()
	{
		return this.index.count();
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public Iterable<UUID> uuids()
	{
		return this.index.uuids();
	}
}
