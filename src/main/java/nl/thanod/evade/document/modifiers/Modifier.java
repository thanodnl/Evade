/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public abstract class Modifier extends DocumentVisitor<Document, Void>
{

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.StringDocument, java.lang.Object)
	 */
	@Override
	public Document visit(StringDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.NullDocument, java.lang.Object)
	 */
	@Override
	public Document visit(NullDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.DictDocument, java.lang.Object)
	 */
	@Override
	public Document visit(DictDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.BooleanDocument, java.lang.Object)
	 */
	@Override
	public Document visit(BooleanDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.IntegerDocument, java.lang.Object)
	 */
	@Override
	public Document visit(IntegerDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.LongDocument, java.lang.Object)
	 */
	@Override
	public Document visit(LongDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.UUIDDocument, java.lang.Object)
	 */
	@Override
	public Document visit(UUIDDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.DoubleDocument, java.lang.Object)
	 */
	@Override
	public Document visit(DoubleDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.FloatDocument, java.lang.Object)
	 */
	@Override
	public Document visit(FloatDocument doc, Void data)
	{
		return defaultVisit(doc, data);
	}

	/**
	 * This method is called when the implementor of a modifier did not
	 * overwrite the specified visit method for the document being visited. It
	 * is incorrect behavior to do type testing within the body of this
	 * function. If it is desired you should override the correct visit method.
	 * @param doc
	 *            the {@link Document} not handeled by any other visit method
	 * @param data
	 *            --ignore--
	 * @return the modified {@link Document}
	 */
	public abstract Document defaultVisit(Document doc, Void data);

}