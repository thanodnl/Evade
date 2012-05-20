/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.*;

/**
 * {@link LowerCase} transforms {@link StringDocument} instances to their lower
 * case variant
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

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.AbstractDocumentVisitor#defaultVisit
	 * (nl.thanod.evade.document.Document, java.lang.Object)
	 */
	@Override
	public Document defaultVisit(Document doc, Void data)
	{
		return new NullDocument(doc.version);
	}
}
