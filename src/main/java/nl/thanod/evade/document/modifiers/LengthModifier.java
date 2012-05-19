/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.IntegerDocument;
import nl.thanod.evade.document.StringDocument;

/**
 * @author nilsdijk
 */
public class LengthModifier extends Modifier
{

	public static LengthModifier INSTANCE = new LengthModifier();

	@Override
	public Document visit(StringDocument doc, Void v)
	{
		return new IntegerDocument(doc.version, doc.value.length());
	}
}
