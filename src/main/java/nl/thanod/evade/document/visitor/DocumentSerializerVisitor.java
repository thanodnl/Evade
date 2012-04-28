/**
 * 
 */
package nl.thanod.evade.document.visitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import nl.thanod.evade.document.DictDocument;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.StringDocument;

/**
 * @author nilsdijk
 */
public class DocumentSerializerVisitor implements DocumentVisitor
{

	public static final Charset STRING_ENCODING = Charset.forName("UTF8");

	private final DataOutput out;

	public DocumentSerializerVisitor(DataOutput out)
	{
		this.out = out;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.StringDocument)
	 */
	@Override
	public void visit(StringDocument doc)
	{
		try {
			// general document information
			this.out.write(Document.Type.STRING.code);
			this.out.writeLong(doc.version);

			// write the length of the string followed by the contents
			this.out.writeUTF(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.NullDocument)
	 */
	@Override
	public void visit(NullDocument doc)
	{
		try {
			// general document information
			this.out.write(Document.Type.NULL.code);
			this.out.writeLong(doc.version);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DictDocument)
	 */
	@Override
	public void visit(DictDocument doc)
	{
		try {
			// general document information
			this.out.write(Document.Type.DICT.code);

			for (Map.Entry<String, Document> e : doc.entrySet()) {
				this.out.write(0xFF); // entry following

				// write the key
				this.out.writeUTF(e.getKey());
				// write the value
				e.getValue().accept(this);
			}
			this.out.write(0x00); // no entry following anymore

			// the moment this dict was cleared
			this.out.writeLong(doc.clearedOn);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}

	public static Document deserialize(DataInput stream)
	{
		try {
			int code = stream.readByte() & 0xFF;
			Document.Type type = Document.Type.getByCode(code);
			switch (type) {
				case NULL:
					return new NullDocument(stream.readLong());
				case STRING:
					return new StringDocument(stream.readLong(), stream.readUTF());
				case DICT:
					Map<String, Document> map = new TreeMap<String, Document>();
					while (stream.readByte() != 0)
						map.put(stream.readUTF(), deserialize(stream));
					return new DictDocument(map, stream.readLong(), false);
				default:
					throw new ProtocolException("unknown type: " + type + " (#" + code + ")");
			}
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}
}
