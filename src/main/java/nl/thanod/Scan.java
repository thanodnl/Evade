/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.IntegerDocument;
import nl.thanod.evade.document.ValueDocument;

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

		DocumentPath path = new DocumentPath("age");
		ValueDocument find = new IntegerDocument(0, 27);

		//		Constraint c = new StartsWithConstraint(new LowerCase(), "zh1");

		//		for (int i = 0; i < 100; i++) {
		//			int co = 0;
		long start = System.nanoTime();
		int c = 0;
		for (Document.Entry e : t) {
			Document d = e.doc;
			if (d == null)
				continue;
			d = d.get(path);
			if (d == null || !(d instanceof ValueDocument))
				continue;
			ValueDocument vd = (ValueDocument) d;
			if (ValueDocument.VALUE_COMPARE.compare(vd, find) == 0)
				c++;
			//			Document age = DocumentBuilder.start(System.currentTimeMillis()).put("age", RANDY.nextInt(17) + 15).make();
			//			t.update(e.id, age);
			//			System.out.println(e);
		}
		long took = System.nanoTime() - start;
		System.out.println("seen " + c + " documents in " + took + "ns (" + took / 1000000.0 + "ms)");
		t.persist();

		//		start = System.nanoTime();
		//		t.majorCompact(null);
		//		took = System.nanoTime() - start;
		//		System.out.println("compaction took " + took + "ns (" + took / 1000000.0 + "ms)");

		//			double took = (System.nanoTime() - start) / 1000000.0;
		//			System.out.println("took: " + took + "ms");
		//			System.out.println("found: " + co);
		//		}
	}
}
