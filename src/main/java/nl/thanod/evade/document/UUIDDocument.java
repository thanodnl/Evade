/**
 * 
 */
package nl.thanod.evade.document;

import java.util.UUID;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class UUIDDocument extends ValueDocument
{

	public final UUID value;

	/**
	 * @param version
	 * @param type
	 */
	public UUIDDocument(long version, UUID value)
	{
		super(version, Type.UUID);
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor
	 * .ParameterizedDocumentVisitor, java.lang.Object)
	 */
	@Override
	public <OUT, IN> OUT accept(DocumentVisitor<OUT, IN> visitor, IN data)
	{
		return visitor.visit(this, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#compareValue(nl.thanod.evade.document
	 * .Document)
	 */
	@Override
	protected int compareValue(ValueDocument other)
	{
		UUIDDocument that = (UUIDDocument) other;
		return this.value.compareTo(that.value);
	}

	@Override
	public String toString()
	{
		return super.toString() + "(UUID)" + this.value;
	}

}
