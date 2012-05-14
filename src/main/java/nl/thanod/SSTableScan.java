/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.collection.index.Memdex;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.modifiers.LowerCase;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.query.string.StartsWithConstraint;

/**
 * @author nilsdijk
 */
public class SSTableScan
{
	public static void main(String... args) throws IOException
	{
		long took;
		File dataDir = new File("data", "github");

		int i = 0;
		do {
			File sstable = new File(dataDir, "github" + i++ + ".sstable");
			System.out.println(sstable);
			if (!sstable.exists())
				break;
			SSTable table = new SSTable(sstable);

			took = System.nanoTime();
			DocumentPath path = new DocumentPath("actor_attributes", "login");
			Memdex.persistSortedIndex(table, path, new LowerCase());
			took = System.nanoTime() - took;
			System.out.println("It took " + took / 1000000f + "ms create the index");
			
			// finished with this table so files can be unmapped
			table.close();
		} while (true);
	}
}
