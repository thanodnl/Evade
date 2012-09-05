/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;

/**
 * @author nilsdijk
 */
public class Compacter
{
	public static void main(String... args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");
		
		Database db = conf.loadDatabase();
		db.getCollection("github").majorCompact(null);
	}
}
