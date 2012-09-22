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
import nl.thanod.evade.store.Header;
import nl.thanod.evade.store.bloom.Bloom;
import nl.thanod.evade.store.bloom.BloomFilter;
import nl.thanod.evade.store.bloom.BloomHasher;
import nl.thanod.evade.util.ByteBufferDataInput;
import nl.thanod.evade.util.ByteBufferInputStream;
import nl.thanod.evade.util.iterator.Generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.compress.lzf.LZFEncoder;
import com.ning.compress.lzf.LZFInputStream;

/**
 * @author nilsdijk
 */
public class SSTable extends Collection implements Closeable
{
	public static final int FILE_HEADER = 0xEFADE000;
	public static final int FILE_VERSION = 2;

	private static final Logger log = LoggerFactory.getLogger(SSTable.class);

	private static final int DEF_DATA_SIZE = 256 * 1024 * 1024;
	private static final int MAX_DATA_SIZE = 512 * 1024 * 1024;

	public final File file;
	public final UUID max;
	public final UUID min;
	public final Header header;
	public final int version;

	protected final UUIDPositionIndex index;

	private final BloomFilter bloom;
	private final ByteBuffer datamap;
	private final RandomAccessFile raf;

	public static interface Visitor
	{
		void visit(SSTable table);
	}

	public SSTable(File file) throws IOException
	{
		this.file = file;

		this.raf = new RandomAccessFile(file, "r");

		int fileheader = this.raf.readInt(); // read the header
		Header header;
		if ((fileheader & SSTable.FILE_HEADER) != SSTable.FILE_HEADER) {
			log.warn("The file {} is probably not an evade file. It is loaded as if it were a development file", file);
			raf.seek(0);
			header = Header.read(raf);
			this.version = 0;
		} else {
			this.version = fileheader ^ SSTable.FILE_HEADER;
			log.info("Opening {} as version {}", file, this.version);
			switch (this.version) {
				case 0:
				case 1:
					header = Header.readFromEnd(raf);
					break;
				case 2:
					header = Header.read2(raf);
					break;
				default:
					log.error("Invalid version (version:{})", this.version);
					throw new IOException("Invalid file version");
			}
		}
		this.header = header;

		// the index is located at start index until the end of the file minus the 4 bytes
		// indicating the start of the index
		this.index = new UUIDPositionIndex(header.map(raf, Header.Type.UUID_INDEX));

		this.min = this.index.get(0).id;
		this.max = this.index.get(this.index.count() - 1).id;

		// the beginning of the file to the start of the index is the datapart of the file
		this.datamap = header.map(raf, Header.Type.DATA);
		this.bloom = BloomFilter.fromBuffer(header.map(raf, Header.Type.BLOOM));
	}

