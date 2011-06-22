package nl.thanod.evade.store.local;

import nl.thanod.evade.bytes.ByteSequence;
import nl.thanod.evade.bytes.Bytes;

public class ColumnEntry {
	private final Bytes key;
	private Bytes value;
	private long timestamp;

	public ColumnEntry(Bytes key) {
		this.key = key;
	}

	public ColumnEntry(Bytes key, Bytes value) {
		this(key);
		this.value = value;
	}

	public ColumnEntry(Bytes key, Bytes value, long timestamp) {
		this(key, value);
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public Bytes getKey() {
		return this.key;
	}

	public Bytes getValue() {
		return this.value;
	}

	public boolean update(ColumnEntry entry) {
		if (entry == null)
			throw new NullPointerException("entry can't be null");
		if (!entry.getKey().equals(getKey()))
			throw new IllegalArgumentException("The entry name does not match this entry");
		return update(entry.getValue(), entry.getTimestamp());
	}

	public boolean update(Bytes value, long timestamp) {
		if (!shouldUpdate(value, timestamp))
			return false;
		this.value = value;
		this.timestamp = timestamp;
		return true;
	}

	private boolean shouldUpdate(Bytes value, long timestamp) {
		if (timestamp < getTimestamp())
			return false; // do not update with older values
		if (timestamp == getTimestamp())
			return ByteSequence.COMPARATOR.compare(this.value, value) > 0;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(getKey());
		sb.append(", ");
		if (getValue() != null)
			sb.append(getValue());
		sb.append(", @");
		sb.append(getTimestamp());
		sb.append(')');
		return sb.toString();
	}

	public static void main(String... args) {
		System.out.println(new Bytes(Serializer.serialize(new ColumnEntry(new Bytes("Hello".getBytes()), new Bytes("World".getBytes()),System.currentTimeMillis()))));
	}
}
