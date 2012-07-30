/**
 * 
 */
package nl.thanod.evade.util;

/**
 * @author nilsdijk
 */
public class Stopwatch<R extends Runnable> implements Runnable
{

	public final R delegate;

	private long took;

	public Stopwatch(R delegate)
	{
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		long took = System.nanoTime();
		this.delegate.run();
		took = System.nanoTime() - took;

		this.took = took;
	}

	@Override
	public String toString()
	{
		return this.took + "ns (" + this.took / 1000000.0 + "ms)";
	}

	public static <R extends Runnable> Stopwatch<R> create(R delegate)
	{
		return new Stopwatch<R>(delegate);
	}
}
