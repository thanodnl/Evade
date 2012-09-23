/**
 * 
 */
package nl.thanod.evade.document.visitor;

import nl.thanod.evade.document.*;

/**
 * @author nilsdijk
 */
public class GranularityVisitor extends ValueDocumentVisitor<Integer>
{
	public static final GranularityVisitor VISITOR = new GranularityVisitor();
	private GranularityVisitor()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.evade
	 * .document.TupleDocument, java.lang.Object)
	 */
	@Override
	public TupleDocument visit(TupleDocument doc, Integer data)
	{
		return doc.granularity(data);
	}
}
