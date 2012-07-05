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

	public final ArrayList<Peekerator<E>> source;
	public final Comparator<Peekerator<E>> comp;

	public Sorterator(Comparator<? super E> comp, Iterator<E>... source)
	{
		this.source = new ArrayList<Peekerator<E>>(source.length);
		for (Iterator<E> it : source) {
			if (it == null)
				continue;
			if (it.hasNext())
				this.source.add(new Peekerator<E>(it));
		}
		this.comp = new Peekerator.Sorter<E>(comp);
	}

	public Sorterator(Iterable<? extends Iterable<E>> source, Comparator<E> comp)
	{
		this.source = new ArrayList<Peekerator<E>>();
		for (Iterable<E> e : source) {
			Iterator<E> it = e.iterator();
			if (it.hasNext())
				this.source.add(new Peekerator<E>(it));
		}
		this.comp = new Peekerator.Sorter<E>(comp);
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
		Collections.sort(this.source, this.comp);
		E e = this.source.get(0).next();
		if (!this.source.get(0).hasNext())
			this.source.remove(0);
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
