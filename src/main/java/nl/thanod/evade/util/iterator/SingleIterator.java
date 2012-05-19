/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public class SingleIterator<E> implements Iterator<E>
{

	private E e;

	public SingleIterator(E e)
	{
		this.e = e;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.e != null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next()
	{
		E e = this.e;
		if (e == null)
			throw new NoSuchElementException();
		this.e = null;
		return e;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
