/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public interface DocumentVisitor
{
	public void visit(StringDocument doc);

	public void visit(NullDocument doc);

	public void visit(DictDocument doc);

	public void visit(BooleanDocument doc);

	public void visit(IntegerDocument doc);

	public void visit(LongDocument doc);

	public void visit(UUIDDocument doc);

	public void visit(DoubleDocument doc);

	public void visit(FloatDocument doc);

}
