/**
 * 
 */
package nl.thanod.evade.util;

import java.util.Comparator;

/**
 * @author nilsdijk
 */
public abstract class ConvertedComparator<From, To> implements Comparator<From>
{
	private final Comparator<To> comparator;

	public ConvertedComparator()
	{
		this.comparator = null;
	}

	public ConvertedComparator(Comparator<To> comparator)
	{
		this.comparator = comparator;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(From o1, From o2)
	{
		To t1 = convert(o1);
		To t2 = convert(o2);
		if (this.comparator != null)
			return this.comparator.compare(t1, t2);
		else
			return ((Comparable<To>) t1).compareTo(t2);
	}

	protected abstract To convert(From from);
}
