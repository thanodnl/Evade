/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author nilsdijk
 */
public class LimitedInputStream extends InputStream
{

	private final InputStream in;
	private long limit;

	public LimitedInputStream(InputStream in, long limit)
	{
		this.in = in;
		this.limit = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		if (--this.limit < 0)
			return -1;
		return this.in.read();
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException
	{
		if (this.limit <= 0)
			return -1;
		len = (int)Math.min(len, this.limit);
		len = this.in.read(buffer, off, len);
		this.limit -= len;
		return len;
	}

	@Override
	public void close() throws IOException
	{
		this.in.close();
	}

	@Override
	public int available()
	{
		return (int)Math.max(this.limit, 0);
	}
}
