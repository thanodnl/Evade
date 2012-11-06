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

	/**
	 * @param ids
	 */
	public Sorterator(Iterable<? extends Iterable<E>>... sources)
	{
		// make a Sorterator using the natural ordering of E
		this(null, sources);
	}

	public Sorterator(Comparator<E> comp, Iterable<? extends Iterable<E>>... sources)
	{
		this.source = new PriorityQueue<Peekerator<E>>(64, new Peekerator.Sorter<E>(comp));
		for (Iterable<? extends Iterable<E>> source : sources) {
			for (Iterable<E> e : source) {
				if (e == null)
					continue;
				Iterator<E> it = e.iterator();
				if (it.hasNext())
					this.source.add(new Peekerator<E>(it));
			}
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

		// remove the first Peekerator from the sorted sources
		Peekerator<E> it = this.source.poll();

		// take the fisrt item
		E e = it.next();

		// re-add the Peekerator if it is not empty, re-adding it will also sort it in the heap of the PriorityQueue
		if (it.hasNext())
			this.source.add(it);

		// return the element coming from the front of the queue
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
