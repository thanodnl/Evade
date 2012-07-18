/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import nl.thanod.evade.collection.index.Index.Entry;

/**
 * @author nilsdijk
 */
public class CompoundIndex
{
	private static class CompoundIndexEntry extends Index.Entry
	{
		private PriorityQueue<Entry> found;
		private Entry next = null;
		private Entry prev = null;

		protected CompoundIndexEntry(Index.Entry prev, PriorityQueue<Entry> found)
		{
			super(take(found));
			this.prev = prev;
			this.found = found;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#next()
		 */
		@Override
		public Entry next()
		{
			if (this.next == null && this.found != null && this.found.size() != 0) {
				this.next = new CompoundIndexEntry(this, this.found);
				this.found = null;
			}

			return this.next;
		}

		/*
		 * (non-Javadoc)
		 * @see nl.thanod.evade.collection.index.Index.Entry#previous()
		 */
		@Override
		public Entry previous()
		{
			return this.prev;
		}

		public static Index.Entry take(PriorityQueue<Index.Entry> found)
		{
			Index.Entry base = found.poll();
			Index.Entry next = base.next();
			if (next != null)
				found.add(next);
			else
				found.remove(0);
			return base;
		}
	}

	private final List<Index> indices;

	public CompoundIndex(Index... indices)
	{
		this.indices = new ArrayList<Index>();
		for (Index index : indices)
			this.add(index);
	}

	public void add(Index index)
	{
		this.indices.add(index);
	}

	public Index.Entry before(Comparable<? super Index.Entry> find)
	{
		final PriorityQueue<Index.Entry> found = new PriorityQueue<Index.Entry>(this.indices.size(), Index.Entry.VALUE_COMPARE);
		for (Index index : this.indices) {
			Entry use = Search.before(index, find);
			if (find.compareTo(use) < 0)
				use = use.next();
			if (use != null)
				found.add(use);
		}

		if (found.size() == 0)
			return null;

		return new CompoundIndexEntry(null, found);
	}
}
