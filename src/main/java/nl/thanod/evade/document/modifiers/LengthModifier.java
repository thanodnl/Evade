/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.IntegerDocument;
import nl.thanod.evade.document.StringDocument;

/**
 * {@link LengthModifier} creates an {@link IntegerDocument} for the
 * {@link StringDocument} provided containing the length of the given
 * {@link String}. The {@link Document#version} will be equal to the
 * {@link StringDocument} given.
 * @author nilsdijk
 */
public class LengthModifier extends Modifier
{

	public static LengthModifier INSTANCE = new LengthModifier();

	@Override
	public Document visit(StringDocument doc, Void v)
	{
		return new IntegerDocument(doc.version, doc.value.length());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.AbstractDocumentVisitor#defaultVisit
	 * (nl.thanod.evade.document.Document, java.lang.Object)
	 */
	@Override
	public Document defaultVisit(Document doc, Void data)
	{
		return new IntegerDocument(doc.version, 0);
	}
}
