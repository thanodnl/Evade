/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public class Peekerator<E> implements Iterator<E>
{

	public static class Sorter<E> implements Comparator<Peekerator<E>>
	{

		private final Comparator<? super E> comp;

		public Sorter(Comparator<? super E> comp)
		{
			this.comp = comp;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Peekerator<E> o1, Peekerator<E> o2)
		{
			if (this.comp != null)
				return this.comp.compare(o1.peek(), o2.peek());
			return ((Comparable<E>) o1.peek()).compareTo(o2.peek());
		}

	}

	private final Iterator<? extends E> it;
	private E peek;
	private boolean peeked;

	public Peekerator(Iterator<? extends E> it)
	{
		this.it = it;
	}

	public E peek()
	{
		if (peeked)
			return peek;
		if (!it.hasNext())
			throw new NoSuchElementException();
		peek = it.next();
		peeked = true;
		return peek;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.peeked || this.it.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next()
	{
		if (this.peeked) {
			this.peeked = false;
			return this.peek;
		}
		return this.it.next();
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
