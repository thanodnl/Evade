/**
 * 
 */
package nl.thanod;

import java.io.*;
import java.util.UUID;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.document.DocumentBuilder;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.modifiers.LowerCase;

/**
 * @author nilsdijk
 */
public class Names
{
	public static void main(String... args) throws IOException
	{
		File data = new File("data", "names");
		File f = new File(data, "namelist.txt");
		System.out.println(f.getAbsoluteFile());
		InputStream in = new FileInputStream(f);
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		int c = 0;

		Table table = Table.load(data, "names");
		table.maintainIndex(new DocumentPath("name"), new LowerCase());
		
		while ((line = r.readLine()) != null) {
			table.update(UUID.randomUUID(), DocumentBuilder.start(System.currentTimeMillis()).put("name", line).make());
			c++;
		}
		table.persist();

		System.out.println("lines: " + c);
	}
}
