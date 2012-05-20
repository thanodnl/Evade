/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.visitor.AbstractDocumentVisitor;
import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public abstract class Modifier extends AbstractDocumentVisitor<Document, Void>
{

	/**
	 * Safely tests of mod is not null before running it through
	 * {@link Document#accept(DocumentVisitor, Object)}
	 * @param mod
	 *            the modifier to apply on doc
	 * @param doc
	 *            the {@link Document} to modify
	 * @return a possibly modified {@link Document}
	 */
	public static Document safeModify(Modifier mod, Document doc)
	{
		if (mod == null || doc == null)
			return doc;
		return doc.accept(mod);
	}
}