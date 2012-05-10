/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public abstract class ParameterizedDocumentVisitor<User>
{
	public abstract void visit(StringDocument doc, User data);

	public abstract void visit(NullDocument doc, User data);

	public abstract void visit(DictDocument doc, User data);

	public abstract void visit(BooleanDocument doc, User data);

	public abstract void visit(IntegerDocument doc, User data);

	public abstract void visit(LongDocument doc, User data);

	public abstract void visit(UUIDDocument doc, User data);

	public abstract void visit(DoubleDocument doc, User data);

	public abstract void visit(FloatDocument doc, User data);

}
