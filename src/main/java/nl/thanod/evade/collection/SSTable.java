/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import nl.thanod.evade.collection.index.UUIDPositionIndex;
import nl.thanod.evade.collection.index.UUIDPositionIndex.Pointer;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.document.visitor.ParamDocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.store.bloom.Bloom;
import nl.thanod.evade.store.bloom.BloomFilter;
import nl.thanod.evade.store.bloom.BloomHasher;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.Generator;

/**
 * @author nilsdijk
 */
public class SSTable extends Collection implements Closeable
{
	private static final int DEF_DATA_SIZE = 256 * 1024 * 1024;
	private static final int MAX_DATA_SIZE = 512 * 1024 * 1024;

	public final File file;

	private final RandomAccessFile raf;
	protected final UUIDPositionIndex index;
	private final ByteBuffer datamap;
	private final BloomFilter bloom;

	private final UUID min;
	private final UUID max;

	public SSTable(File file) throws IOException
	{
		this.file = file;

		this.raf = new RandomAccessFile(file, "r");
		Header header = Header.read(raf);

		// the index is located at start index until the end of the file minus the 4 bytes
		// indicating the start of the index
		this.index = new UUIDPositionIndex(header.map(raf, Header.Type.UUID_INDEX));

		this.min = this.index.get(0).id;
		this.max = this.index.get(this.index.count() - 1).id;

		// the beginning of the file to the start of the index is the datapart of the file
		this.datamap = header.map(raf, Header.Type.DATA);
		this.bloom = BloomFilter.fromBuffer(header.map(raf, Header.Type.BLOOM));
	}

	@Override
	protected void finalize()
	{
		this.close();
	}

	public boolean earlySkip(Bloom<?> bloom)
	{
		return !(this.bloom != null && bloom.containedBy(this.bloom));
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
		final ByteBuffer buffer = this.datamap.duplicate();
		buffer.position(0);
		final DataInput input = new ByteBufferDataInput(buffer);

		return new Generator<Document.Entry>() {
			int ordinal = 0;

			@Override
			protected Entry generate() throws NoSuchElementException
			{
				if (ordinal >= SSTable.this.index.count())
					throw new NoSuchElementException();
				Pointer p = SSTable.this.index.get(ordinal);
				++ordinal;
				if (p.pos != buffer.position())
					buffer.position(p.pos);
				return new Entry(p.id, DocumentSerializerVisitor.deserialize(input));
			}
		};
	}

	public static List<File> save(File directory, String name, Iterable<Document.Entry> data) throws FileNotFoundException, IOException
	{
		return SSTable.save(directory, name, data, SSTable.DEF_DATA_SIZE); // save in blobs of 512 megabyte
	}

	public static List<File> save(File datadir, String name, Iterable<Document.Entry> data, int maxdatasize) throws FileNotFoundException, IOException
	{
		// be sure that the max datasize is not exceeding the system setting
		maxdatasize = Math.min(maxdatasize, MAX_DATA_SIZE);

		List<File> files = new LinkedList<File>();
		Iterator<Document.Entry> it = data.iterator();
		while (it.hasNext()) {
			int idx = 0;
			File f;
			do {
				f = new File(datadir, name + idx++ + ".sstable");
			} while (f.exists());

			Map<UUID, Integer> index = new TreeMap<UUID, Integer>();

			RandomAccessFile raf = new RandomAccessFile(f, "rw");

			ByteArrayOutputStream bos = new ByteArrayOutputStream(4 * 1024);
			DataOutputStream dos = new DataOutputStream(bos);

			Header.reserve(raf, 4);

			Header header = new Header();

			long offset = raf.getFilePointer();
			header.put(Header.Type.DATA, offset);

			// write the datablob
			while (it.hasNext() && raf.getFilePointer() < maxdatasize) {
				Document.Entry e = it.next();
				index.put(e.id, (int) (raf.getFilePointer() - offset));

				// serialize document
				e.doc.accept(ParamDocumentSerializerVisitor.VISITOR, dos);
				raf.write(bos.toByteArray());
				bos.reset();
			}

			header.put(Header.Type.UUID_INDEX, raf.getFilePointer());

			BloomFilter bloom = BloomFilter.optimal(index.size(), 5);

			// write the index
			for (Map.Entry<UUID, Integer> e : index.entrySet()) {
				if (bloom != null)
					new Bloom<UUID>(e.getKey(), BloomHasher.UUID).putIn(bloom);

				dos.writeLong(e.getKey().getMostSignificantBits());
				dos.writeLong(e.getKey().getLeastSignificantBits());
				dos.writeInt(e.getValue());
			}
			raf.write(bos.toByteArray());
			bos.reset();

			header.put(Header.Type.BLOOM, raf.getFilePointer());
			if (bloom != null)
				bloom.write(raf);
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

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close()
	{
		try {
			this.raf.close();
		} catch (IOException ball) {
			//TODO log exception for closing SSTable, not that you can do much about it but it would be nice to mesure the number of failures here
		}
	}
}
