/**
 * 
 */
package nl.thanod.evade.document;

import java.util.*;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public abstract class Document implements Comparable<Document>
{
	public static class Entry
	{
		public static Comparator<Entry> COMPARATOR = new Comparator<Document.Entry>() {
			@Override
			public int compare(Entry o1, Entry o2)
			{
				return o1.id.compareTo(o2.id);
			}
		};

		public final UUID id;
		public final Document doc;

		public Entry(UUID id, Document doc)
		{
			this.id = id;
			this.doc = doc;
		}

		@Override
		public String toString()
		{
			return this.id + ":" + this.doc;
		}
	}

	public enum Type
	{
		NULL(0x00),
		DICT(0x01),
		STRING(0x0F),
		BOOLEAN(0x10),
		INTEGER(0x11),
		LONG(0x12),
		UUID(0x13),
		DOUBLE(0x14),
		FLOAT(0x15);

		public final int code;
		private Type(int code)
		{
			this.code = code;
		}

		public static Type getByCode(int code)
		{
			for (Type t : Type.values())
				if (t.code == code)
					return t;
			return null;
		}
	}

	public final long version;
	public final Type type;

	public Document(long version, Type type)
	{
		this.version = version;
		this.type = type;
	}

	public long version()
	{
		return this.version;
	}

	public static ValueDocument newest(ValueDocument one, ValueDocument two)
	{
		if (one.version == two.version) {
			if (one.hashCode() < two.hashCode())
				return one;
			else
				return two;
		}
		return one.version > two.version ? one : two;
	}

	@Override
	public String toString()
	{
		return "(" + this.version + ") ";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (int) (version ^ (version >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (type != other.type)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	public static Document merge(Document... docs)
	{
		return merge(Arrays.asList(docs));
	}

	public static Document merge(Iterable<Document> docs)
	{
		Iterator<Document> it = docs.iterator();
		Document doc = it.next();
		while (it.hasNext())
			doc = merge(doc, it.next());
		return doc;
	}

	public static Document merge(Document doc1, Document doc2)
	{
		// return the other is one of those is empty
		if (doc1 == null)
			return doc2;
		if (doc2 == null)
			return doc1;

		if (doc1 instanceof ValueDocument && doc2 instanceof ValueDocument)
			return newest((ValueDocument)doc1, (ValueDocument)doc2);
		if (doc1 instanceof DictDocument && doc2 instanceof DictDocument)
			return mergeDictDict((DictDocument) doc1, (DictDocument) doc2);
		else if (doc1 instanceof DictDocument)
			return mergeDictValue((DictDocument) doc1, doc2);
		else if (doc2 instanceof DictDocument)
			return mergeDictValue((DictDocument) doc2, doc1);

		throw new RuntimeException("Unknown merge action");
	}

	/**
	 * @param doc1
	 * @param doc2
	 * @return
	 */
	private static Document mergeDictValue(DictDocument doc1, Document doc2)
	{
		if (doc2.version > doc1.version) // value type is newer than the newest dict entry
			return doc2;
		return doc1.clearOn(doc2.version);
	}

	/**
	 * @param doc1
	 * @param doc2
	 * @return
	 */
	private static Document mergeDictDict(DictDocument doc1, DictDocument doc2)
	{
		long cleared = Math.max(doc1.clearedOn, doc2.clearedOn);
		doc1 = doc1.clearOn(cleared);
		doc2 = doc2.clearOn(cleared);
		Map<String, Document> map = new TreeMap<String, Document>();
		for (Map.Entry<String, Document> e : doc1.entrySet())
			map.put(e.getKey(), e.getValue());
		for (Map.Entry<String, Document> e : doc2.entrySet()) {
			Document d = e.getValue();
			if (map.containsKey(e.getKey()))
				d = Document.merge(d, map.get(e.getKey()));
			map.put(e.getKey(), d);
		}
		return new DictDocument(map, cleared, false);
	}

	/**
	 * @param values
	 * @return
	 */
	public static Document newest(Iterable<ValueDocument> values)
	{
		Iterator<ValueDocument> it = values.iterator();
		ValueDocument newest = it.next();
		while (it.hasNext())
			newest = newest(newest, it.next());
		return newest;
	}

	/**
	 * @param values
	 * @return
	 */
	public static long newestVersion(Iterable<Document> values)
	{
		Iterator<Document> docs = values.iterator();
		if (!docs.hasNext())
			return 0;
		long version = docs.next().version;
		while (docs.hasNext())
			version = Math.max(version, docs.next().version);
		return version;
	}

	/**
	 * @param visitor
	 * @return
	 */
	public final <OUT> OUT accept(DocumentVisitor<OUT, ?> visitor)
	{
		return this.accept(visitor, null);
	}

	public abstract <OUT, IN> OUT accept(DocumentVisitor<OUT, IN> visitor, IN data);

	@Override
	public final int compareTo(Document o)
	{
		int diff = this.type.code - o.type.code;
		if (diff != 0)
			return diff;
		return this.compareValue(o);
	}

	protected abstract int compareValue(Document other);

	public Document get(DocumentPath path)
	{
		return path.length() == 0 ? this : null;
	}
}
