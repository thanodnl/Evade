/**
 * 
 */
package nl.thanod.evade.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author nilsdijk
 */
public class ByteBufferInputStream extends InputStream
{

	private final ByteBuffer buffer;

	public ByteBufferInputStream(ByteBuffer buffer, int pos)
	{
		this.buffer = buffer.duplicate();
		this.buffer.position(pos);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		return this.buffer.get() & 0xFF;
	}
	
	@Override
	public int read(byte[] b){
		this.buffer.get(b);
		return b.length;
	}

	@Override
	public int read(byte[] b, int off, int len)
	{
		this.buffer.get(b, off, len);
		return len;
	}

}
