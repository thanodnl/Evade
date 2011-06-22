package nl.thanod.evade.store.local;

import java.nio.ByteBuffer;

import nl.thanod.evade.bytes.ByteSequence;

public class Serializer {
	public static ByteBuffer serialize(ColumnEntry ce) {
		ByteBuffer buffer = ByteBuffer.allocate(length(ce));
		serialize(buffer, ce);
		buffer.flip();
		return buffer;
	}

	public static void serialize(ByteBuffer buffer, ColumnEntry ce) {
		buffer.putLong(ce.getTimestamp()); // the time the entry is edited
		serialize(buffer, ce.getKey());
		serialize(buffer, ce.getValue());
	}

	public static void serialize(ByteBuffer buffer, ByteSequence bytes) {
		if (bytes == null) { // if there isn't a value
			buffer.putInt(-1); // write down a length of -1
		} else {
			buffer.putInt(bytes.length()); // the length of the value
			bytes.getBytes(buffer); // the bytes of the value 
		}
	}

	public static int length(ByteSequence bytes) {
		return 4 + (bytes == null ? 0 : bytes.length());
	}

	public static int length(ColumnEntry ce) {
		return 8 + length(ce.getKey()) + length(ce.getValue());
	}
}
