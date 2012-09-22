/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.util.UUID;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;

/**
 * @author nilsdijk
 */
public class Smallifier
{
	public static void main(String... args)
	{
		String name = "github";

		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		Table to = db.getCollection(name);
		Table tn = db.getOrCreateCollection(name + "_small");
		long c = 0;
		for (UUID id : to.uuids()) {
			if (++c % 17 == 0)
				tn.update(id, to.get(id));
		}
		tn.persist();

	}
}
