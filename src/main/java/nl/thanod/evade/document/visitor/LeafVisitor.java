/**
 * 
 */
package nl.thanod.evade.document.visitor;

import java.util.Map.Entry;

import nl.thanod.evade.document.DictDocument;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;

/**
 * @author nilsdijk
 */
public abstract class LeafVisitor extends AbstractDocumentVisitor<Void, DocumentPath>
{

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DictDocument, java.lang.Object)
	 */
	@Override
	public final Void visit(DictDocument doc, DocumentPath path)
	{
		if (path == null)
			path = DocumentPath.EMPTY;
		for (Entry<String, Document> e : doc.entrySet()) {
			e.getValue().accept(this, new DocumentPath(path, e.getKey()));
		}
		return null;
	}
}
