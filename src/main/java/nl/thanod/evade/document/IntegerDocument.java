/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class IntegerDocument extends ValueDocument
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
	 * nl.thanod.evade.document.ValueDocument#compareValue(nl.thanod.evade.document
	 * .ValueDocument)
	 */
	@Override
	protected int compareValue(ValueDocument other)
	{
		IntegerDocument that = (IntegerDocument) other;
		if (this.value == that.value)
			return 0;
		if (this.value > that.value)
			return -1;
		return 1;
	}

	@Override
	public String toString()
	{
		return super.toString() + this.value;
	}

}
