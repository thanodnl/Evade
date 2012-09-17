/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public class Scan
{
	public static final Random RANDY = new Random(System.currentTimeMillis());

	public static void main(String... args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		Table t = db.getCollection("names");

		long start = System.nanoTime();
		int c = 0;
		for (Document.Entry e : t) {
			Document d = e.doc;
			c++;
		}
		long took = System.nanoTime() - start;
		System.out.println("seen " + c + " documents in " + took + "ns (" + took / 1000000.0 + "ms)");
		t.persist();
	}
}
