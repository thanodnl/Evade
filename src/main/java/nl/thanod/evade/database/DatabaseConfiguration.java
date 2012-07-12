/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.remote.Remote;
import nl.thanod.evade.remote.RemoteBuilder;

/**
 * @author nilsdijk
 */
public class DatabaseConfiguration
{
	private static Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

	public File datadir;
	public List<Map<String, Object>> remote;

	@Override
	public String toString()
	{
		return "DatabaseConfiguration [datadir=" + datadir + ", remote=" + remote + "]";
	}

	/**
	 * @return
	 */
	public Database loadDatabase()
	{
		Database db = new Database();

		File[] collectionDirs = this.datadir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file)
			{
				return file.isDirectory();
			}
		});

		if (collectionDirs != null) {
			for (File collection : collectionDirs) {
				Table table = Table.load(collection, collection.getName());
				db.addCollection(table.getName(), table);
				log.info("Open collection {}", collection.getName());
			}
		} else {
			log.info("No collections loaded");
			System.out.println("WARNING: no collections loaded");
		}

		return db;
	}

	/**
	 * @return
	 */
	public List<Remote> loadRemotes()
	{
		ArrayList<Remote> remoteList = new ArrayList<Remote>();
		if (this.remote != null) {
			for (Map<String, Object> rc : this.remote) {
				Remote remote = RemoteBuilder.load(rc);
				if (remote == null) {
					log.error("No remote for {}", rc);
					continue;
				}
				remoteList.add(remote);
			}
		}
		remoteList.trimToSize();
		return remoteList;
	}
}
