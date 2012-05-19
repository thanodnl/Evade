/**
 * 
 */
package nl.thanod.evade.query.string;

import nl.thanod.evade.document.StringDocument;
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

	/* (non-Javadoc)
	 * @see nl.thanod.evade.query.string.StringConstraint#visit(nl.thanod.evade.document.StringDocument, java.lang.Void)
	 */
	@Override
	public Boolean visit(StringDocument doc, Void v)
	{
		return  doc.value.compareTo(this.s) > 0;
	}
}
