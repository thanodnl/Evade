/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.util.Documenter;

/**
 * @author nilsdijk
 */
public class Compacter
{
	public static void main(String... args) throws IOException
	{
		List<SSTable> tables = new LinkedList<SSTable>();

		File f;
		int i = 0;
		while(true) {
			f = new File("data", "out" + i++ + ".sstable");
			if (!f.exists())
				break;
			
			tables.add(new SSTable(f));
		}
		System.out.println("opened " + tables.size() +" tables");
		
		System.out.println(SSTable.save(new Documenter(tables)));
	}
}
