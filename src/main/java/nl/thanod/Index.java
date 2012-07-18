/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.collection.index.IndexSerializer;
import nl.thanod.evade.document.DocumentPath;

/**
 * @author nilsdijk
 */
public class Index
{
	public static void main(String... args) throws IOException
	{

		String name = "github";
		File dir = new File("data", name);
		Table t = Table.load(dir, name);

		System.out.println(t.iterator().next());
		DocumentPath path = new DocumentPath("actor_attributes", "login");
		IndexSerializer.persistSortedIndex(t, new IndexDescriptor(path), dir, name);
	}
}
