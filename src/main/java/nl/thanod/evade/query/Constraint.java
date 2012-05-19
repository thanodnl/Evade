/**
 * 
 */
package nl.thanod.evade.query;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class Constraint extends DocumentVisitor<Boolean, Void>
{

	public final Modifier modifier;

	/**
	 * @param m
	 */
	public Constraint(Modifier m)
	{
		this.modifier = m;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.StringDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(StringDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.NullDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(NullDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.DictDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(DictDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.BooleanDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(BooleanDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.IntegerDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(IntegerDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.LongDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(LongDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.UUIDDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(UUIDDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.DoubleDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(DoubleDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.ParameterizedDocumentVisitor#visit(nl
	 * .thanod.evade.document.FloatDocument, java.lang.Object)
	 */
	@Override
	public Boolean visit(FloatDocument doc, Void data)
	{
		return Boolean.FALSE;
	}

	public boolean test(Document doc)
	{
		if (this.modifier != null)
			doc = doc.accept(this.modifier, null);
		return doc.accept(this, null);
	}

}