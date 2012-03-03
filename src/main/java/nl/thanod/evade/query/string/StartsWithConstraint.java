/**
 * 
 */
package nl.thanod.evade.query.string;

import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public class StartsWithConstraint extends StringConstraint
{

	public StartsWithConstraint(Modifier m, String s)
	{
		super(m, s);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.query.string.StringConstraint#doTest(java.lang.String)
	 */
	@Override
	public boolean doTest(String s)
	{
		return s.startsWith(this.s);
	}

}
