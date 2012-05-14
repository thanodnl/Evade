/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.Memtable;
import nl.thanod.evade.collection.index.Memdex;
import nl.thanod.evade.collection.index.SSIndex;

/**
 * @author nilsdijk
 */
public class IndexCompacter
{
	public static void main(String... args) throws IOException
	{
		File dir = new File("data", "github_index");
		File[] indexFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".idx");
			}
		});

		List<SSIndex> indices = new ArrayList<SSIndex>(indexFiles.length);

		for (File idxFile : indexFiles)
			indices.add(new SSIndex(idxFile));

		Memdex.compactIndices(indices);
	}
}
