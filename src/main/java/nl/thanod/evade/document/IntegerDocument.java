/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor;

/**
 * @author nilsdijk
 */
public class IntegerDocument extends Document
{

	public final int value;

	public IntegerDocument(long version, int value)
	{
		super(version, Type.INTEGER);
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor
	 * .DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor
	 * .ParameterizedDocumentVisitor, java.lang.Object)
	 */
	@Override
	public <OUT,IN> OUT accept(ParameterizedDocumentVisitor<OUT,IN> visitor, IN data)
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
	protected int compareValue(Document other)
	{
		IntegerDocument that = (IntegerDocument) other;
		return Integer.compare(this.value, that.value);
	}

	@Override
	public String toString()
	{
		return super.toString() + "(int)" + this.value;
	}

}
