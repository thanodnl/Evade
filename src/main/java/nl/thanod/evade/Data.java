/**
 * 
 */
package nl.thanod.evade;

import java.nio.charset.Charset;

/**
 * @author nilsdijk
 */
public class Data {
	public static byte[] encode(long n) {
		int numRelevantBits = Long.SIZE - Long.numberOfLeadingZeros(n);
		int numBytes = (numRelevantBits + 6) / 7;
		if (numBytes == 0)
			numBytes = 1;
		byte[] output = new byte[numBytes];
		for (int i = numBytes - 1; i >= 0; i--) {
			int curByte = (int) (n & 0x7F);
			if (i == (numBytes - 1))
				curByte |= 0x80;
			output[i] = (byte) curByte;
			n >>>= 7;
		}
		return output;
	}

	public static long decode(byte[] b) {
		long n = 0;
		for (int i = 0; i < b.length; i++) {
			int curByte = b[i] & 0xFF;
			n = (n << 7) | (curByte & 0x7F);
			if ((curByte & 0x80) != 0)
				break;
		}
		return n;
	}

	public static String hex(byte[] bytes) {
		String hex = "0123456789ABCDEF";
		StringBuilder sb = new StringBuilder(2 + bytes.length * 2);
		sb.append("0x");
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			sb.append(hex.charAt((b >> 4) & 0x0F));
			sb.append(hex.charAt(b & 0x0F));
		}
		return sb.toString();
	}

	public static String bits(byte[] bytes) {
		StringBuilder sb = new StringBuilder(2 + bytes.length * 8 + bytes.length - 1);
		sb.append("0b");
		for (int i = 0; i < bytes.length; i++) {
			if (i != 0)
				sb.append('_');
			byte b = bytes[i];
			sb.append(((b >> 7) & 0x01));
			sb.append(((b >> 6) & 0x01));
			sb.append(((b >> 5) & 0x01));
			sb.append(((b >> 4) & 0x01));
			sb.append(((b >> 3) & 0x01));
			sb.append(((b >> 2) & 0x01));
			sb.append(((b >> 1) & 0x01));
			sb.append(((b >> 0) & 0x01));
		}
		return sb.toString();
	}

	public static void main(String... args) {
		System.out.println(bits(encode(1337)));
		System.out.println(Charset.forName("UTF16"));
		byte[] b = "Hello World".getBytes(Charset.forName("UTF16"));
		System.out.println(hex(b));
		System.out.println(new String(b,Charset.forName("UTF16")));
	}

}
