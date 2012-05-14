/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public abstract class Generator<E> implements Iterator<E>, Iterable<E>
{

	private boolean next = false;
	private E value;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public final boolean hasNext()
	{
		if (next)
			return next;
		try {
			this.value = this.generate();
			this.next = true;
			return true;
		} catch (NoSuchElementException ball) {
			return false;
		}
	}

	@Override
	public final E next()
	{
		if (hasNext())
			return this.take();
		else
			throw new NoSuchElementException();
	}

	/**
	 * @return
	 */
	private E take()
	{
		if (!this.next)
			throw new NoSuchElementException();
		this.next = false;
		return this.value;
	}

	protected abstract E generate() throws NoSuchElementException;

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public final void remove()
	{
		throw new UnsupportedOperationException();
	}

}
