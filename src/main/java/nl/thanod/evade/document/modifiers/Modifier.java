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
		return doc;
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
		return doc;
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
		return doc;
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
		// TODO Auto-generated method stub
		return doc;
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
		return doc;
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
		return doc;
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
		return doc;
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
		return doc;
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
		return doc;
	}
}