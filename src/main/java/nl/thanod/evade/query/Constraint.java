/**
 * 
 */
package nl.thanod.evade.query;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.document.visitor.AbstractDocumentVisitor;

/**
 * @author nilsdijk
 */
public class Constraint extends AbstractDocumentVisitor<Boolean, Void>
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
	 * nl.thanod.evade.document.visitor.AbstractDocumentVisitor#defaultVisit
	 * (nl.thanod.evade.document.Document, java.lang.Object)
	 */
	@Override
	public Boolean defaultVisit(Document doc, Void data)
	{
		return Boolean.FALSE;
	}

	public boolean test(Document doc)
	{
		doc = Modifier.safeModify(this.modifier, doc);
		return doc.accept(this);
	}

}