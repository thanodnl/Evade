/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.ning.compress.lzf.LZFInputStream;

import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.store.Header;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author nilsdijk
 */
public class SSTableIterator extends Generator<Entry> implements Closeable
{

	private final DataInputStream din;

	/**
	 * @param file
	 * @param entry
	 * @throws IOException
	 */
	public SSTableIterator(File file, Header.Entry entry) throws IOException
	{
		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file), 1024 * 1024);
		buf.skip(entry.start);
		InputStream in = new LimitedInputStream(buf, entry.next().start - entry.start);

		if (entry.flags.contains(Header.Flags.LZF))
			in = new LZFInputStream(in);

		this.din = new DataInputStream(in);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.util.iterator.Generator#generate()
	 */
	@Override
	protected Entry generate() throws NoSuchElementException
	{
		UUID id;
		try {
			id = new UUID(this.din.readLong(), this.din.readLong());
		} catch (EOFException ball) {
			throw new NoSuchElementException();
		} catch (IOException ball) {
			throw new RuntimeException("Error while reading uuid", ball);
		}
		return new Entry(id, DocumentSerializerVisitor.deserialize(this.din));
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException
	{
		this.din.close();
	}

}
