/**
 * 
 */
package nl.thanod.evade;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

import nl.thanod.evade.UUIDIndex.Entry;

/**
 * @author nilsdijk
 */
public class UUIDIndex {
	public static Random RANDY = new Random(System.currentTimeMillis());

	public static final int COUNT = 1000000;
	private final MappedByteBuffer buffer;

	public static class Entry {
		public final UUID uuid;
		public final long position;

		Entry(UUID uuid, long position) {
			this.uuid = uuid;
			this.position = position;
		}

		@Override
		public String toString() {
			return uuid + ":" + this.position;
		}
	}

	public UUIDIndex(File f) throws IOException {
		FileChannel ch = new FileInputStream(f).getChannel();
		this.buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());
	}

	public int count() {
		return buffer.capacity() / 24;
	}

	public Entry getRandom() {
		return get(RANDY.nextInt(count()));
	}

	public Entry find(UUID uuid) {
		return doFind(uuid, 0, count());
	}

	private Entry doFind(UUID uuid, int min, int max) {
		while (min <= max) {
			int mid = (max - min) / 2 + min;
			Entry found = get(mid);
			if (found.uuid.equals(uuid))
				return found;
			if (mid == min && mid == max)
				break;

			if (found.uuid.compareTo(uuid) < 0)
				min = mid + 1;
			else
				max = mid - 1;
		}
		return null;
	}

	/**
	 * @param nextInt
	 * @return
	 */
	public Entry get(int n) {
		buffer.position(n * 24);
		UUID id = new UUID(buffer.getLong(), buffer.getLong());
		return new Entry(id, buffer.getLong());
	}

	public static void main(String... args) throws IOException {
		File f = new File("uuid.idx");
		//		create(f);
		//		System.out.println("created");
		test(f);
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	private static void test(File f) throws IOException {
		UUIDIndex index = new UUIDIndex(f);

		findRandom(index);
		findRandom(index);
		findRandom(index);
		findRandom(index);
		findRandom(index);

		long start = System.nanoTime();
		int c = 0;
		for (int i = 0; i < index.count(); i++) {
			Entry find = index.get(i);
			Entry found = index.find(find);
			if (found == null)
				System.err.println("#" + i + " index " + find + " not found");
			c++;
		}
		long took = System.nanoTime() - start;
		System.out.println("found all " + c + " in " + (took / 1000000.0) + "ms");
	}

	/**
	 * @param index
	 */
	private static void findRandom(UUIDIndex index) {
		Entry find = index.getRandom();
		System.out.println(find);
		findMe(index, find.uuid);
	}

	private static void findMe(UUIDIndex index, UUID id) {
		long start = System.nanoTime();
		Entry found = index.find(id);
		long took = System.nanoTime() - start;
		System.out.println(found);
		System.out.println(took / 1000000.0 + "ms");
	}

	public Entry find(Entry find) {
		return doFind(find.uuid, 0, count());
	}

	public static void create(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		DataOutputStream dos = new DataOutputStream(fos);

		List<UUID> uuids = new ArrayList<UUID>(COUNT);
		for (int i = 0; i < COUNT; i++) {
			uuids.add(UUID.randomUUID());
		}

		Collections.shuffle(uuids);
		TreeMap<UUID, Long> map = new TreeMap<UUID, Long>();
		for (int i = 0; i < uuids.size(); i++) {
			map.put(uuids.get(i), new Long(i));
		}
		uuids = null;
		UUID prev = null;
		for (Map.Entry<UUID, Long> e : map.entrySet()) {
			assert prev == null || prev.compareTo(e.getKey()) == -1 : "Not the correct order";
			dos.writeLong(e.getKey().getMostSignificantBits());
			dos.writeLong(e.getKey().getLeastSignificantBits());
			dos.writeLong(e.getValue());
			prev = e.getKey();
		}
		dos.close();
	}
}
