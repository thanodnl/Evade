/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.StringDocument;

/**
 * @author nilsdijk
 */
public class UpperCase extends Modifier
{

	@Override
	public Document visit(StringDocument doc, Void in)
	{
		return new StringDocument(doc.version, doc.value.toUpperCase());
	}
}
