/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public class LazyIterator<E> implements Iterator<E>
{

	private final Iterable<E> source;
	private Iterator<E> it;

	public LazyIterator(Iterable<E> source)
	{
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		if (it == null)
			it = this.source.iterator();
		return it.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next()
	{
		if (!hasNext())
			throw new NoSuchElementException();
		return this.it.next();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		this.it.remove();
	}

}
