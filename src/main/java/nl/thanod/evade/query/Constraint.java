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
public interface Constraint
{

	public boolean test(StringDocument doc);
	
	public boolean test(NullDocument doc);
	
	public boolean test(DictDocument doc);

	public Modifier getModifier();

}