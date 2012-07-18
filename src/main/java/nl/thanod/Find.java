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
import nl.thanod.evade.document.IntegerDocument;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.ValueDocument;

/**
 * @author nilsdijk
 */
public class Find
{
	public static void main(String... args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		IndexDescriptor desc = new IndexDescriptor(new DocumentPath("actor"));
		
		System.out.println(db.getCollection("github").iterator().next());

		db.ensureIndex(null, "github", desc);
		db.ensureIndex(null, "names", desc);
		CompoundIndex idx = db.getTableIndex("github").get(desc);
		
		Table table = db.getCollection("github");
		
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
//			System.out.println(e);
			c++;
			e = e.next();
		}
		System.out.println("found " + c);
	}
}
