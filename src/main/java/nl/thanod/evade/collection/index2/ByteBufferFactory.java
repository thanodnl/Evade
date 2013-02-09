package nl.thanod.evade.collection.index2;

import java.nio.ByteBuffer;

public class ByteBufferFactory
{

	private final boolean preferDirect;

	public ByteBufferFactory(boolean preferDirect)
	{
		this.preferDirect = preferDirect;
	}

	static class DefaultHolder
	{
		public static final ByteBufferFactory INSTANCE = new ByteBufferFactory(true);
	}

	public static ByteBufferFactory getDefault()
	{
		return DefaultHolder.INSTANCE;
	}

	public ByteBuffer create(int size)
	{
		if (this.preferDirect) {
			try {
				return ByteBuffer.allocateDirect(size);
			} catch (OutOfMemoryError ball) {
				// fall through to array backed implementation
			}
		}
		return ByteBuffer.allocate(size);
	}
}
