/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.modifiers.LowerCase;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.query.string.StartsWithConstraint;

/**
 * @author nilsdijk
 */
public class Scan
{
	public static void main(String... args) throws IOException
	{
		Table t = Table.load(new File("data"), "out");

		Constraint c = new StartsWithConstraint(new LowerCase(), "zh1");
		DocumentPath path = new DocumentPath("name");

		for (int i = 0; i < 100; i++) {
			int co = 0;
			long start = System.nanoTime();
			for (Document.Entry e : t) {
				Document d = e.doc;
				d = d.get(path);
				if (d != null && d.test(c)) {
					System.out.println(e);
					co++;
				}
			}
			float took = (System.nanoTime() - start) / 1000000;
			System.out.println("took: " + took + "ms");
			System.out.println("found: " + co);
		}
	}
}
