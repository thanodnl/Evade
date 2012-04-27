/**
 * 
 */
package nl.thanod.evade.collection.index;

/**
 * @author nilsdijk
 */
public final class Search
{
	public static interface Searchable<T>
	{
		T get(int index);

		int count();
	}

	/**
	 * Un-instantiable class
	 */
	private Search()
	{
	}

	/**
	 * implementation of a binary search
	 * @param scope
	 * @param compare
	 * @return
	 */
	public static <T> T binsearch(Searchable<T> scope, Comparable<? super T> compare)
	{
		int min = 0;
		int max = scope.count();

		T t;
		do {
			int mid = (max - min) / 2 + min;
			t = scope.get(mid);

			int diff = compare.compareTo(t);

			if (diff == 0) {
				return t;
			} else if (diff > 0) {
				min = mid + 1;
			} else {
				max = mid - 1;
			}
		} while (min < max);
		if (min == max && min >= 0 && min < scope.count())
			t = scope.get(min);
		return t;
	}
}
