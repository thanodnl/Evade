/**
 * 
 */
package nl.thanod.evade.document;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author nilsdijk
 */
public class DocumentSerializer
{

	public static Document deserialize(DataInputStream stream) throws IOException
	{
		long version = stream.readLong();
		int code = stream.read();
		Document.Type type = Document.Type.getByCode(code);
		switch (type) {
			case NULL:
				return new NullDocument(version);
			case DOCUMENT:
				return deserializeDocument(stream, version);
			case STRING:
				return deserializeStringValue(stream, version);
			default:
				throw new RuntimeException("No deserializer for " + type);
		}
	}

	/**
	 * @param stream
	 * @param version
	 * @return
	 * @throws IOException
	 */
	private static Document deserializeStringValue(DataInputStream stream, long version) throws IOException
	{
		byte[] bytes = new byte[16];
		int index = 0;
		int c = stream.read();
		while (c > 0) {
			if (index == bytes.length)
				bytes = Arrays.copyOf(bytes, bytes.length * 2);
			bytes[index++] = (byte) c;
			c = stream.read();
		}
		return new StringDocument(version, new String(bytes, 0, index));
	}

	/**
	 * @param stream
	 * @param version
	 * @return
	 * @throws IOException
	 */
	private static DictDocument deserializeDocument(DataInputStream stream, long version) throws IOException
	{
		int cont = stream.read();
		Map<String, Document> map = new TreeMap<String, Document>();
		while (cont == 0xFF) {
			String key = deserializeString(stream);
			Document value = deserialize(stream);
			map.put(key, value);
			cont = stream.read();
		}
		DictDocument doc = new DictDocument(map, false);
		return doc;
	}

	/**
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	private static String deserializeString(DataInputStream stream) throws IOException
	{
		byte[] bytes = new byte[16];
		int index = 0;
		int c = stream.read();
		while (c > 0) {
			if (index == bytes.length)
				bytes = Arrays.copyOf(bytes, bytes.length * 2);
			bytes[index++] = (byte) c;
			c = stream.read();
		}
		return new String(bytes, 0, index);
	}

	public static int serialize(DataOutputStream stream, Document base) throws IOException
	{
		int size = 0;
		stream.writeLong(base.version);
		size += 8;
		stream.write(base.type.code);
		size += 1;
		switch (base.type) {
			case NULL:
				return size;
			case DOCUMENT:
				return size + serializeDocument(stream, (DictDocument) base);
			case STRING:
				return size + serializeStringValue(stream, (StringDocument) base);
			default:
				throw new RuntimeException("No serializer for " + base.type);
		}
	}

	/**
	 * @param stream
	 * @param base
	 * @return
	 * @throws IOException
	 */
	private static int serializeStringValue(DataOutputStream stream, StringDocument base) throws IOException
	{
		byte[] b = base.value.getBytes(Charset.forName("UTF8"));
		stream.write(b);
		stream.write(0x00);
		return b.length + 1;
	}

	/**
	 * @param stream
	 * @param base
	 * @return
	 * @throws IOException
	 */
	private static int serializeDocument(DataOutputStream stream, DictDocument base) throws IOException
	{
		int size = 0;
		for (Map.Entry<String, Document> e : base.entrySet()) {
			stream.write(0xFF);
			size += 1;
			size += serializeString(stream, e.getKey());
			size += serialize(stream, e.getValue());
		}
		stream.write(0x00);
		size += 1;
		return size;
	}

	/**
	 * @param stream
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private static int serializeString(DataOutputStream stream, String key) throws IOException
	{
		byte[] b = key.getBytes(Charset.forName("UTF8"));
		stream.write(b);
		stream.write(0x00);
		return b.length + 1;
	}

	public static void main(String... args) throws IOException
	{
		DictDocument doc = DocumentBuilder.start(123).put("name", "Klaas Jan Pietje").tree("props").put("age", "24").put("gender", "male").make();
		System.out.println(doc);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int length = serialize(new DataOutputStream(baos), doc);

		byte[] bytes = baos.toByteArray();
		System.out.println("lenght: " + length + " actual: " + bytes.length);

		Document read = deserialize(new DataInputStream(new ByteArrayInputStream(bytes)));
		System.out.println(read);
	}
}
