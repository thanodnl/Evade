/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class BooleanDocument extends ValueDocument
{
	public final boolean value;

	public BooleanDocument(long version, boolean value)
	{
		super(version, Type.BOOLEAN);
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
		BooleanDocument that = (BooleanDocument) other;
		if (this.value == that.value)
			return 0;
		if (this.value)
			return -1;
		return 1;
	}

	@Override
	protected String valueString()
	{
		return Boolean.toString(this.value);
	}

}
