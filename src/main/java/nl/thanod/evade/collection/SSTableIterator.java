/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.UUID;

import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author nilsdijk
 */
public class SSTableIterator extends Generator<Entry> implements Closeable
{

	private final DataInputStream din;
	private LimitedInputStream lin;

	/**
	 * @param file
	 * @param position
	 * @param length
	 * @throws IOException
	 */
	public SSTableIterator(File file, int position, int length) throws IOException
	{
		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file), 1024 * 1024);
		buf.skip(position);
		this.lin = new LimitedInputStream(buf, length);

		this.din = new DataInputStream(this.lin);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.util.iterator.Generator#generate()
	 */
	@Override
	protected Entry generate() throws NoSuchElementException
	{
		if (this.lin.available() <= 0)
			throw new NoSuchElementException();
		UUID id;
		try {
			id = new UUID(this.din.readLong(), this.din.readLong());
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
