/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public class ValueDocumentVisitor<IN> extends DocumentVisitor<ValueDocument, IN>
{
	public static final ValueDocumentVisitor<Void> VALUE = new ValueDocumentVisitor<Void>();
	
	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.StringDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(StringDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.NullDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(NullDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DictDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(DictDocument doc, IN data)
	{
		return new NullDocument(doc.version);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.BooleanDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(BooleanDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.IntegerDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(IntegerDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.LongDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(LongDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.UUIDDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(UUIDDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DoubleDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(DoubleDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.FloatDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(FloatDocument doc, IN data)
	{
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.TupleDocument, java.lang.Object)
	 */
	@Override
	public ValueDocument visit(TupleDocument doc, IN data)
	{
		return doc;
	}
}
