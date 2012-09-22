/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.CompoundIndex;
import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.ValueDocument;

/**
 * @author nilsdijk
 */
public class Find
{
	public static void main(String... args) throws IOException
	{
		String collection = "github_small";
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		IndexDescriptor desc = new IndexDescriptor(new DocumentPath("actor"));

		db.ensureIndex(null, collection, desc);
		CompoundIndex idx = db.getTableIndex(collection).get(desc);

		Table table = db.getCollection(collection);

		final ValueDocument find = new StringDocument("okoeroo");

		Comparable<Entry> comp = new Comparable<Entry>() {
			@Override
			public int compareTo(Entry paramT)
			{
				return ValueDocument.VALUE_COMPARE.compare(find, paramT.match);
			}
		};
		Entry e = idx.before(comp);
		int c = 0;
		while (e != null) {
			if (comp.compareTo(e) != 0)
				break;
			System.out.println(e.id + ": " + table.get(e.id));
			c++;
			e = e.next();
		}
		System.out.println("found " + c);
	}
}
