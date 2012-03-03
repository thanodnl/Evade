/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.document.Document;
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
		File f = new File("data", "out97.sstable");
		SSTable ss = new SSTable(f);
		Constraint c = new StartsWithConstraint(new LowerCase(), "zh1");
		List<String> path = new ArrayList<String>();
		path.add("name");
		int co = 0;
		for (Document.Entry e : ss) {
			Document d = e.doc;
			d = d.path(path);
			if (d != null && d.test(c)) {
				System.out.println(e);
				co++;
			}
		}
		System.out.println("found: " + co);
	}
}
