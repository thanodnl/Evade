/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.collection.index.SSIndex;
import nl.thanod.evade.collection.index.Search;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.modifiers.LowerCase;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.query.string.StartsWithConstraint;

/**
 * @author nilsdijk
 */
public class Find
{
	public static void main(String... args) throws IOException
	{

		File data = new File("data");

		Table t = Table.load(data, "github");
		SSIndex index = new SSIndex(new File(data, "github0.idx"));

		find(t, index, "thanodnl");
	}

	/**
	 * @param t
	 * @param index
	 * @param name
	 */
	private static void find(Table t, SSIndex index, final String name)
	{
		System.out.println("looking for " + name);

		Constraint c = new StartsWithConstraint(new LowerCase(), name);
		DocumentPath path = new DocumentPath("actor_attributes", "login");

		Comparable<Entry> search = new Comparable<Entry>() {

			@Override
			public int compareTo(Entry o)
			{
				// TODO test! this could be in the incorrect order
				int diff = o.match.type.code - Document.Type.STRING.code;
				if (diff != 0)
					return diff;

				StringDocument sd = (StringDocument) o.match;
				String s = name;
				return s.compareTo(sd.value);
			}
		};

		List<Document.Entry> result = new ArrayList<Document.Entry>(100);
		long took = System.nanoTime();
		Entry e = Search.before(index, search);

		while (e != null && e.match.test(c)) {
			Document doc = t.get(e.id);
			if (doc != null) {
				Document q = doc.get(path);
				if (q != null) {
					if (q.test(c)) {
						Document.Entry de = new Document.Entry(e.id, doc);
						result.add(de);
					}
				}
			}
			e = e.next();
		}

		took = System.nanoTime() - took;
		System.out.println("took: " + took + "ns (" + took / 1000000 + "ms) to find " + result.size());
	}
}
