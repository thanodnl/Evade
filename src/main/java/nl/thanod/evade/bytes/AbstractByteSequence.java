package nl.thanod.evade.bytes;

public abstract class AbstractByteSequence implements ByteSequence {

	private static final String HEX = "0123456789ABCDEF";

	public AbstractByteSequence() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2 * length() + 2);
		sb.append("0x");
		for (int i = 0; i < length(); i++) {
			byte b = byteAt(i);
			sb.append(HEX.charAt(b >> 4 & 0x0F));
			sb.append(HEX.charAt(b & 0x0F));
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ByteSequence))
			return false;
		ByteSequence that = (ByteSequence) o;
		if (this.length() != that.length())
			return false;
		for (int i = 0; i < length(); i++)
			if (this.byteAt(i) != that.byteAt(i))
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (int i = 0; i < length(); i++)
			result = prime * result + byteAt(i);
		return result;
	}

	@Override
	public int compareTo(ByteSequence that) {
		return ByteSequence.COMPARATOR.compare(this, that);
	}

}