/**
 * 
 */
package nl.thanod;

import java.io.*;
import java.util.UUID;

import nl.thanod.evade.collection.Memtable;
import nl.thanod.evade.collection.SSTable;
import nl.thanod.evade.document.DocumentBuilder;

/**
 * @author nilsdijk
 */
public class Names
{
	public static void main(String... args) throws IOException
	{
		File f = new File("data", "namelist.txt");
		System.out.println(f.getAbsoluteFile());
		InputStream in = new FileInputStream(f);
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		int c = 0;

		Memtable table = new Memtable();
		while ((line = r.readLine()) != null) {
			table.update(UUID.randomUUID(), DocumentBuilder.start(System.currentTimeMillis()).put("name", line).make());
			c++;
			if (c >= 10000){
				System.out.println(SSTable.save(table));
				table = new Memtable();
				c = 0;
			}
		}
		System.out.println(SSTable.save(table));


		System.out.println("lines: " + c);
	}
}
