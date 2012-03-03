/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.*;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.util.Peekerator;

/**
 * @author nilsdijk
 */
public abstract class Collection implements Iterable<Document.Entry>
{
	public abstract boolean contains(UUID id);

	public abstract Document get(UUID id);

	public static Set<SSTable> compact(Collection... collections)
	{
		List<Peekerator<Document.Entry>> its = new ArrayList<Peekerator<Document.Entry>>(collections.length);
		for (int i = 0; i < collections.length; i++)
			its.add(new Peekerator<Document.Entry>(collections[i].iterator()));

		return Collections.emptySet();
	}
}
