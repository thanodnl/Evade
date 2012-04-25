/**
 * 
 */
package nl.thanod.evade.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * @author nilsdijk
 */
public class ByteBufferDataInput implements DataInput
{
	private final ByteBuffer buffer;

	public ByteBufferDataInput(ByteBuffer buffer)
	{
		this.buffer = buffer;

	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readFully(byte[])
	 */
	@Override
	public void readFully(byte[] b) throws IOException
	{
		try {
			buffer.get(b, 0, b.length);
		} catch (BufferUnderflowException ball) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException
	{
		try {
			buffer.get(b, off, len);
		} catch (BufferUnderflowException ball) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#skipBytes(int)
	 */
	@Override
	public int skipBytes(int skip) throws IOException
	{
		buffer.position(buffer.position() + skip);
		return skip;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws IOException
	{
		return buffer.get() != 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readByte()
	 */
	@Override
	public byte readByte() throws IOException
	{
		return buffer.get();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte() throws IOException
	{
		return buffer.get() & 0xFF;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readShort()
	 */
	@Override
	public short readShort() throws IOException
	{
		return buffer.getShort();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort() throws IOException
	{
		return buffer.getShort() & 0xFFFF;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readChar()
	 */
	@Override
	public char readChar() throws IOException
	{
		return buffer.getChar();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readInt()
	 */
	@Override
	public int readInt() throws IOException
	{
		return buffer.getInt();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readLong()
	 */
	@Override
	public long readLong() throws IOException
	{
		return buffer.getLong();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readFloat()
	 */
	@Override
	public float readFloat() throws IOException
	{
		return buffer.getFloat();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readDouble()
	 */
	@Override
	public double readDouble() throws IOException
	{
		return buffer.getDouble();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readLine()
	 */
	@Override
	@Deprecated
	public String readLine() throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.DataInput#readUTF()
	 */
	@Override
	public String readUTF() throws IOException
	{
		return DataInputStream.readUTF(this);
	}

}
