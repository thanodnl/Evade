/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nl.thanod.evade.document.DictDocument;
import nl.thanod.evade.document.Document;

/**
 * The {@link RecursiveModifier} class can be used to modify all elements within
 * a {@link DictDocument} recursively. Only the specific visit methode should be
 * implemented to transform the objects you would like to modify.
 * @author nilsdijk
 */
public abstract class RecursiveModifier extends Modifier
{
	@Override
	public final Document visit(DictDocument doc, Void data)
	{
		Map<String, Document> map = new TreeMap<String, Document>();
		for (Entry<String, Document> e : doc.entrySet())
			map.put(e.getKey(), e.getValue().accept(this, data));
		return new DictDocument(map, doc.clearedOn, false);
	}

}
