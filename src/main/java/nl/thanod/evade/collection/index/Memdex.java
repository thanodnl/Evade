/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.modifiers.Modifier;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.util.Linked;

/**
 * @author nilsdijk
 */
public class Memdex implements Index
{
	private final Map<StringDocument, Linked<UUID>> map;
	private final Modifier modifier;

	private Memdex(Map<StringDocument, Linked<UUID>> map, Modifier modifier)
	{
		this.map = map;
		this.modifier = modifier;
	}

	public static Memdex createSorted(Iterable<Document.Entry> data, List<String> path, Constraint constraint)
	{
		Modifier mod = constraint.getModifier();

		Map<StringDocument, Linked<UUID>> map = new TreeMap<StringDocument, Linked<UUID>>(StringDocument.VALUE_SORT);

		for (Document.Entry e : data) {
			Document doc = e.doc.path(path);
			if (doc == null || !doc.type.valuetype)
				continue;

			// for developing only, should be refoctored to support all value types
			// need a comparator on content and type for this
			if (!(doc instanceof StringDocument))
				continue;

			// extract the value to index
			StringDocument sd = (StringDocument) doc;
			String s = sd.value;
			s = mod.modify(s);

			sd = new StringDocument(sd.version, s);

			// put it in the map
			map.put(sd, new Linked<UUID>(map.get(sd), e.id));
		}

		// return an index
		return new Memdex(map, mod);
	}
}
