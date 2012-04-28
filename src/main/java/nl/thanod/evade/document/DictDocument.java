/**
 * 
 */
package nl.thanod.evade.document;

import java.util.*;

import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public class DictDocument extends Document
{

	private final Map<String, Document> data;

	public final long clearedOn;

	public DictDocument(Map<String, Document> entries)
	{
		this(entries, 0, true);
	}

	public DictDocument(Map<String, Document> data, boolean clone)
	{
		this(data, 0, clone);
	}

	/**
	 * @param version
	 */
	public DictDocument(Map<String, Document> data, long clearedOn, boolean clone)
	{
		super(Document.newestVersion(data.values()), Type.DICT);
		this.clearedOn = clearedOn;
		if (clone) {
			this.data = new TreeMap<String, Document>();
			for (Map.Entry<String, Document> e : data.entrySet())
				this.data.put(e.getKey(), e.getValue());
		} else {
			this.data = data;
		}
	}

	public Document get(String name)
	{
		return this.data.get(name);
	}

	@Override
	public String toString()
	{
		return "(" + this.version + '/' + this.clearedOn + ") " + this.data.toString();
	}

	/**
	 * @return
	 */
	public Set<Map.Entry<String, Document>> entrySet()
	{
		return Collections.unmodifiableSet(this.data.entrySet());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (clearedOn ^ (clearedOn >>> 32));
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictDocument other = (DictDocument) obj;
		if (clearedOn != other.clearedOn)
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	public DictDocument clearOn(long version)
	{
		if (version <= this.clearedOn)
			return this;
		Map<String, Document> map = new TreeMap<String, Document>();
		for (Map.Entry<String, Document> e : this.data.entrySet()) {
			if (e.getValue().version < version)
				continue;
			if (e.getValue() instanceof DictDocument)
				map.put(e.getKey(), ((DictDocument) e.getValue()).clearOn(version));
			else
				map.put(e.getKey(), e.getValue());
		}
		return new DictDocument(map, version, false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#test(nl.thanod.evade.query.Constraint)
	 */
	@Override
	public boolean test(Constraint c)
	{
		return c.test(this);
	}

	@Override
	public Document path(List<String> path)
	{
		Document d = this.get(path.get(0));
		if (d == null)
			return null;
		return d.path(path.subList(1, path.size()));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#visit(nl.thanod.evade.document.visitor
	 * .DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#compareValue(nl.thanod.evade.document
	 * .Document)
	 */
	@Override
	protected int compareValue(Document other)
	{
		DictDocument dother = (DictDocument) other;
		Iterator<Map.Entry<String, Document>> tit = this.data.entrySet().iterator();
		Iterator<Map.Entry<String, Document>> oit = dother.data.entrySet().iterator();

		int diff;

		while (tit.hasNext() && oit.hasNext()) {
			Map.Entry<String, Document> te = tit.next();
			Map.Entry<String, Document> oe = oit.next();

			diff = te.getKey().compareTo(oe.getKey());
			if (diff != 0)
				return diff;
			diff = Document.VALUE_SORT.compare(te.getValue(), oe.getValue());
			if (diff != 0)
				return diff;
		}
		if (tit.hasNext())
			return 1;
		if (oit.hasNext())
			return -1;
		return 0;
	}
}
