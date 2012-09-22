/**
 * 
 */
package nl.thanod.evade.util;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
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
		try {
			return this.buffer.get() & 0xFF;
		} catch (BufferUnderflowException ball) {
			return -1;
		}
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws EOFException
	{
		try {
			this.buffer.get(buffer, offset, length);
			return length;
		} catch (BufferUnderflowException ball) {
			return -1;
		}
	}

}
