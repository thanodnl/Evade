/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

import nl.thanod.evade.collection.index.UUIDPositionIndex;
import nl.thanod.evade.collection.index.UUIDPositionIndex.Pointer;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.ByteBufferDataInput;
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
	private final ByteBuffer datamap;

	private final UUID min;
	private final UUID max;

	public SSTable(File file) throws IOException
	{
		this.file = file;

		RandomAccessFile raf = new RandomAccessFile(file, "r");
		Header header = Header.read(raf);

		// the index is located at start index until the end of the file minus the 4 bytes
		// indicating the start of the index
		index = new UUIDPositionIndex(header.map(raf, Header.Type.UUID_INDEX));

		this.min = this.index.get(0).id;
		this.max = this.index.get(this.index.count() - 1).id;

		// the beginning of the file to the start of the index is the datapart of the file
		datamap = header.map(raf, Header.Type.DATA);
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

			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			DocumentSerializerVisitor dsv = new DocumentSerializerVisitor(raf);

			Header.reserve(raf, 3);

			Header header = new Header();

			long offset = raf.getFilePointer();
			header.put(Header.Type.DATA, offset);

			// write the datablob
			while (it.hasNext() && raf.getFilePointer() < maxdatasize) {
				Document.Entry e = it.next();
				index.put(e.id, (int) (raf.getFilePointer() - offset));

				// serialize document
				e.doc.accept(dsv);
			}

			header.put(Header.Type.UUID_INDEX, raf.getFilePointer());

			// write the index
			for (Map.Entry<UUID, Integer> e : index.entrySet()) {
				raf.writeLong(e.getKey().getMostSignificantBits());
				raf.writeLong(e.getKey().getLeastSignificantBits());
				raf.writeInt(e.getValue());
			}

			header.put(Header.Type.EOF, raf.getFilePointer());

			// the offset where the index starts
			raf.seek(0);
			header.write(raf);

			// close the file
			raf.close();
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

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#ids()
	 */
	@Override
	public Iterable<UUID> uuids()
	{
		return this.index.uuids();
	}
}
