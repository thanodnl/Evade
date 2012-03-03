/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.DictDocument;
import nl.thanod.evade.document.NullDocument;
import nl.thanod.evade.document.StringDocument;

/**
 * @author nilsdijk
 */
public interface DocumentVisitor
{
	public void visit(StringDocument doc);

	public void visit(NullDocument doc);

	public void visit(DictDocument doc);
}
