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
public class LongDocument extends Document
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
		LongDocument that = (LongDocument) other;
		return Long.compare(this.value, that.value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#modify(nl.thanod.evade.document.modifiers
	 * .Modifier)
	 */
	@Override
	public LongDocument modify(Modifier m)
	{
		if (m == null)
			return this;
		return new LongDocument(this.version, m.modify(this.value));
	}

}
