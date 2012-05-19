/**
 * 
 */
package nl.thanod.evade.query.string;

import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public abstract class StringConstraint extends Constraint
{

	protected final String s;

	public StringConstraint(Modifier m, String s)
	{
		super(m);
		this.s = s;
	}

	@Override
	public abstract Boolean visit(StringDocument doc, Void v);
}
