/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.query.Constraint;


/**
 * @author nilsdijk
 */
public class NullDocument extends Document
{

	/**
	 * @param version
	 */
	public NullDocument(long version)
	{
		super(version, Type.NULL);
	}

	@Override
	public String toString()
	{
		return super.toString() + "null";
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
	 * @see nl.thanod.evade.document.Document#visit(nl.thanod.evade.document.visitor.DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		visitor.visit(this);
	}
}
