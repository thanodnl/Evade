/**
 * 
 */
package nl.thanod;

import java.io.*;

import com.ning.compress.lzf.LZFEncoder;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.visitor.DocumentSerializerVisitor;

/**
 * @author nilsdijk
 */
public class Compressor
{
	public static void main(String... args) throws IOException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		Database db = conf.loadDatabase();
		Table t = db.getCollection("github");

		File log = new File("log" + System.currentTimeMillis() + ".csv"); 
		FileOutputStream fos = new FileOutputStream(log);
//		Writer logger = new OutputStreamWriter(fos);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
//		logger.write("original;compressed;\n");
		long oc = 0;
		long cc = 0;
		long c = 0;
		for (Document.Entry e : t) {
			try {
				dos.writeLong(e.id.getMostSignificantBits());
				dos.writeLong(e.id.getLeastSignificantBits());
			} catch (IOException ball) {
				ball.printStackTrace();
			};
			e.doc.accept(DocumentSerializerVisitor.VERSIONED, dos);
			
			byte[] orig = bos.toByteArray();
			byte[] compressed = LZFEncoder.encode(orig);
//			logger.write(orig.length + ";" + compressed.length + ";" + "\n");
			
			oc += orig.length;
			cc += compressed.length;
			
			bos.reset();
			
			if (++c%100000==0){
				System.out.println("saved " + (oc - cc) + " bytes after " + c + " items");
			}
		}
		System.out.println("csv: " + log.getAbsolutePath());
		System.out.println("saved " + (oc - cc) + " bytes");
	}
}
