/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import nl.thanod.evade.remote.Remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author nilsdijk
 */
public class Main
{
	private static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String... args) throws FileNotFoundException
	{
		File configFile = findConfigFile();
		InputStream stream = new FileInputStream(configFile);
		Yaml yaml = new Yaml(new Constructor(DatabaseConfiguration.class));
		DatabaseConfiguration config = (DatabaseConfiguration) yaml.load(stream);
		log.info("Loaded configuration from {}", configFile.getAbsolutePath());

		// initialize the database
		Database db = config.loadDatabase();

		// initialize the remote interfaces
		List<Remote> remotes = config.loadRemotes();
		for (Remote r : remotes) {
			log.info("Booting {}", r);

			// put the database on the remote
			r.setDB(db);

			// start the remote on a new thread
			Thread t = new Thread(r);
			t.setName("Remote: " + r.toString());
			t.start();
		}
	}

	/**
	 * Look at default places to find the configuration file Currently it just
	 * returns a static file wether it exists or not.
	 * <p>
	 * Future: look at /etc/evade/config.yml and ~/.evade/config.yml etc.
	 * @return
	 */
	private static File findConfigFile()
	{
		return new File("config", "config.yml");
	}
}
