/**
 * 
 */
package nl.thanod.evade.query;

import nl.thanod.evade.document.DictDocument;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 *
 */
public abstract class AbstractConstraint implements Constraint
{
	protected final Modifier m;

	/**
	 * @param m
	 */
	public AbstractConstraint(Modifier m)
	{
		this.m = m;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.query.Constraint#test(nl.thanod.evade.document.StringDocument)
	 */
	@Override
	public boolean test(StringDocument doc){
		return false;
	}
	
	@Override
	public boolean test(NullDocument doc){
		return false;
	}
	
	@Override
	public boolean test(DictDocument doc){
		return false;
	}
}
