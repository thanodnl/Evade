/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public abstract class AbstractDocumentVisitor<OUT, IN> extends DocumentVisitor<OUT, IN>
{

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.StringDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(StringDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.NullDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(NullDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DictDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(DictDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.BooleanDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(BooleanDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.IntegerDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(IntegerDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.LongDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(LongDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.UUIDDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(UUIDDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.DoubleDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(DoubleDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.FloatDocument, java.lang.Object)
	 */
	@Override
	public OUT visit(FloatDocument doc, IN data)
	{
		return defaultVisit(doc, data);
	}

	/**
	 * This method is called when the implementor of a visitor did not overwrite
	 * the specified visit method for the document being visited. It is
	 * incorrect behavior to do type testing within the body of this function.
	 * If it is desired you should override the correct visit method.
	 * @param doc
	 *            the {@link Document} not handeled by any other visit method
	 * @param data
	 *            the userprovided data
	 * @return the modified {@link Document}
	 */
	public abstract OUT defaultVisit(Document doc, IN data);

}
