/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public class KDConstraint
{
	private final Comparable<? super Document> lower;
	private final Comparable<? super Document> upper;
	private final int d;

	public KDConstraint(Comparable<? super Document> lower, Comparable<? super Document> upper, int d)
	{
		this.lower = lower;
		this.upper = upper;
		this.d = d;
	}

	public boolean followLeft(KDNode node) // follow lower
	{
		if (this.lower == null || node.depth() % node.entry().getDimensions() != d)
			return true; // the tree is not separated on this axis
		return this.lower.compareTo(node.entry().get(this.d)) < 0;
	}

	public boolean followRight(KDNode node) // follow lower
	{
		if (this.upper == null || node.depth() % node.entry().getDimensions() != d)
			return true; // the tree is not separated on this axis
		return this.upper.compareTo(node.entry().get(this.d)) > 0;
	}

	public boolean test(KDEntry entry)
	{
		Document e = entry.get(this.d);
		return (this.lower == null || this.lower.compareTo(e) < 0) && (this.upper == null || this.upper.compareTo(e) > 0);
	}

	public static boolean followLeft(KDNode node, KDConstraint... constraints)
	{
		for (int i=constraints.length-1; i>=0; i--)
			if (!constraints[i].followLeft(node))
				return false;
		return true;
	}
	
	public static boolean followRight(KDNode node, KDConstraint... constraints)
	{
		for (int i=constraints.length-1; i>=0; i--)
			if (!constraints[i].followRight(node))
				return false;
		return true;
	}
	
	public static boolean test(KDEntry entry, KDConstraint... constraints)
	{
		for (int i=constraints.length-1; i>=0; i--)
			if (!constraints[i].test(entry))
				return false;
		return true;
	}
}
