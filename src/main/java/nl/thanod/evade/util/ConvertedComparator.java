/**
 * 
 */
package nl.thanod.evade.util;

import java.util.Comparator;

/**
 * @author nilsdijk
 *
 */
public abstract class ConvertedComparator<From, To extends Comparable<? super To>> implements Comparator<From>
{
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(From o1, From o2)
	{
		To t1 = convert(o1);
		To t2 = convert(o2);
		return t1.compareTo(t2);
	}

	protected abstract To convert(From from);
}
