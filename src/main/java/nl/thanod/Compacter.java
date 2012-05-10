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
		Table t = Table.load(new File("data"), "out");
		System.out.println(SSTable.save(t));
	}
}
