/**
 * 
 */
package nl.thanod.evade.document.visitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public class DocumentSerializerVisitor extends DocumentVisitor<Void, DataOutput>
{

	public static final Charset STRING_ENCODING = Charset.forName("UTF8");

	public static final DocumentSerializerVisitor VERSIONED = new DocumentSerializerVisitor(true);
	public static final DocumentSerializerVisitor NON_VERSIONED = new DocumentSerializerVisitor(false);

	public final boolean versioned;

	private DocumentSerializerVisitor(boolean versioned)
	{
		this.versioned = versioned;
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
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
			// write version
			if (versioned)
				out.writeLong(doc.version);
			out.writeFloat(doc.value);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	@Override
	public Void visit(TupleDocument doc, DataOutput out)
	{
		try {
			// general document information
			out.write(doc.type.code);
			// write version
			if (versioned)
				out.writeLong(doc.version);
			out.writeInt(doc.size());
			for (int i = 0; i < doc.size(); i++)
				doc.get(i).accept(DocumentSerializerVisitor.NON_VERSIONED, out);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
		return null;
	}

	public Document deserialize(DataInput stream)
	{
		try {
			int code = stream.readByte() & 0xFF;
			Document.Type type = Document.Type.getByCode(code);
			long version = 0;
			switch (type) {
				case NULL:
					if (this.versioned)
						version = stream.readLong();
					return new NullDocument(version);
				case STRING:
					if (this.versioned)
						version = stream.readLong();
					return new StringDocument(version, stream.readUTF());
				case BOOLEAN:
					if (this.versioned)
						version = stream.readLong();
					return new BooleanDocument(version, stream.readByte() != 0);
				case INTEGER:
					if (this.versioned)
						version = stream.readLong();
					return new IntegerDocument(version, stream.readInt());
				case LONG:
					if (this.versioned)
						version = stream.readLong();
					return new LongDocument(version, stream.readLong());
				case UUID:
					if (this.versioned)
						version = stream.readLong();
					return new UUIDDocument(version, new UUID(stream.readLong(), stream.readLong()));
				case DOUBLE:
					if (this.versioned)
						version = stream.readLong();
					return new DoubleDocument(version, stream.readDouble());
				case FLOAT:
					if (this.versioned)
						version = stream.readLong();
					return new FloatDocument(version, stream.readFloat());
				case TUPLE:
					if (this.versioned)
						version = stream.readLong();
					ValueDocument[] docs = new ValueDocument[stream.readInt()];
					for (int i = 0; i < docs.length; i++)
						docs[i] = (ValueDocument) DocumentSerializerVisitor.NON_VERSIONED.deserialize(stream);
					return new TupleDocument(version, docs);
				case DICT:
					Map<String, Document> map = new HashMap<String, Document>();
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
	public void move(DataInput in, DataOutput out)
	{
		int temp;
		try {
			int code = in.readByte() & 0xFF;

			// header
			out.writeByte(code);

			Document.Type type = Document.Type.getByCode(code);
			switch (type) {
				case NULL:
					if (this.versioned)
						out.writeLong(in.readLong()); // verion
					break; // no data
				case STRING:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeUTF(in.readUTF()); // data
					break;
				case BOOLEAN:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeByte(in.readByte()); // data
					break;
				case INTEGER:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeInt(in.readInt()); // data
					break;
				case LONG:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeLong(in.readLong()); // data
					break;
				case UUID:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeLong(in.readLong()); // data 128-bit
					out.writeLong(in.readLong());
					break;
				case DOUBLE:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeDouble(in.readDouble()); // data
					break;
				case FLOAT:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					out.writeFloat(in.readFloat()); // data
					break;
				case TUPLE:
					if (this.versioned)
						out.writeLong(in.readLong()); // version
					int size;
					out.writeInt(size = in.readInt()); // number of items in tuple
					while (size > 0) {
						DocumentSerializerVisitor.NON_VERSIONED.move(in, out);
						--size;
					}
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
