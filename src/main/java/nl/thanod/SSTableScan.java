/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.collection.index.IndexSerializer;
import nl.thanod.evade.document.DocumentPath;

/**
 * @author nilsdijk
 */
public class SSTableScan
{
	public static void main(String... args) throws IOException
	{
		long took;
		String name = "github";
		File dataDir = new File("data", name);

		int i = 0;
		do {
			File sstable = new File(dataDir, name + i++ + ".sstable");
			System.out.println(sstable);
			if (!sstable.exists())
				break;
			SSTable table = new SSTable(sstable);

			took = System.nanoTime();
			DocumentPath path = new DocumentPath("actor_attributes", "login");
			IndexSerializer.persistSortedIndex(table, new IndexDescriptor(path), dataDir, name);
			took = System.nanoTime() - took;
			System.out.println("It took " + took / 1000000f + "ms create the index");
			
			// finished with this table so files can be unmapped
			table.close();
		} while (true);
	}
}
