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
public class DoubleDocument extends Document
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

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.Document#test(nl.thanod.evade.query.Constraint)
	 */
	@Override
	public boolean test(Constraint c)
	{
		return c.test(this);
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor.DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.Document#compareValue(nl.thanod.evade.document.Document)
	 */
	@Override
	protected int compareValue(Document other)
	{
		DoubleDocument that = (DoubleDocument)other;
		return Double.compare(this.value, that.value);
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.Document#modify(nl.thanod.evade.document.modifiers.Modifier)
	 */
	@Override
	public DoubleDocument modify(Modifier m)
	{
		if (m == null)
			return this;
		return new DoubleDocument(this.version, m.modify(this.value));
	}

}
