/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import nl.thanod.evade.database.Database;
import nl.thanod.evade.database.DatabaseConfiguration;
import nl.thanod.evade.util.Stopwatch;

/**
 * @author nilsdijk
 */
public class Counter
{
	public static void main(String... args) throws InterruptedException, ExecutionException
	{
		DatabaseConfiguration conf = new DatabaseConfiguration();
		conf.datadir = new File("data");

		final Database db = conf.loadDatabase();

		Stopwatch<FutureTask<Integer>> sw1 = Stopwatch.create(new FutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception
			{
				//				return db.getCollection("github").size();
				int c = 0;
				for (UUID uuid : db.getCollection("github").uuids())
					c++;
				return c;
			}
		}));
		sw1.run();
		System.out.println("counting " + sw1.delegate.get() + " entries took " + sw1);

		//		ApproxCounter counter = new ApproxCounter(db.getCollection("github"));
		//		System.out.println(counter.get() + " items");

		//		final BloomFilter filter = BloomFilter.optimal(sw1.delegate.get(), 5);
		//		Stopwatch<Runnable> sw2 = new Stopwatch<Runnable>(new Runnable() {
		//			@Override
		//			public void run()
		//			{
		//				for (UUID uuid : db.getCollection("github").uuids()) {
		//					Bloom<UUID> bloom = new Bloom<UUID>(uuid, BloomHasher.UUID);
		//					bloom.putIn(filter);
		//				}
		//			}
		//		});
		//
		//		for (int i = 0; i < 1000; i++) {
		//			sw2.run();
		//			System.out.println("building bloom filter took " + sw2);
		//		}
	}
}
