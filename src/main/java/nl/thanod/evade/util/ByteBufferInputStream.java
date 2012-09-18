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

	public ByteBufferInputStream(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read()
	{
		return this.buffer.get() & 0xFF;
	}

	@Override
	public int read(byte[] buffer, int offset, int length)
	{
		this.buffer.get(buffer, offset, length);
		return length;
	}

}
