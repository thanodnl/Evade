/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.StringDocument;

/**
 * {@link UpperCase} transforms {@link StringDocument} instances to their upper
 * case variant
 * @author nilsdijk
 */
public class UpperCase extends Modifier
{

	@Override
	public Document visit(StringDocument doc, Void in)
	{
		return new StringDocument(doc.version, doc.value.toUpperCase());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.modifiers.Modifier#defaultVisit(nl.thanod.evade
	 * .document.Document, java.lang.Void)
	 */
	@Override
	public Document defaultVisit(Document doc, Void data)
	{
		return new NullDocument(doc.version);
	}
}
