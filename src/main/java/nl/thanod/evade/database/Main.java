/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.thanod.evade.remote.Remote;
import nl.thanod.evade.remote.RemoteBuilder;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author nilsdijk
 */
public class Main
{
	private static final List<Remote> remotes = new ArrayList<Remote>();
	private static Database db;

	public static void main(String... args) throws FileNotFoundException
	{
		InputStream stream = new FileInputStream(new File("config", "config.yml"));
		Yaml yaml = new Yaml(new Constructor(DatabaseConfiguration.class));
		DatabaseConfiguration config = (DatabaseConfiguration) yaml.load(stream);
		
		config.preprocess();
		
		System.out.println(config);
		System.out.println(config.datadir.getAbsolutePath());

		// initialize the database
		Main.db = config.loadDatabase();

		// initialize the remote interfaces
		loadRemotes(config.remote);
		for (Remote r : Main.remotes) {
			System.out.println("initializing " + r);

			// put the database on the remote
			r.setDB(Main.db);

			// start the remote on a new thread
			Thread t = new Thread(r);
			t.setName("Remote: " + r.toString());
			t.start();
		}
	}

	/**
	 * @param remote
	 */
	private static void loadRemotes(List<Map<String, Object>> remotes)
	{
		if (remotes != null) {
			for (Map<String, Object> rc : remotes) {
				Remote remote = RemoteBuilder.load(rc);
				if (remote == null) {
					System.err.println("No remote for " + rc);
					continue;
				}
				Main.remotes.add(remote);
			}
		}
	}
}
