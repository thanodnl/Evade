/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import java.io.*;

import nl.thanod.evade.store.Header;
import nl.thanod.evade.store.Header.Type;

/**
 * @author nilsdijk
 */
public class KDSerializer
{
	private final RandomAccessFile raf;
	private final DataOutput out;

	private final ByteArrayOutputStream bos;

	private int dataoffset;
	private int nodesoffset;

	private KDSerializer(KDTree tree, File file, boolean buffered) throws IOException
	{
		this.raf = new RandomAccessFile(file, "rw");

		if (buffered) {
			this.out = new DataOutputStream(this.bos = new ByteArrayOutputStream(4 * 1024));
		} else {
			this.out = this.raf;
			this.bos = null;
		}

		Header.reserve(raf, 2);
		Header header = new Header();

		this.dataoffset = (int) raf.getFilePointer();
		header.put(Type.DATA, this.dataoffset);

		raf.writeInt(0); // reserve the offset for the root node
		this.nodesoffset = (int) raf.getFilePointer();
		int start = serialize(tree.root());
		// eof
		header.put(Type.EOF, raf.getFilePointer());

		this.raf.seek(this.dataoffset);
		this.raf.writeInt(start); // write the offset for the rootnode as first in the data blob

		this.raf.seek(0);
		header.write(this.raf);

		this.raf.close();

		System.out.println("persisted to: " + file);
	}

	private int serialize(KDNode node) throws IOException
	{
		if (node == null)
			return -1;
		int left = serialize(node.left());
		int right = serialize(node.right());

		int start = (int) raf.getFilePointer() - this.nodesoffset;

		this.out.writeInt(left);
		this.out.writeInt(right);

		KDEntry entry = node.entry();

		KDEntry.write(entry, this.out);

		flush(); // be sure that everything is written to file
		return start;
	}

	private void flush() throws IOException
	{
		if (this.bos == null)
			return;

		this.raf.write(this.bos.toByteArray());
		this.bos.reset();
	}

	public static void serialize(File dir, String name, KDTree tree) throws IOException
	{
		File f;
		int fi = 0;
		do {
			f = new File(dir, name + (fi++) + ".kidx");
		} while (f.exists());

		f.getParentFile().mkdirs();

		new KDSerializer(tree, f, true);
	}
}
