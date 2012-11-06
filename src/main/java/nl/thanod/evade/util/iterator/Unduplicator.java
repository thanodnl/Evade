/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public class Unduplicator<T> extends Generator<T>
{
	public interface Predicate<T>
	{
		boolean shouldMerge(T t1, T t2);

		T merge(T t1, T t2);
	}

	private final Peekerator<? extends T> it;
	private final Predicate<T> predicate;

	/**
	 * @param it
	 *            a sorted iterator to ignore duplicates for
	 */
	public Unduplicator(Iterator<? extends T> it)
	{
		this(it, null);
	}

	/**
	 * @param it
	 *            a sorted iterator to ignore duplicates for
	 * @param predicate
	 *            a {@link Predicate} for merging objects
	 */
	public Unduplicator(Iterator<? extends T> it, Predicate<T> predicate)
	{
		this.it = new Peekerator<T>(it);
		this.predicate = predicate;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.util.iterator.Generator#generate()
	 */
	@Override
	protected T generate() throws NoSuchElementException
	{
		if (!it.hasNext())
			throw new NoSuchElementException();

		T current = it.next();
		if (this.predicate != null) {
			// when there is a predicate set use it to predict merges and execute the merge
			while (it.hasNext() && this.predicate.shouldMerge(current, it.peek())) {
				current = this.predicate.merge(current, it.next());
			}
		} else {
			// when no predicate is set remove all elements that are equal
			while (it.hasNext() && current.equals(it.peek())) {
				current = it.next();
			}
		}
		return current;
	}
}
