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
		File dir = new File("data");
		String name = "out";
		Table t = Table.load(dir, name);
		System.out.println(SSTable.save(dir, name, t));
	}
}
