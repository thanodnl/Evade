/**
 * 
 */
package nl.thanod.evade.query.string;

import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.query.AbstractConstraint;

/**
 * @author nilsdijk
 *
 */
public abstract class StringConstraint extends AbstractConstraint
{
	
	protected final String s;

	public StringConstraint(Modifier m, String s){
		super(m);
		this.s = this.m.modify(s);
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.query.Constraint#test(nl.thanod.evade.document.StringDocument)
	 */
	@Override
	public final boolean test(StringDocument doc)
	{
		return doTest(this.m.modify(doc.value));
	}

	/**
	 * @param s
	 * @return
	 */
	public abstract boolean doTest(String s);

}
