/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class DoubleDocument extends ValueDocument
{

	public final double value;

	/**
	 * @param version
	 * @param type
	 */
	public DoubleDocument(long version, double value)
	{
		super(version, Type.DOUBLE);
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
		DoubleDocument that = (DoubleDocument) other;
		return Double.compare(this.value, that.value);
	}

	@Override
	public String toString()
	{
		return super.toString() + this.value;
	}
}
