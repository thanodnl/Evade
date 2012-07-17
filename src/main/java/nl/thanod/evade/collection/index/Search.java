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
	 * implementation of a binary search which works on a sorted set
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

	public static <T> int before(T[] scope, int offset, int length, Comparable<? super T> compare)
	{
		int size = length;
		if (size == 0)
			return 0;

		int bit = 1 << (31 - Integer.numberOfLeadingZeros(size));
		int found = 0;

		while (bit != 0) {
			int index = found | bit;
			if (index < size) {
				T t = scope[offset + index];
				if (compare.compareTo(t) > 0)
					found = index;
			}
			bit >>= 1;
		}
		T t = scope[offset + found];
		if (compare.compareTo(t) > 0)
			return offset + found + 1;
		return offset + found;
	}

	public static <T> T before(Searchable<T> scope, Comparable<? super T> compare)
	{
		int size = scope.count();
		if (size == 0)
			return null;

		int bit = 1 << (31 - Integer.numberOfLeadingZeros(size));
		int found = 0;

		while (bit != 0) {
			int index = found | bit;
			if (index < size) {
				T t = scope.get(index);
				if (compare.compareTo(t) > 0)
					found = index;
			}
			bit >>= 1;
		}
		T t = scope.get(found);
		if (compare.compareTo(t) > 0)
			if (found + 1 < size)
				t = scope.get(found + 1);
		return t;
	}

	public static <T> T after(Searchable<T> scope, Comparable<? super T> compare)
	{
		int size = scope.count();
		if (size == 0)
			return null;

		int bit = 1 << (31 - Integer.numberOfLeadingZeros(size));
		int found = 0;

		while (bit != 0) {
			int index = found | bit;
			if (index < size) {
				T t = scope.get(index);
				if (compare.compareTo(t) >= 0)
					found = index;
			}
			bit >>= 1;
		}
		T t = scope.get(found);
		if (compare.compareTo(t) < 0)
			t = scope.get(found - 1);
		return t;
	}
}
