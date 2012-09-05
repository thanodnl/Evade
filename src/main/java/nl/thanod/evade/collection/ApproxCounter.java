/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.UUID;

import nl.thanod.evade.store.bloom.Bloom;
import nl.thanod.evade.store.bloom.BloomFilter;
import nl.thanod.evade.store.bloom.BloomHasher;

/**
 * @author nilsdijk
 */
public class ApproxCounter
{
	private final int max;
	private BloomFilter bloom;
	private volatile int count;

	public ApproxCounter(Table table)
	{
		int count = 0;
		for (UUID uuid : table.uuids())
			count++;
		this.count = count;

		this.max = (int) (count * 1.5);
		this.bloom = BloomFilter.optimal(max, 3);

		for (UUID uuid : table.uuids())
			new Bloom<UUID>(uuid, BloomHasher.UUID).putIn(this.bloom);
	}

	public void add(UUID uuid)
	{
		if (new Bloom<UUID>(uuid, BloomHasher.UUID).putIn(this.bloom))
			this.count++;
	}

	public int get()
	{
		return this.count;
	}
}
