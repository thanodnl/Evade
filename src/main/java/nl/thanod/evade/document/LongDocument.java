/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class LongDocument extends ValueDocument
{

	public final long value;

	/**
	 * @param version
	 * @param type
	 */
	public LongDocument(long version, long value)
	{
		super(version, Type.LONG);
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
		LongDocument that = (LongDocument) other;
		if (this.value == that.value)
			return 0;
		if (this.value > that.value)
			return -1;
		return 1;
	}

	@Override
	protected String valueString()
	{
		return Long.toString(this.value);
	}

}
