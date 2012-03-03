/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.util.Documenter;

/**
 * @author nilsdijk
 */
public class Reading
{
	public static void main(String... args) throws IOException
	{
//		File f = new File("data", "out98.sstable");
//		SSTable ss = new SSTable(f);
		measureReading(getAll());
	}

	public static Documenter getAll() throws IOException
	{
		List<SSTable> tables = new LinkedList<SSTable>();

		File f;
		int i = 0;
		while (true) {
			f = new File("data", "out" + i++ + ".sstable");
			if (!f.exists())
				break;

			tables.add(new SSTable(f));
		}
		return new Documenter(tables);
	}

	public static void measureReading(Iterable<Document.Entry> ss)
	{
		int c = 0;
		long start = System.nanoTime();
		for (Document.Entry e : ss) {
			c++;
			//			System.out.println(e);
		}
		float took = (System.nanoTime() - start) / 1000000f;
		System.out.println("took: " + took + "ms");
		System.out.println(c);
	}
}
