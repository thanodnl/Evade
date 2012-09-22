/**
 * 
 */
package nl.thanod;

import java.io.File;

import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;

/**
 * @author nilsdijk
 */
public class SSTableStats implements SSTable.Visitor
{
	long compressed = 0;
	long uncompressed = 0;

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.collection.SSTable.Visitor#visit(nl.thanod.evade.collection
	 * .SSTable)
	 */
	@Override
	public void visit(SSTable table)
	{
		this.uncompressed += table.header.uncompressed;
		this.compressed += table.header.compressed;
	}

	@Override
	public String toString()
	{
		return "SSTableStats [uncompressed=" + this.uncompressed + ", compressed=" + this.compressed + "]";
	}

	public static void main(String... args)
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		Table t = db.getCollection("github_small");
		
		SSTableStats stats = new SSTableStats();
		t.accept(stats);
		System.out.println(stats);
	}
}
