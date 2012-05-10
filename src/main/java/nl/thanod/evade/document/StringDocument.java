/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public class StringDocument extends Document
{
	public final String value;

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
	public String toString()
	{
		return super.toString() + '"' + this.value + '"';
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
	 * nl.thanod.evade.document.Document#test(nl.thanod.evade.query.Constraint)
	 */
	@Override
	public boolean test(Constraint c)
	{
		return c.test(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#visit(nl.thanod.evade.document.visitor
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
	public <User> void accept(ParameterizedDocumentVisitor<User> visitor, User data)
	{
		visitor.visit(this, data);
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
		StringDocument sd = (StringDocument) other;
		return this.value.compareTo(sd.value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#modify(nl.thanod.evade.document.modifiers
	 * .Modifier)
	 */
	@Override
	public StringDocument modify(Modifier m)
	{
		if (m == null)
			return this;
		return new StringDocument(this.version, m.modify(this.value));
	}
}
