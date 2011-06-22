package nl.thanod.evade.bytes;

import java.nio.ByteBuffer;
import java.util.Comparator;

public interface ByteSequence extends Comparable<ByteSequence> {
	public static final Comparator<ByteSequence> COMPARATOR = new Comparator<ByteSequence>() {

		@Override
		public int compare(ByteSequence o1, ByteSequence o2) {
			int len1 = o1 == null ? 0 : o1.length();
			int len2 = o2 == null ? 0 : o2.length();
			int len = Math.min(len1, len2);
			for (int i = 0; i < len; i++)
				if (o1.byteAt(i) != o2.byteAt(i))
					return o1.byteAt(i) - o2.byteAt(i);
			return len1 - len2;
		}
	};

	byte byteAt(int index);

	byte[] getBytes();

	void getBytes(byte[] bytes);

	void getBytes(byte[] bytes, int offset);

	void getBytes(ByteBuffer buffer);

	int length();

	ByteSequence subsequence(int start);

	ByteSequence subsequence(int start, int length);

	Bytes toBytes();
}
