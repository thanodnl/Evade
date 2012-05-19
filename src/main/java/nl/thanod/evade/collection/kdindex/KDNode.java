/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import java.util.*;

import nl.thanod.evade.util.iterator.Appenderator;
import nl.thanod.evade.util.iterator.Generator;
import nl.thanod.evade.util.iterator.SingleIterator;
import nl.thanod.evade.util.iterator.Sorterator;

/**
 * @author nilsdijk
 */
public class KDNode
{

	public final int depth;

	public KDNode parent;
	private final KDEntry entry;
	private final KDNode left;
	private final KDNode right;

	private KDNode(int depth, KDEntry entry, KDNode left, KDNode right)
	{
		this.depth = depth;
		this.entry = entry;
		this.left = left;
		this.right = right;
		if (this.left != null)
			this.left.parent = this;
		if (this.right != null)
			this.right.parent = this;
	}

	@Override
	public String toString()
	{
		return this.entry + "<left: " + (left != null ? left.entry : "none") + " | right: " + (right != null ? right.entry : "null") + ">";
	}

	public int size()
	{
		int c = 1;
		if (left != null)
			c += left.size();
		if (right != null)
			c += right.size();
		return c;
	}

	public KDNode left()
	{
		return this.left;
	}

	public KDNode right()
	{
		return this.right;
	}

	public KDEntry entry()
	{
		return this.entry;
	}

	public int depth()
	{
		return this.depth;
	}

	public static KDNode tree(List<KDEntry> data)
	{
		return tree(data, 0);
	}

	private static KDNode tree(List<KDEntry> data, int depth)
	{
		if (data.size() == 0)
			return null;
		int k = depth % data.get(0).getDimensions();
		Collections.sort(data, new KDEntry.Sorter(k));

		int median = data.size() / 2;
		KDNode left = (median > 0 ? tree(data.subList(0, median), depth + 1) : null);
		KDNode right = (median + 1 < data.size() ? tree(data.subList(median + 1, data.size()), depth + 1) : null);
		return new KDNode(depth, data.get(median), left, right);
	}

	public static Iterator<KDEntry> sorted(KDNode node, int d)
	{
		if (node == null)
			return null;

		if (node.depth() % node.entry().getDimensions() == d) {
			return new Appenderator<>(sorted(node.left(), d), new SingleIterator<>(node.entry()), sorted(node.right(), d));
		}
		return new Sorterator<>(new KDEntry.Sorter(d), sorted(node.left(), d), new SingleIterator<>(node.entry()), sorted(node.right(), d));
	}

	public static Iterator<KDEntry> filter(final KDNode root, final KDConstraint... constraints)
	{
		final Stack<KDNode> stack = new Stack<>();
		stack.push(root);
		return new Generator<KDEntry>() {
			int c = 0;

			@Override
			protected KDEntry generate() throws NoSuchElementException
			{
				KDNode node = null;
				do {
					if (stack.size() <= 0) {
						System.out.println("visited " + c + " nodes");
						throw new NoSuchElementException();
					}
					c++;
					node = stack.pop();

					// place children on the stack
					KDNode n;
					if (KDConstraint.followRight(node, constraints)) {
						n = node.right();
						if (n != null)
							stack.push(n);
					}
					if (KDConstraint.followLeft(node, constraints)) {
						n = node.left();
						if (n != null)
							stack.push(n);
					}

					if (KDConstraint.test(node.entry(), constraints))
						return node.entry();

				} while (true);
			}
		};
	}

	public static Iterator<KDEntry> all(KDNode root)
	{
		return filter(root);
	}
}
