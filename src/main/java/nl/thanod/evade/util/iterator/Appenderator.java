/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.*;

/**
 * @author nilsdijk
 */
public class Appenderator<E> implements Iterator<E>
{

	private final Queue<Iterator<E>> iterators;

	public Appenderator(Iterator<E>... iterators)
	{
		this.iterators = new LinkedList<Iterator<E>>();
		for (Iterator<E> e : iterators)
			if (e != null)
				this.iterators.add(e);
	}

	public Appenderator(List<Iterator<E>> iterators)
	{
		this.iterators = new LinkedList<Iterator<E>>(iterators);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		while (this.iterators.size() > 0 && !this.iterators.peek().hasNext())
			this.iterators.poll();
		return this.iterators.size() > 0 && this.iterators.peek().hasNext();
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
		return this.iterators.peek().next();
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
