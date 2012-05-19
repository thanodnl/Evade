/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.collection.Table;

/**
 * @author nilsdijk
 */
public class Compacter
{
	public static void main(String... args) throws IOException
	{
		String name = "github";
		File dir = new File("data",name);
		Table t = Table.load(dir, name);
		System.out.println(SSTable.save(new File("data","compacted_github"), name, t));
	}
}
