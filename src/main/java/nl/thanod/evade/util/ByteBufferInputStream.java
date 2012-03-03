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
	private int pos;

	public ByteBufferInputStream(ByteBuffer buffer, int pos)
	{
		this.buffer = buffer;
		this.pos = pos;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		return this.buffer.get(this.pos++) & 0xFF;
	}

}
