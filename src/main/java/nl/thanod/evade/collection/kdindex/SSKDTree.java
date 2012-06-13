/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import nl.thanod.evade.store.Header;
import nl.thanod.evade.store.Header.Type;
import nl.thanod.evade.util.ByteBufferDataInput;

/**
 * @author nilsdijk
 */
public class SSKDTree extends KDTree implements Closeable
{
	private class SSKDNode extends KDNode
	{
		private final int leftOffset;
		private final int rightOffset;

		private WeakReference<KDNode> left;
		private WeakReference<KDNode> right;

		private final KDEntry entry;

		public SSKDNode(int depth, int leftOffset, int rightOffset, KDEntry entry)
		{
			super(depth);

			this.leftOffset = leftOffset;
			this.rightOffset = rightOffset;
			this.entry = entry;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.kdindex.KDNode#left()
		 */
		@Override
		public KDNode left()
		{
			KDNode left;
			if (this.left != null) {
				left = this.left.get();
				if (left != null)
					return left;
			}
			left = read(this.leftOffset, this.depth + 1);
			if (left != null)
				this.left = new WeakReference<KDNode>(left);
			return left;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.kdindex.KDNode#right()
		 */
		@Override
		public KDNode right()
		{
			KDNode right;
			if (this.right != null) {
				right = this.right.get();
				if (right != null)
					return right;
			}
			right = read(this.rightOffset, this.depth + 1);
			if (right != null)
				this.right = new WeakReference<KDNode>(right);
			return right;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.kdindex.KDNode#entry()
		 */
		@Override
		public KDEntry entry()
		{
			return this.entry;
		}
	}

	private final RandomAccessFile raf;
	private final File file;

	private final int rootOffset;
	private final ByteBuffer data;

	public SSKDTree(File file) throws IOException
	{
		this.file = file;
		this.raf = new RandomAccessFile(file, "r");

		Header header = Header.read(this.raf);
		ByteBuffer map = header.map(this.raf, Type.DATA);
		this.rootOffset = map.getInt();
		this.data = map.slice();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.kdindex.KDTree#root()
	 */
	@Override
	public KDNode root()
	{
		return read(this.rootOffset, 0);
	}

	protected KDNode read(int offset, int depth)
	{
		if (offset < 0)
			return null;

		ByteBuffer map = this.data.duplicate();
		map.position(offset);

		try {
			return new SSKDNode(depth, map.getInt(), map.getInt(), KDEntry.read(new ByteBufferDataInput(map)));
		} catch (IOException ball) {
			// should never fail
			// TODO log exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close()
	{
		try {
			this.raf.close();
		} catch (IOException ball) {
			System.err.println("Could not close " + this.file);
			ball.printStackTrace();
		}
	}

	@Override
	protected void finalize()
	{
		// close the file when the object is disposed
		this.close();
	}

}
