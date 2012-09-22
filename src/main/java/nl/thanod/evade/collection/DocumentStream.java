/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author nilsdijk
 */
public class DocumentStream extends Generator<Document.Entry>
{
	private final DataInput in;

	public DocumentStream(DataInput in)
	{
		this.in = in;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.util.iterator.Generator#generate()
	 */
	@Override
	protected Entry generate() throws NoSuchElementException
	{
		try {
			UUID id = new UUID(this.in.readLong(), this.in.readLong());
			Document doc = DocumentSerializerVisitor.deserialize(this.in);
			return new Document.Entry(id, doc);
		} catch (IOException ball) {
			throw new NoSuchElementException();
		}
	}
}
