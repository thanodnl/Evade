/**
 * 
 */
package nl.thanod.evade.util;

import java.util.*;

/**
 * @author nilsdijk
 */
public class Sorterator<E> extends Generator<E>
{

	public final List<Peekerator<E>> source;
	public final Comparator<Peekerator<E>> comp;

	public Sorterator(Iterable<? extends Iterable<E>> source, Comparator<E> comp)
	{
		this.source = new ArrayList<Peekerator<E>>();
		for (Iterable<E> e : source)
			this.source.add(new Peekerator<E>(e.iterator()));
		this.comp = new Peekerator.Sorter<E>(comp);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.util.Generator#generate()
	 */
	@Override
	protected E generate() throws NoSuchElementException
	{
		if (this.source.size() <= 0)
			throw new NoSuchElementException();
		Collections.sort(this.source, this.comp);
		E e = this.source.get(0).next();
		if (!this.source.get(0).hasNext())
			this.source.remove(0);
		return e;
	}

}
