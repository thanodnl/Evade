/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public abstract class ParameterizedDocumentVisitor<OUT,IN>
{
	public abstract OUT visit(StringDocument doc, IN data);

	public abstract OUT visit(NullDocument doc, IN data);

	public abstract OUT visit(DictDocument doc, IN data);

	public abstract OUT visit(BooleanDocument doc, IN data);

	public abstract OUT visit(IntegerDocument doc, IN data);

	public abstract OUT visit(LongDocument doc, IN data);

	public abstract OUT visit(UUIDDocument doc, IN data);

	public abstract OUT visit(DoubleDocument doc, IN data);

	public abstract OUT visit(FloatDocument doc, IN data);

}
