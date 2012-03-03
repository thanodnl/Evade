/**
 * 
 */
package nl.thanod.evade.query.string;

import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public class GreaterThanString extends StringConstraint
{
	public GreaterThanString(Modifier m, String s)
	{
		super(m,s);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.query.Constraint#test(nl.thanod.evade.document.Document)
	 */
	@Override
	public boolean doTest(String s)
	{
		return s.compareTo(this.s) > 0;
	}
}
