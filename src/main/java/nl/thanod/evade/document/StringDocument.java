/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class StringDocument extends ValueDocument
{
	public final String value;

	/**
	 * @param string
	 */
	public StringDocument(String value)
	{
		this(0, value);
	}

	/**
	 * @param version
	 * @param type
	 */
	public StringDocument(long version, String value)
	{
		super(version, Type.STRING);
		this.value = value;
	}

	@Override
	protected String valueString()
	{
		return '"' + this.value + '"';
	}

	@Override
	public boolean equals(Object that)
	{
		if (!super.equals(that))
			return false;
		if (!(that instanceof StringDocument))
			return false;
		StringDocument thats = (StringDocument) that;
		return this.value.equals(thats.value);
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
		StringDocument sd = (StringDocument) other;
		return this.value.compareTo(sd.value);
	}
}
