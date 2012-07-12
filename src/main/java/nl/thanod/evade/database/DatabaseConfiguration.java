/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;

import nl.thanod.evade.collection.Table;

/**
 * @author nilsdijk
 */
public class DatabaseConfiguration
{
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
			}
		} else {
			System.out.println("WARNING: no collections loaded");
		}

		return db;
	}

	/**
	 * 
	 */
	public void preprocess()
	{
		this.datadir = this.datadir.getAbsoluteFile();
	}

}
