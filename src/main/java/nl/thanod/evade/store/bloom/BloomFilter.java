/**
 * 
 */
package nl.thanod.evade.store.bloom;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author nilsdijk
 */
public class BloomFilter
{
	private final ByteBuffer backed;
	private final int bits;
	private final int hashes;

	private BloomFilter(int hashes, int bits, ByteBuffer backed)
	{
		this.hashes = hashes;
		this.bits = bits;
		this.backed = backed;
	}

	public BloomFilter(int hashes, int bits)
	{
		this.hashes = hashes;
		int bytes = bits / 8 + (bits % 8 > 0 ? 1 : 0);
		this.bits = bits;

		// big bloom filters should not reside in heap
		// that could trigger OOM exceptions
		if (bytes <= 1024 * 1024) // current threshold is arbitrary set at 1MB
			this.backed = ByteBuffer.allocate(bytes);
		else
			this.backed = ByteBuffer.allocateDirect(bytes);
	}

	public synchronized boolean put(int[] hashes)
	{
		boolean set = false;
		if (this.hashes > hashes.length)
			throw new RuntimeException("Not enough hashes supplied for this bloomfilter");
		for (int i = 0; i < this.hashes; i++)
			set |= put(hashes[i]);
		return set;
	}

	private boolean put(int hash)
	{
		long bit = hash & 0xFFFFFFFFL;
		bit %= this.bits;
		int index = (int) (bit / 8);
		bit %= 8;

		byte b = this.backed.get(index);
		bit = 1 << bit;
		if ((b & bit) == 0)
			return false;
		b |= bit;
		this.backed.put(index, b);
		return true;
	}

	public boolean contains(int[] hashes)
	{
		if (this.hashes > hashes.length)
			// maybe we need to return true here all the time to prevent false negatives or exceptions 
			throw new RuntimeException("Not enough hashes supplied for this bloomfilter");
		for (int i = 0; i < this.hashes; i++)
			if (!contains(hashes[i]))
				return false;
		return true;
	}

	private boolean contains(int hash)
	{
		long bit = hash & 0xFFFFFFFFL;
		bit %= this.bits;
		int index = (int) (bit / 8);
		bit %= 8;

		byte b = this.backed.get(index);
		return (b & (1 << bit)) != 0;
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(this.hashes);
		out.writeInt(this.bits);

		byte[] buffer = new byte[this.backed.capacity()];
		this.backed.position(0);
		this.backed.get(buffer);
		this.backed.position(0);

		out.write(buffer);
	}

	/**
	 * Creates a {@link BloomFilter} with a suitable size for a given number of
	 * elements and a given number of hashfunctions.
	 * <p>
	 * The formula used for this is m = n(k/ln2). This is taken from the formula
	 * posted on wikipedia to calculate an appropriate number of hash functions
	 * given the number of items to store and the number of bits. ( k =
	 * (m/n)ln2)
	 * @param items
	 *            the number of items to store in this BloomFilter. This is not
	 *            a hard limit but it should be an educated guess to create a
	 *            suitable sized {@link BloomFilter}
	 */
	public static BloomFilter optimal(int items, int hashes)
	{
		double bitsPerItem = hashes / (1 - Math.log10(2)); // hashes / ln(2)
		int bits = (int) (bitsPerItem * items);

		return new BloomFilter(hashes, bits);
	}

	public static BloomFilter fromBuffer(ByteBuffer buffer)
	{
		if (buffer == null)
			return null;
		return new BloomFilter(buffer.getInt(), buffer.getInt(), buffer.slice());
	}

	/**
	 * @return
	 */
	public int hashCount()
	{
		return this.hashes;
	}
}
