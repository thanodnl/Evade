/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public class LowerCase extends Modifier
{
	public static LowerCase INSTANCE = new LowerCase();

	@Override
	public Document visit(StringDocument doc, Void in)
	{
		return new StringDocument(doc.version, doc.value.toLowerCase());
	}
}
