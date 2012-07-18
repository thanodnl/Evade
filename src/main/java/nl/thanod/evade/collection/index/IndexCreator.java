/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class IndexCreator implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(IndexCreator.class);

	public static interface IndexCreatorListener
	{
		public void indexCreated(List<SSIndex> tables);
	}

	private final Table table;
	private final IndexDescriptor desc;
	private IndexCreatorListener listener;

	public IndexCreator(Table table, IndexDescriptor desc)
	{
		this.table = table;
		this.desc = desc;
	}

	public void addListener(final IndexCreatorListener listener)
	{
		if (this.listener == null) {
			this.listener = listener;
		} else {
			final IndexCreatorListener current = this.listener;
			this.listener = new IndexCreatorListener() {
				@Override
				public void indexCreated(List<SSIndex> tables)
				{
					current.indexCreated(tables);
					listener.indexCreated(tables);
				}
			};
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try {
			//List<File> files = IndexSerializer.persistSortedIndex(this.table, this.desc, this.table.directory, this.table.name);
			List<File> files = IndexSerializer.createIndex(this.table, this.desc, 100000);
			List<SSIndex> tables = new ArrayList<SSIndex>(files.size());
			for (File file : files)
				tables.add(new SSIndex(file));
			if (listener != null)
				listener.indexCreated(tables);
		} catch (IOException ball) {
			log.error("Could not create index for {}", this.table.name);
		}
	}
}
