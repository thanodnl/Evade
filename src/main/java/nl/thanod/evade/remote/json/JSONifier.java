/**
 * 
 */
package nl.thanod.evade.remote.json;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.visitor.DocumentVisitor;

import org.json.JSONObject;

/**
 * @author nilsdijk
 */
public class JSONifier extends DocumentVisitor<Object, Void>
{

	public static final JSONifier INSTANCE = new JSONifier();

	private JSONifier()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.StringDocument, java.lang.Object)
	 */
	@Override
	public Object visit(StringDocument doc, Void data)
	{
		return doc.value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.NullDocument, java.lang.Object)
	 */
	@Override
	public Object visit(NullDocument doc, Void data)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.DictDocument, java.lang.Object)
	 */
	@Override
	public Object visit(DictDocument doc, Void data)
	{
		Map<String, Object> content = new TreeMap<String, Object>();
		for (Entry<String, Document> e : doc.entrySet())
			content.put(e.getKey(), e.getValue().accept(this));
		return new JSONObject(content);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.BooleanDocument, java.lang.Object)
	 */
	@Override
	public Object visit(BooleanDocument doc, Void data)
	{
		return doc.value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.IntegerDocument, java.lang.Object)
	 */
	@Override
	public Object visit(IntegerDocument doc, Void data)
	{
		return doc.value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.LongDocument, java.lang.Object)
	 */
	@Override
	public Object visit(LongDocument doc, Void data)
	{
		return doc.value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.UUIDDocument, java.lang.Object)
	 */
	@Override
	public Object visit(UUIDDocument doc, Void data)
	{
		return doc.value.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.DoubleDocument, java.lang.Object)
	 */
	@Override
	public Object visit(DoubleDocument doc, Void data)
	{
		return doc.value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
	 * evade.document.FloatDocument, java.lang.Object)
	 */
	@Override
	public Object visit(FloatDocument doc, Void data)
	{
		return doc.value;
	}
}
