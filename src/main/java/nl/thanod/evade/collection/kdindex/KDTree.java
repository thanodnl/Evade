/**
 * 
 */
package nl.thanod.evade.collection.kdindex;

import java.util.Collections;
import java.util.List;

/**
 * @author nilsdijk
 */
public abstract class KDTree
{

	public abstract KDNode root();

	public static KDTree tree(List<KDEntry> data)
	{
		final KDNode root = tree(data, 0);
		return new KDTree() {

			@Override
			public KDNode root()
			{
				return root;
			}
		};
	}

	private static KDMemNode tree(List<KDEntry> data, int depth)
	{
		if (data.size() == 0)
			return null;
		int k = depth % data.get(0).getDimensions();
		Collections.sort(data, new KDEntry.Sorter(k));

		int median = data.size() / 2;
		KDMemNode left = (median > 0 ? tree(data.subList(0, median), depth + 1) : null);
		KDMemNode right = (median + 1 < data.size() ? tree(data.subList(median + 1, data.size()), depth + 1) : null);
		return new KDMemNode(depth, data.get(median), left, right);
	}
}