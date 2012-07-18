/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.SSIndex;
import nl.thanod.evade.collection.index.TableIndex;
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
		Database db = new Database(this);

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
				db.addCollection(table);
				log.info("Open collection {}", collection.getName());

				loadIndex(db.getTableIndex(collection.getName()), collection, collection.getName());
			}
		} else {
			log.info("No collections loaded");
			System.out.println("WARNING: no collections loaded");
		}

		return db;
	}

	/**
	 * @param tableIndex
	 * @param collection
	 * @param name
	 */
	private void loadIndex(TableIndex tableIndex, File dir, final String collectionName)
	{
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File paramFile, String name)
			{
				return name.endsWith(".idx") && name.startsWith(collectionName);
			}
		});

		if (files == null)
			return;

		for (File file : files) {
			try {
				SSIndex idx = new SSIndex(file);
				tableIndex.add(idx);
			} catch (IOException ball) {
				log.error("Could not open index " + file, ball);
			}
		}
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
