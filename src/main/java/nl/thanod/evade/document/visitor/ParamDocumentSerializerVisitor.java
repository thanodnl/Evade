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
import java.util.UUID;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public class ParamDocumentSerializerVisitor extends ParameterizedDocumentVisitor<Void, DataOutput>
{

	public static final Charset STRING_ENCODING = Charset.forName("UTF8");

	public static final ParamDocumentSerializerVisitor VISITOR = new ParamDocumentSerializerVisitor();

	private ParamDocumentSerializerVisitor()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.StringDocument)
	 */
	@Override
	public Void visit(StringDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);

			// write the length of the string followed by the contents
			out.writeUTF(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.NullDocument)
	 */
	@Override
	public Void visit(NullDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DictDocument)
	 */
	@Override
	public Void visit(DictDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);

			for (Map.Entry<String, Document> e : doc.entrySet()) {
				out.write(0xFF); // entry following

				// write the key
				out.writeUTF(e.getKey());
				// write the value
				e.getValue().accept(this, out);
			}
			out.write(0x00); // no entry following anymore

			// the moment this dict was cleared
			out.writeLong(doc.clearedOn);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.BooleanDocument)
	 */
	@Override
	public Void visit(BooleanDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.write(doc.value ? 0xFF : 0x00);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.IntegerDocument)
	 */
	@Override
	public Void visit(IntegerDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.writeInt(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.LongDocument)
	 */
	@Override
	public Void visit(LongDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.writeLong(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.UUIDDocument)
	 */
	@Override
	public Void visit(UUIDDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.writeLong(doc.value.getMostSignificantBits());
			out.writeLong(doc.value.getLeastSignificantBits());
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DoubleDocument)
	 */
	@Override
	public Void visit(DoubleDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.writeDouble(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.FloatDocument)
	 */
	@Override
	public Void visit(FloatDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			out.writeLong(doc.version);
			out.writeFloat(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
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
				case BOOLEAN:
					return new BooleanDocument(stream.readLong(), stream.readByte() != 0);
				case INTEGER:
					return new IntegerDocument(stream.readLong(), stream.readInt());
				case LONG:
					return new LongDocument(stream.readLong(), stream.readLong());
				case UUID:
					return new UUIDDocument(stream.readLong(), new UUID(stream.readLong(), stream.readLong()));
				case DOUBLE:
					return new DoubleDocument(stream.readLong(), stream.readDouble());
				case FLOAT:
					return new FloatDocument(stream.readLong(), stream.readFloat());
				case DICT:
					Map<String, Document> map = new TreeMap<String, Document>();
					while (stream.readByte() != 0)
						map.put(stream.readUTF(), deserialize(stream));
					return new DictDocument(map, stream.readLong(), false);
				default:
					throw new ProtocolException("unknown type: " + type + " (0x" + Integer.toHexString(code) + ")");
			}
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}

	/**
	 * Move a document from one source to another without fully deserializing
	 * @param in
	 * @param out
	 */
	public static void move(DataInput in, DataOutput out)
	{
		int temp;
		try {
			int code = in.readByte() & 0xFF;

			// header
			out.writeByte(code);

			Document.Type type = Document.Type.getByCode(code);
			switch (type) {
				case NULL:
					out.writeLong(in.readLong()); // verion
					break; // no data
				case STRING:
					out.writeLong(in.readLong()); // version
					out.writeUTF(in.readUTF()); // data
					break;
				case BOOLEAN:
					out.writeLong(in.readLong()); // version
					out.writeByte(in.readByte()); // data
					break;
				case INTEGER:
					out.writeLong(in.readLong()); // version
					out.writeInt(in.readInt()); // data
					break;
				case LONG:
					out.writeLong(in.readLong()); // version
					out.writeLong(in.readLong()); // data
					break;
				case UUID:
					out.writeLong(in.readLong()); // version
					out.writeLong(in.readLong()); // data 128-bit
					out.writeLong(in.readLong());
					break;
				case DOUBLE:
					out.writeLong(in.readLong()); // version
					out.writeDouble(in.readDouble()); // data
					break;
				case FLOAT:
					out.writeLong(in.readLong()); // version
					out.writeFloat(in.readFloat()); // data
					break;
				case DICT:
					// data
					while ((temp = in.readByte()) != 0) {
						out.writeByte(temp);
						out.writeUTF(in.readUTF());
						move(in, out);
					}
					out.writeByte(temp); // end of list
					out.writeLong(in.readLong()); // the time the list was cleared
					break;
				default:
					throw new ProtocolException("unknown type: " + type + " (0x" + Integer.toHexString(code) + ")");
			}
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}
}
