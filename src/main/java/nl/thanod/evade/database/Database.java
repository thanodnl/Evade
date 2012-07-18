/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.IndexCreator;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.collection.index.SSIndex;
import nl.thanod.evade.collection.index.TableIndex;

/**
 * @author nilsdijk
 */
public class Database
{
	private final Map<String, Table> collections = new HashMap<String, Table>();
	private final Map<String, TableIndex> indices = new HashMap<String, TableIndex>();
	private final DatabaseConfiguration config;

	public Database(DatabaseConfiguration config)
	{
		this.config = config;
	}

	public void addCollection(Table table)
	{
		this.collections.put(table.name, table);
	}

	public Table getCollection(String name)
	{
		return this.collections.get(name);
	}

	public Table getOrCreateCollection(String name)
	{
		if (!this.collections.containsKey(name)) {
			Table table = Table.load(new File(config.datadir, name), name);
			addCollection(table);
		}
		return this.collections.get(name);
	}

	public TableIndex getTableIndex(String collectionName)
	{
		TableIndex tidx = this.indices.get(collectionName);
		if (tidx == null)
			this.indices.put(collectionName, tidx = new TableIndex());
		return tidx;
	}

	public void ensureIndex(Executor exec, final String collectionName, IndexDescriptor desc)
	{
		if (getTableIndex(collectionName).contains(desc))
			return;

		// create the index
		IndexCreator creator = new IndexCreator(getCollection(collectionName), desc);

		creator.addListener(new IndexCreator.IndexCreatorListener() {

			@Override
			public void indexCreated(List<SSIndex> indices)
			{
				for (SSIndex index : indices)
					getTableIndex(collectionName).add(index);
			}
		});

		if (exec != null)
			exec.execute(creator);
		else
			creator.run();
	}
}
