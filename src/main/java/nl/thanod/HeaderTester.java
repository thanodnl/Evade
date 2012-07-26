/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import nl.thanod.evade.store.Header;

/**
 * @author nilsdijk
 */
public class HeaderTester
{
	public static void main(String... args) throws IOException
	{
		File tmp = File.createTempFile("header", ".eva");
		tmp.deleteOnExit();

		Header header = new Header();
		header.put(Header.Type.DATA, 0);
		header.put(Header.Type.INDEX_DESC, 10);
		header.put(Header.Type.EOF, 15);
		System.out.println(header);

		RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
		header.writeAtEnd(raf);

		Header read = Header.readFromEnd(raf);
		System.out.println(read);
		raf.close();
	}
}
