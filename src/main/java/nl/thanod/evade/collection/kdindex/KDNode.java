/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import nl.thanod.evade.util.iterator.Appenderator;
import nl.thanod.evade.util.iterator.Generator;
import nl.thanod.evade.util.iterator.SingleIterator;
import nl.thanod.evade.util.iterator.Sorterator;

/**
 * @author nilsdijk
 */
public abstract class KDNode
{

	public final int depth;

	/**
	 * 
	 */
	public KDNode(int depth)
	{
		super();
		this.depth = depth;
	}

	public abstract KDNode left();

	public abstract KDNode right();

	public abstract KDEntry entry();

	public static Iterator<KDEntry> sorted(KDNode node, int d)
	{
		if (node == null)
			return null;

		if (node.depth % node.entry().getDimensions() == d) {
			return new Appenderator<>(sorted(node.left(), d), new SingleIterator<>(node.entry()), sorted(node.right(), d));
		}
		return new Sorterator<>(new KDEntry.Sorter(d), sorted(node.left(), d), new SingleIterator<>(node.entry()), sorted(node.right(), d));
	}

	public static Iterator<KDEntry> filter(final KDTree tree, final KDConstraint... constraints)
	{
		final Stack<KDNode> stack = new Stack<>();
		stack.push(tree.root());
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

	public static Iterator<KDEntry> all(KDTree tree)
	{
		return filter(tree);
	}

}