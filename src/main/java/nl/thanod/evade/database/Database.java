/**
 * 
 */
package nl.thanod.evade.database;

import java.util.HashMap;
import java.util.Map;

import nl.thanod.evade.collection.Table;

/**
 * @author nilsdijk
 */
public class Database
{
	private final Map<String, Table> collections = new HashMap<String, Table>();

	public Database()
	{
	}

	public void addCollection(String name, Table table)
	{
		this.collections.put(name, table);
	}

	public Table getCollection(String name)
	{
		return this.collections.get(name);
	}
}
