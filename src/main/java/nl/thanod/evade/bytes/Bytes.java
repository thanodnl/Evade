package nl.thanod.evade.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Bytes extends AbstractByteSequence {

	public static final Bytes EMPTY = new Bytes(new byte[0], 0, 0, false);

	private final byte[] bs;
	private final int length;
	private final int offset;

	public Bytes(byte[] bs) {
		this(bs, 0, bs.length, true);
	}

	public Bytes(byte[] bs, int offset, int length) {
		this(bs, offset, length, true);
	}

	public Bytes(ByteBuffer buffer) {
		this(buffer.array(), buffer.position(), buffer.remaining());
	}

	protected Bytes(byte[] bs, int offset, int length, boolean copy) {
		if (copy) {
			this.bs = Arrays.copyOfRange(bs, offset, length);
			this.offset = 0;
		} else {
			this.bs = bs;
			this.offset = offset;
		}
		this.length = length;
	}

	@Override
	public byte byteAt(int index) {
		return this.bs[ix(index)];
	}

	@Override
	public byte[] getBytes() {
		return Arrays.copyOfRange(this.bs, ix(0), length());
	}

	@Override
	public void getBytes(byte[] bytes) {
		this.getBytes(bytes, 0);
	}

	@Override
	public void getBytes(byte[] bytes, int offset) {
		System.arraycopy(this.bs, ix(0), bytes, offset, length());
	}

	@Override
	public void getBytes(ByteBuffer buffer) {
		buffer.put(this.bs, ix(0), length());
	}

	@Override
	public int length() {
		return this.length;
	}

	@Override
	public Bytes subsequence(int start) {
		if (start == length())
			return Bytes.EMPTY;
		return new Bytes(this.bs, ix(start), length() - start, false);
	}

	@Override
	public Bytes subsequence(int start, int length) {
		if (length == 0 || start == length())
			return Bytes.EMPTY;
		ix(length - 1);
		return new Bytes(this.bs, ix(start), length, false);
	}

	@Override
	public Bytes toBytes() {
		return this;
	}

	private int ix(int index) {
		if (index < 0 || index >= this.length)
			throw new IndexOutOfBoundsException();
		return this.offset + index;
	}

}
