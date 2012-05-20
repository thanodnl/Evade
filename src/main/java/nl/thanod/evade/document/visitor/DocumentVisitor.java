/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public abstract class DocumentVisitor<OUT, IN>
{
	/**
	 * Called on a visitor by the
	 * {@link StringDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link StringDocument} on which the accept method is
	 *            called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(StringDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link NullDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link NullDocument} on which the accept method is called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(NullDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link DictDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link DictDocument} on which the accept method is called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(DictDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link BooleanDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link BooleanDocument} on which the accept method is
	 *            called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(BooleanDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link IntegerDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link IntegerDocument} on which the accept method is
	 *            called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(IntegerDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link LongDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link LongDocument} on which the accept method is called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(LongDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link UUIDDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link UUIDDocument} on which the accept method is called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(UUIDDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link DoubleDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link DoubleDocument} on which the accept method is
	 *            called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(DoubleDocument doc, IN data);

	/**
	 * Called on a visitor by the
	 * {@link FloatDocument#accept(DocumentVisitor, Object)} method
	 * @param doc
	 *            the {@link FloatDocument} on which the accept method is called
	 * @param data
	 *            user data provided to the accept method
	 * @return the value of type OUT calculated by the visit method
	 */
	public abstract OUT visit(FloatDocument doc, IN data);

}
