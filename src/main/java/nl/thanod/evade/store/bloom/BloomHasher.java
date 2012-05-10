/**
 * 
 */
package nl.thanod.evade.store.bloom;

import java.util.UUID;

import nl.thanod.evade.util.hash.MurmurHash3;

/**
 * @author nilsdijk
 */
public abstract class BloomHasher<Data>
{
	public static final BloomHasher<UUID> UUID = new BloomHasher<UUID>() {
		@Override
		protected byte[] data(UUID data)
		{
			byte[] b = new byte[16];
			long d = data.getMostSignificantBits();
			b[0] = (byte) ((d >> 56) & 0xFF);
			b[1] = (byte) ((d >> 48) & 0xFF);
			b[2] = (byte) ((d >> 40) & 0xFF);
			b[3] = (byte) ((d >> 32) & 0xFF);
			b[4] = (byte) ((d >> 24) & 0xFF);
			b[5] = (byte) ((d >> 16) & 0xFF);
			b[6] = (byte) ((d >> 8) & 0xFF);
			b[7] = (byte) ((d >> 0) & 0xFF);

			d = data.getLeastSignificantBits();
			b[8] = (byte) ((d >> 56) & 0xFF);
			b[9] = (byte) ((d >> 48) & 0xFF);
			b[10] = (byte) ((d >> 40) & 0xFF);
			b[11] = (byte) ((d >> 32) & 0xFF);
			b[12] = (byte) ((d >> 24) & 0xFF);
			b[13] = (byte) ((d >> 16) & 0xFF);
			b[14] = (byte) ((d >> 8) & 0xFF);
			b[15] = (byte) ((d >> 0) & 0xFF);
			return b;
		}
	};

	public void bloom(Data data, int[] hashes)
	{
		this.bloom(data, hashes, 0, hashes.length);
	}

	public void bloom(Data data, int[] hashes, int offset, int length)
	{
		if (length <= 0)
			return;
		int seed = 0;
		if (offset > 0)
			seed = hashes[offset - 1];
		byte[] bytes = data(data);
		for (int i = 0; i < length; i++)
			hashes[offset + i] = seed = MurmurHash3.murmurhash3_x86_32(bytes, 0, bytes.length, seed);
	}

	protected abstract byte[] data(Data data);
}
