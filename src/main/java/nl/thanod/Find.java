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
		File data = new File ("data");
		
		Table t = Table.load(data, "out");
		SSIndex index = new SSIndex(new File(data, "out0.idx"));

		Constraint c = new StartsWithConstraint(new LowerCase(), "zh1");
		List<String> path = new ArrayList<String>();
		path.add("name");

		for (int i = 0; i < 100; i++) {
			long took = System.nanoTime();
			Entry e = Search.binsearch(index, new Comparable<Entry>() {

				@Override
				public int compareTo(Entry o)
				{
					// TODO test! this could be in the incorrect order
					int diff = o.match.type.code - Document.Type.STRING.code;
					if (diff != 0)
						return diff;

					StringDocument sd = (StringDocument) o.match;
					String s = "zh1";
					return s.compareTo(sd.value);
				}
			});

			// is going wrong when there are multiple entries on the same index
			if (!e.match.test(c))
				e = e.next();
			Document doc = t.get(e.id);
			while (e.match.test(c)) {
				if (doc != null) {
					Document q = doc.path(path);
					if (q.test(c)) {

						System.out.println(new Document.Entry(e.id, doc));
					}
				}
				e = e.next();
				doc = t.get(e.id);
			}

			took = System.nanoTime() - took;
			System.out.println("took: " + took + "ns (" + took / 1000000 + "ms)");
		}
	}
}
