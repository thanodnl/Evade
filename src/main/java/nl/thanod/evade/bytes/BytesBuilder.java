package nl.thanod.evade.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BytesBuilder extends AbstractByteSequence {

	private byte[] bs;
	private int index;

	public BytesBuilder(int initialSize) {
		this.bs = new byte[initialSize];
	}

	@Override
	public byte byteAt(int index) {
		if (index >= this.index)
			throw new IndexOutOfBoundsException();
		return this.bs[index];
	}

	@Override
	public byte[] getBytes() {
		return Arrays.copyOf(this.bs, this.index);
	}

	@Override
	public void getBytes(byte[] bytes) {
		System.arraycopy(this.bs, 0, bytes, 0, this.index);
	}

	@Override
	public void getBytes(byte[] bytes, int offset) {
		System.arraycopy(this.bs, 0, bytes, offset, this.index);
	}

	@Override
	public void getBytes(ByteBuffer buffer) {
		buffer.put(this.bs, 0, this.index);
	}

	@Override
	public int length() {
		return this.index;
	}

	@Override
	public Bytes subsequence(int start) {
		if (start < 0)
			throw new IndexOutOfBoundsException();
		return new Bytes(this.bs, start, this.index - start, false);
	}

	@Override
	public Bytes subsequence(int start, int length) {
		if (start < 0 || length < 0 || start + length > this.index)
			throw new IndexOutOfBoundsException();
		return new Bytes(this.bs, start, length, false);
	}

	@Override
	public Bytes toBytes() {
		return new Bytes(this.bs, 0, this.index, false);
	}

	public BytesBuilder append(ByteSequence bytes) {
		ensureSpace(bytes.length());
		bytes.getBytes(this.bs, this.index);
		this.index += bytes.length();
		return this;
	}

	public BytesBuilder append(byte b) {
		ensureSpace(1);
		this.bs[this.index++] = b;
		return this;
	}

	public BytesBuilder append(byte[] bytes) {
		ensureSpace(bytes.length);
		System.arraycopy(bytes, 0, this.bs, index, bytes.length);
		this.index += bytes.length;
		return this;
	}

	public BytesBuilder appendShort(short s) {
		ensureSpace(2);
		this.bs[this.index++] = (byte) (s >> 8 & 0xFF);
		this.bs[this.index++] = (byte) (s & 0xFF);
		return this;
	}

	public BytesBuilder appendInt(int i) {
		ensureSpace(4);
		this.bs[this.index++] = (byte) (i >> 24 & 0xFF);
		this.bs[this.index++] = (byte) (i >> 16 & 0xFF);
		this.bs[this.index++] = (byte) (i >> 8 & 0xFF);
		this.bs[this.index++] = (byte) (i & 0xFF);
		return this;
	}

	public BytesBuilder appendLong(long l) {
		ensureSpace(8);
		this.bs[this.index++] = (byte) (l >> 56 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 48 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 40 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 32 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 24 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 16 & 0xFF);
		this.bs[this.index++] = (byte) (l >> 8 & 0xFF);
		this.bs[this.index++] = (byte) (l & 0xFF);
		return this;
	}

	public BytesBuilder appendFloat(float f) {
		return this.appendInt(Float.floatToIntBits(f));
	}

	public BytesBuilder appendDouble(double d) {
		return this.appendLong(Double.doubleToLongBits(d));
	}

	public void compact() {
		this.bs = Arrays.copyOf(this.bs, this.index);
	}

	private void ensureSpace(int space) {
		if (this.index + space > this.bs.length)
			this.bs = Arrays.copyOf(this.bs, Math.max(this.bs.length << 1, this.index + space));
	}
}
