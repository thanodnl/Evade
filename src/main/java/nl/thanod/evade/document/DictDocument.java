/**
 * 
 */
package nl.thanod.evade.document;

import java.util.*;

import nl.thanod.evade.document.visitor.DocumentVisitor;

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
		if (data.size() == 0) {
			this.data = Collections.emptyMap();
		} else if (data.size() == 1) {
			Map.Entry<String, Document> content = data.entrySet().iterator().next();
			this.data = Collections.singletonMap(content.getKey(), content.getValue());
		} else if (clone) {
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
		return "(" + this.version + '/' + this.clearedOn + ":" + this.type.name() + ") " + this.data.toString();
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

	@Override
	public Document get(DocumentPath path)
	{
		Document d = this.get(path.get(0));
		if (d == null)
			return null;
		return d.get(path.next());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor
	 * .ParameterizedDocumentVisitor, java.lang.Object)
	 */
	@Override
	public <OUT, IN> OUT accept(DocumentVisitor<OUT, IN> visitor, IN data)
	{
		return visitor.visit(this, data);
	}
}
