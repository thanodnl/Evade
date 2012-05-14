/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.index.IndexSerializer;
import nl.thanod.evade.collection.index.SSIndex;

/**
 * @author nilsdijk
 */
public class IndexCompacter
{
	public static void main(String... args) throws IOException
	{
		final String name = "github";
		File dir = new File("data", name);
		File[] indexFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String file)
			{
				return file.startsWith(name) && file.endsWith(".idx");
			}
		});

		List<SSIndex> indices = new ArrayList<SSIndex>(indexFiles.length);

		for (File idxFile : indexFiles)
			indices.add(new SSIndex(idxFile));

		// merge all indices
		IndexSerializer.compactIndices(dir, name, indices);
		
		// remove the old index files
		for (File idxFile : indexFiles){
			try {
				idxFile.delete();
				System.out.println("deleted: " + idxFile);
			} catch(Exception ball){
				ball.printStackTrace();
				idxFile.deleteOnExit();
			}	
		}
	}
}
