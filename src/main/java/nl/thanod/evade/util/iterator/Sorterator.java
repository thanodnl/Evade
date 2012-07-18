/**
 * 
 */
package nl.thanod.evade.util.iterator;

import java.util.*;

/**
 * @author nilsdijk
 */
public class Sorterator<E> implements Iterator<E>
{

	public final PriorityQueue<Peekerator<E>> source;

	public Sorterator(Comparator<? super E> comp, Iterator<E>... source)
	{
		this.source = new PriorityQueue<Peekerator<E>>(source.length, new Peekerator.Sorter<E>(comp));
		for (Iterator<E> it : source) {
			if (it == null)
				continue;
			if (it.hasNext())
				this.source.add(new Peekerator<E>(it));
		}
	}

	public Sorterator(Iterable<? extends Iterable<E>> source, Comparator<E> comp)
	{
		this.source = new PriorityQueue<Peekerator<E>>(64, new Peekerator.Sorter<E>(comp));
		for (Iterable<E> e : source) {
			if (e == null)
				continue;
			Iterator<E> it = e.iterator();
			if (it.hasNext())
				this.source.add(new Peekerator<E>(it));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.source.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next()
	{
		if (this.source.size() <= 0)
			throw new NoSuchElementException();
		Peekerator<E> it = this.source.poll();
		E e = it.next();
		if (it.hasNext())
			this.source.add(it);
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
