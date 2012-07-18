/**
 * 
 */
package nl.thanod.evade.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.*;
import nl.thanod.evade.collection.index.IndexCreator.IndexCreatorListener;

/**
 * @author nilsdijk
 */
public class Database
{
	private final Map<String, Table> collections = new HashMap<String, Table>();
	private final Map<String, TableIndex> indices = new HashMap<String, TableIndex>();

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
