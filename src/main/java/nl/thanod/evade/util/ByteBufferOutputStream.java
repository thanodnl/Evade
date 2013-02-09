package nl.thanod.evade.util;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream
{

	public final ByteBuffer use;

	public ByteBufferOutputStream(ByteBuffer use)
	{
		this.use = use;
	}

	@Override
	public void write(int b)
	{
		this.use.put((byte) b);
	}

	@Override
	public void write(byte[] buffer)
	{
		this.use.put(buffer);
	}

	@Override
	public void write(byte[] buffer, int offset, int len)
	{
		this.use.put(buffer, offset, len);
	}
}