	public void accept(Visitor visitor)
	{
		visitor.visit(this);
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

	public boolean earlySkip(Bloom<?> bloom)
	{
		if (this.bloom == null)
			return false;
		return !bloom.containedBy(this.bloom);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSTable other = (SSTable) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id)
	{
		if (this.min.compareTo(id) > 0 || this.max.compareTo(id) < 0)
			return null;
		Pointer p = this.index.before(id);
		if (!p.id.equals(id))
			return null;
		return get(p);
	}

	public Document get(UUIDPositionIndex.Pointer pointer)
	{
		DataInput di;
		ByteBuffer buffer = this.datamap.duplicate();
		buffer.position(pointer.pos);
		if (this.header.get(Header.Type.DATA).flags.contains(Header.Flags.LZF)) {
			try {
				di = new DataInputStream(new LZFInputStream(new ByteBufferInputStream(buffer)));
			} catch (IOException ball) {
				log.error("The data in {} is LZF encoded, but the decoder did not initialize", this.file);
				return null;
			}
		} else {
			di = new ByteBufferDataInput(buffer);
		}

		if (this.header.version >= 2 || this.version == 1) {
			UUID id = null;
			try {
				id = new UUID(di.readLong(), di.readLong());
				if (!id.equals(pointer.id)) {
					log.error("The pointer for {} did not point to the correct entry in {}", pointer.id, this.file);
					return null;
				}
			} catch (IOException ball) {
				ball.printStackTrace();
			}
		}
		return DocumentSerializerVisitor.deserialize(di);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		if (this.version == 1 || this.header.version >= 2) {
			Header.Entry data = this.header.get(Header.Type.DATA);

			InputStream in = null;

			// open file
			try {
				in = new FileInputStream(this.file);
				in.skip(data.start);
				in = new LimitedInputStream(in, data.next().start - data.start);
			} catch (IOException ball) {
			}
			
			// fallback on memory map
			if (in == null){
				final ByteBuffer buffer = this.datamap.duplicate();
				buffer.position(0);
				in = new ByteBufferInputStream(buffer);
			}

			// apply lzf when compressed
			if (data.flags.contains(Header.Flags.LZF)) {
				try {
					in = new LZFInputStream(in, true);
				} catch (IOException ball) {
				}
			}
			
			return new DocumentStream(new DataInputStream(in));
		}

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

	@Override
	protected void finalize()
	{
		this.close();
	}

	public static List<File> save(File directory, String name, Iterable<Document.Entry> data) throws FileNotFoundException, IOException
	{
		return SSTable.save(directory, name, data, SSTable.DEF_DATA_SIZE); // save in blobs of 512 megabyte
	}

	public static List<File> save(File datadir, String name, Iterable<Document.Entry> data, int maxdatasize) throws FileNotFoundException, IOException
	{
		if (!datadir.exists()) {
			if (!datadir.mkdirs()) {
				throw new IOException("Could not make parent directories to " + datadir);
			}
		}

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

			SortedMap<UUID, Integer> index = new TreeMap<UUID, Integer>();

			RandomAccessFile raf = new RandomAccessFile(f, "rw");

			ByteArrayOutputStream bos = new ByteArrayOutputStream(4 * 1024);
			DataOutputStream dos = new DataOutputStream(bos);

			raf.writeInt(SSTable.FILE_HEADER | SSTable.FILE_VERSION);

			Header header = new Header(2);

			long offset = raf.getFilePointer();
			header.put(Header.Type.DATA, offset, Header.Flags.LZF);

			header.uncompressed = 0;
			header.compressed = 0;

			// write the datablob
			while (it.hasNext() && raf.getFilePointer() < maxdatasize) {
				Document.Entry e = it.next();
				index.put(e.id, (int) (raf.getFilePointer() - offset));

				// write the document id for linear iterating
				dos.writeLong(e.id.getMostSignificantBits());
				dos.writeLong(e.id.getLeastSignificantBits());

				// serialize document
				e.doc.accept(DocumentSerializerVisitor.VISITOR, dos);

				// compress the data
				byte[] orig = bos.toByteArray();
				byte[] comp = LZFEncoder.encode(orig);

				// count the bytes
				header.uncompressed += orig.length;
				header.compressed += comp.length;

				// write the compressed data to the file
				raf.write(comp);
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

				if (bos.size() > 10 * 1024) {
					raf.write(bos.toByteArray());
					bos.reset();
				}
			}
			raf.write(bos.toByteArray());
			bos.reset();

			if (bloom != null) {
				header.put(Header.Type.BLOOM, raf.getFilePointer());
				bloom.write(raf);
			}
			header.put(Header.Type.EOF, raf.getFilePointer());

			// the offset where the index starts
			header.write2(raf);

			// close the file
			raf.close();
			files.add(f);
		}
		return files;
	}

	/**
	 * 
	 */
	public void remove()
	{
		this.close();

		// delete the file
		if (!this.file.delete()) {
			log.info("Can not delete {}, the file is marked for removal on exit", this.file);
			this.file.deleteOnExit();
		}
	}
}
