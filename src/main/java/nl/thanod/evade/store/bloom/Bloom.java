/**
 * 
 */
package nl.thanod.evade.store.bloom;

import java.util.Arrays;

/**
 * @author nilsdijk
 */
public class Bloom<Data>
{
	public final Data data;
	private final BloomHasher<Data> hasher;

	private int[] hashes = new int[0];

	public Bloom(Data data, BloomHasher<Data> hasher)
	{
		this.data = data;
		this.hasher = hasher;
	}

	public boolean containedBy(BloomFilter filter)
	{
		ensureHashes(filter.hashCount());
		return filter.contains(this.hashes);
	}

	public boolean putIn(BloomFilter filter)
	{
		ensureHashes(filter.hashCount());
		return filter.put(this.hashes);
	}

	/**
	 * @param hashCount
	 */
	private void ensureHashes(int hashCount)
	{
		if (hashCount <= this.hashes.length)
			return;
		int offset = this.hashes.length;
		this.hashes = Arrays.copyOf(this.hashes, hashCount);
		this.hasher.bloom(this.data, this.hashes, offset, hashCount - offset);
	}
}
