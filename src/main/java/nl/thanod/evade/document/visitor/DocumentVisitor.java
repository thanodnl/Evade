/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public abstract class DocumentVisitor
{
	public abstract void visit(StringDocument doc);

	public abstract void visit(NullDocument doc);

	public abstract void visit(DictDocument doc);

	public abstract void visit(BooleanDocument doc);

	public abstract void visit(IntegerDocument doc);

	public abstract void visit(LongDocument doc);

	public abstract void visit(UUIDDocument doc);

	public abstract void visit(DoubleDocument doc);

	public abstract void visit(FloatDocument doc);

}
