/**
 * 
 */
package nl.thanod.evade.store;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.EnumSet;
import java.util.Set;

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
		BLOOM(4),
		INDEX_DESC(5),
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

	public enum Flags
	{
		LZF;
	}

	public static final class Entry
	{
		public final Type type;
		public final long start;
		public final EnumSet<Flags> flags;

		protected Entry next;

		public Entry(Header.Type type, long start, EnumSet<Flags> flags)
		{
			this.type = type;
			this.start = start;
			if (flags == null)
				this.flags = EnumSet.noneOf(Flags.class);
			else
				this.flags = flags;
		}

		public Entry next()
		{
			return this.next;
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
				long size = next.start - this.start;
				if (size <= 0)
					return null;
				return channel.map(MapMode.READ_ONLY, this.start, size);
			} catch (IOException ball) {
				return null;
			}
		}

		@Override
		public String toString()
		{
			return this.type.name() + ":" + this.start + ":" + this.flags;
		}
	}

	private Entry first;
	private Entry last;

	private int count = 0;
	public final int version;

	public long uncompressed = -1;
	public long compressed = -1;

	public Header(int version)
	{
		this.version = version;
	}

	public void put(Type type, long position)
	{
		this.put(type, position, (EnumSet<Flags>) null);
	}

	public void put(Type type, long position, Flags... flags)
	{
		EnumSet<Flags> set;
		if (flags.length > 0)
			set = EnumSet.of(flags[0], flags);
		else
			set = EnumSet.noneOf(Flags.class);
		put(type, position, set);
	}

	public void put(Type type, long position, EnumSet<Flags> flags)
	{
		Entry header = new Entry(type, position, flags);
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
		Entry e = get(type);
		if (e == null)
			return null;
		return e.map(raf.getChannel());
	}

	public static Header read(DataInput in) throws IOException
	{
		Header ih = new Header(0);
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			Type t = Type.byCode(in.readByte() & 0xFF);
			ih.put(t, in.readInt());
		}
		return ih;
	}

	public static Header readFromEnd(RandomAccessFile in) throws IOException
	{
		long pos = in.length();

		Header ih = new Header(1);

		in.seek(pos -= 4);
		int size = in.readInt();

		in.seek(pos -= 5 * size);
		for (int i = 0; i < size; i++) {
			Type t = Type.byCode(in.readByte() & 0xFF);
			ih.put(t, in.readInt());
		}

		return ih;
	}

	@Deprecated
	public void write(DataOutput out) throws IOException
	{
		out.writeInt(count);
		Entry e = this.first;
		for (int i = 0; i < count; i++) {
			out.writeByte(e.type.code);
			out.writeInt((int) e.start);
			e = e.next;
		}
	}

	public void writeAtEnd(DataOutput out) throws IOException
	{
		Entry e = this.first;
		for (int i = 0; i < count; i++) {
			out.writeByte(e.type.code);
			out.writeInt((int) e.start);
			e = e.next;
		}
		out.writeInt(count);
	}

	private static long fromFlags(Set<Flags> flags)
	{
		long l = 0;
		for (Flags flag : flags) {
			if (flag.ordinal() >= Long.SIZE)
				throw new IndexOutOfBoundsException("Error on " + flag + "#" + flag.ordinal() + " Flags cant have a bigger ordinal than " + (Long.SIZE - 1));
			l |= 1 << flag.ordinal();
		}
		return l;
	}

	private static EnumSet<Flags> fromLong(long flags)
	{
		EnumSet<Flags> set = EnumSet.noneOf(Flags.class);
		for (Flags flag : Flags.values()) {
			if (flag.ordinal() >= Long.SIZE)
				throw new IndexOutOfBoundsException("Error on " + flag + "#" + flag.ordinal() + " Flags cant have a bigger ordinal than " + (Long.SIZE - 1));
			if ((flags & (1 << flag.ordinal())) != 0)
				set.add(flag);
		}
		return set;
	}

	public void write2(DataOutput out) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeInt(this.version);

		// from version 2
		if (this.version >= 2) {
			dos.writeLong(this.uncompressed);
			dos.writeLong(this.compressed);
		}

		// all older versions
		Entry e = this.first;
		for (int i = 0; i < count; i++) {
			dos.writeByte(e.type.code);
			dos.writeLong(e.start);
			dos.writeLong(fromFlags(e.flags));
			e = e.next;
		}

		// write stuff to destination
		byte[] header = bos.toByteArray();
		out.write(header);
		out.writeInt(header.length);
	}

	public static Header read2(RandomAccessFile in) throws IOException
	{
		long pos = in.length();

		in.seek(pos -= 4);
		int size = in.readInt();

		in.seek(pos -= size);

		byte[] buf = new byte[size];
		in.read(buf);

		DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));

		Header ih = new Header(din.readInt());
		switch (ih.version) {
			case 2:
				ih.uncompressed = din.readLong();
				ih.compressed = din.readLong();
				//$FALL-THROUGH$
			default:
				while (din.available() > 0) {
					Type t = Type.byCode(din.readByte() & 0xFF);
					long position = din.readLong();
					EnumSet<Flags> flags = fromLong(din.readLong());
					ih.put(t, position, flags);
				}
		}
		return ih;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Header[");
		sb.append("version:");
		sb.append(this.version);
		sb.append(',');
		sb.append("uncompressed:");
		sb.append(this.uncompressed);
		sb.append(',');
		sb.append("compressed:");
		sb.append(this.compressed);

		Entry e = this.first;
		while (e != null) {
			sb.append(',');
			sb.append(e);
			e = e.next;
		}
		sb.append(']');

		return sb.toString();
	}

	public static void reserve(DataOutput out, int entryCount) throws IOException
	{
		out.writeInt(0);
		for (int i = 0; i < entryCount; i++) {
			out.writeByte(0);
			out.writeInt(0);
		}
	}

	/**
	 * @param data
	 * @return
	 */
	public Entry get(Type type)
	{
		Entry e = this.first;
		while (e != null && e.type != type)
			e = e.next;
		return e;
	}
}
