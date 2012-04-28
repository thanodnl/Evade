/**
 * 
 */
package nl.thanod.evade.store;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * @author nilsdijk
 */
public class Header
{
	public enum Type
	{
		DATA(1),
		SORTED_INDEX(2),
		UUID_INDEX(3),
		EOF(0xFF);

		public final int code;

		private Type(int code)
		{
			this.code = code;
		}

		public static Type byCode(int code)
		{
			for (Type t : Type.values())
				if (t.code == code)
					return t;
			return null;
		}
	}

	public static final class Entry
	{
		public final Type type;
		public final int start;

		protected Entry next;

		public Entry(Header.Type type, int start)
		{
			this.type = type;
			this.start = start;
		}

		public void setNext(Header.Entry next)
		{
			this.next = next;
		}

		protected ByteBuffer map(FileChannel channel)
		{
			if (this.next == null)
				throw new IllegalStateException("Can't map the last file entry");
			try {
				return channel.map(MapMode.READ_ONLY, this.start, next.start - this.start);
			} catch (IOException ball) {
				return null;
			}
		}

	}

	private Entry first;
	private Entry last;

	private int count = 0;

	public Header()
	{
	}

	public void put(Type type, int position)
	{
		Entry header = new Entry(type, position);
		if (this.last != null) {
			this.last.setNext(header);
			this.last = header;
		} else {
			this.first = this.last = header;
		}
		count++;
	}

	public ByteBuffer map(RandomAccessFile raf, Type type)
	{
		Entry e = this.first;
		while (e != null && e.type != type)
			e = e.next;
		if (e == null)
			return null;
		return e.map(raf.getChannel());
	}

	public static Header read(DataInput in) throws IOException
	{
		Header ih = new Header();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			Type t = Type.byCode(in.readByte() & 0xFF);
			ih.put(t, in.readInt());
		}
		return ih;
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(count);
		Entry e = this.first;
		for (int i = 0; i < count; i++) {
			out.writeByte(e.type.code);
			out.writeInt(e.start);
			e = e.next;
		}
	}

	public static void reserve(DataOutput out, int entryCount) throws IOException
	{
			out.writeInt(0);
			for (int i = 0; i < entryCount; i++) {
				out.writeByte(0);
				out.writeInt(0);
			}
	}
}
