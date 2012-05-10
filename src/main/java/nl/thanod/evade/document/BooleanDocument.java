/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public class BooleanDocument extends Document
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
	 * nl.thanod.evade.document.Document#compareValue(nl.thanod.evade.document
	 * .Document)
	 */
	@Override
	protected int compareValue(Document other)
	{
		BooleanDocument that = (BooleanDocument) other;
		return Boolean.compare(this.value, that.value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#modify(nl.thanod.evade.document.modifiers
	 * .Modifier)
	 */
	@Override
	public BooleanDocument modify(Modifier m)
	{
		if (m == null)
			return this;
		return new BooleanDocument(this.version, m.modify(this.value));
	}

}
