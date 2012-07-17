/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import nl.thanod.evade.util.Documenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class TableCompacter implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(TableCompacter.class);

	public static interface CompacterListener
	{
		public void compacted(Iterable<SSTable> source, Iterable<SSTable> created);
	}

	private CompacterListener listener;
	private final Table table;
	private final Iterable<SSTable> tables;

	public TableCompacter(Table table)
	{
		this.table = table;
		this.tables = new HashSet<SSTable>(table.sstables);
	}

	public void addListener(final CompacterListener listener)
	{
		if (this.listener == null) {
			this.listener = listener;
		} else {
			final CompacterListener current = this.listener;
			this.listener = new CompacterListener() {

				@Override
				public void compacted(Iterable<SSTable> source, Iterable<SSTable> created)
				{
					current.compacted(source, created);
					listener.compacted(source, created);
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
		List<File> files = Collections.emptyList();
		try {
			files = SSTable.save(this.table.directory, this.table.name, new Documenter(this.tables));
		} catch (FileNotFoundException ball) {
			log.error("Problem while compacting", ball);
			log.error("Because of the error it is asumed that the compaction failed! Files that are created will stay on the system and loaded at a reboot of the database please perform a compaction after rebooting to clean up those files");
			return;
		} catch (IOException ball) {
			log.error("Problem while compacting", ball);
			log.error("Because of the error it is asumed that the compaction failed! Files that are created will stay on the system and loaded at a reboot of the database please perform a compaction after rebooting to clean up those files");
			return;
		}

		if (this.listener != null) {
			List<SSTable> created = new ArrayList<SSTable>(files.size());
			for (File f : files) {
				try {
					created.add(new SSTable(f));
				} catch (IOException ball) {
					log.error("Could not load SSTable: " + f, ball);
					log.error("Because of the error it is asumed that the compaction failed! Files that are created will stay on the system and loaded at a reboot of the database please perform a compaction after rebooting to clean up those files");
					return;
				}
			}
			this.listener.compacted(this.tables, created);
		}
	}
}
