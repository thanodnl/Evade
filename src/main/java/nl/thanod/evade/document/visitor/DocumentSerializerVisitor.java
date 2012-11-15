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
			if (this.versioned)
				return unsafeDeserializeVersioned(stream);
			else
				return unsafeDeserializeNonversioned(stream);
		} catch (IOException ball) {
			throw new RuntimeException(ball);
		}
	}

	private static Document unsafeDeserializeVersioned(DataInput stream) throws IOException, ProtocolException
	{
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
			case TUPLE:
				long version = stream.readLong();
				ValueDocument[] docs = new ValueDocument[stream.readInt()];
				for (int i = 0; i < docs.length; i++)
					docs[i] = (ValueDocument) unsafeDeserializeNonversioned(stream);
				return new TupleDocument(version, docs);
			case DICT:
				Map<String, Document> map = new HashMap<String, Document>();
				while (stream.readByte() != 0)
					map.put(stream.readUTF(), unsafeDeserializeVersioned(stream));
				return new DictDocument(map, stream.readLong(), false);
			default:
				throw new ProtocolException("unknown type: " + type + " (0x" + Integer.toHexString(code) + ")");
		}
	}

	private static Document unsafeDeserializeNonversioned(DataInput stream) throws IOException, ProtocolException
	{
		int code = stream.readByte() & 0xFF;
		Document.Type type = Document.Type.getByCode(code);
		switch (type) {
			case NULL:
				return new NullDocument(0);
			case STRING:
				return new StringDocument(0, stream.readUTF());
			case BOOLEAN:
				return new BooleanDocument(0, stream.readByte() != 0);
			case INTEGER:
				return new IntegerDocument(0, stream.readInt());
			case LONG:
				return new LongDocument(0, stream.readLong());
			case UUID:
				return new UUIDDocument(0, new UUID(stream.readLong(), stream.readLong()));
			case DOUBLE:
				return new DoubleDocument(0, stream.readDouble());
			case FLOAT:
				return new FloatDocument(0, stream.readFloat());
			case TUPLE:
				ValueDocument[] docs = new ValueDocument[stream.readInt()];
				for (int i = 0; i < docs.length; i++)
					docs[i] = (ValueDocument) unsafeDeserializeNonversioned(stream);
				return new TupleDocument(0, docs);
			case DICT:
				Map<String, Document> map = new HashMap<String, Document>();
				while (stream.readByte() != 0)
					map.put(stream.readUTF(), unsafeDeserializeNonversioned(stream));
				return new DictDocument(map, stream.readLong(), false);
			default:
				throw new ProtocolException("unknown type: " + type + " (0x" + Integer.toHexString(code) + ")");
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
