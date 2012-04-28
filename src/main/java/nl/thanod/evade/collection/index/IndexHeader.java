/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * @author nilsdijk
 */
public class IndexHeader
{
	private int datapos;
	private int sindexpos;
	private int uuidpos;
	private int eofpos;

	public IndexHeader()
	{
		this.datapos = 0;
		this.sindexpos = 0;
		this.uuidpos = 0;
		this.eofpos = 0;
	}

	/**
	 * @param readInt
	 * @param readInt2
	 * @param readInt3
	 * @param readInt4
	 */
	private IndexHeader(int datapos, int sindexpos, int uuidpos, int eofpos)
	{
		this.setEOFPosition(eofpos);
		this.setUUIDIndexPosition(uuidpos);
		this.setSortedIndexPosition(sindexpos);
		this.setDataPosition(datapos);
	}

	public void setDataPosition(int datapos)
	{
		this.datapos = datapos;
		if (this.sindexpos < datapos)
			this.setSortedIndexPosition(datapos);
	}

	public void setSortedIndexPosition(int sindexpos)
	{
		this.sindexpos = sindexpos;
		if (this.uuidpos < sindexpos)
			this.setUUIDIndexPosition(sindexpos);
	}

	public void setUUIDIndexPosition(int uuidpos)
	{
		this.uuidpos = uuidpos;
		if (this.eofpos < uuidpos)
			this.setEOFPosition(uuidpos);
	}

	public void setEOFPosition(int eofpos)
	{
		this.eofpos = eofpos;
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(this.datapos);
		out.writeInt(this.sindexpos);
		out.writeInt(this.uuidpos);
		out.writeInt(this.eofpos);
	}

	public static IndexHeader read(DataInput in) throws IOException
	{
		IndexHeader ih = new IndexHeader(in.readInt(), in.readInt(), in.readInt(), in.readInt());
		return ih;
	}

	/**
	 * @param raf
	 * @throws IOException 
	 */
	public MappedByteBuffer mapData(RandomAccessFile raf) throws IOException
	{
		return mapData(raf.getChannel());
	}

	/**
	 * @param channel
	 * @return
	 * @throws IOException 
	 */
	private MappedByteBuffer mapData(FileChannel channel) throws IOException
	{
		return channel.map(MapMode.READ_ONLY, this.datapos, this.sindexpos - this.datapos);
	}

}
