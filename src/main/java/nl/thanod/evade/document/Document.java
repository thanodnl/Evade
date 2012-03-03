/**
 * 
 */
package nl.thanod.evade.document;

import java.util.*;

/**
 * @author nilsdijk
 */
public abstract class Document
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

		public String toString()
		{
			return this.id + ":" + this.doc;
		}
	}

	public enum Type
	{
		NULL(0x00, true),
		DOCUMENT(0x01, false),
		STRING(0x0F, true);

		public final int code;
		public final boolean valuetype;

		private Type(int code, boolean valuetype)
		{
			this.valuetype = valuetype;
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
	private volatile boolean complete = false;

	public Document(long version, Type type)
	{
		this.version = version;
		this.type = type;
	}

	public long version()
	{
		return this.version;
	}

	public static Document newest(Document one, Document two)
	{
		if (one.version == two.version) {
			if (one.hashCode() < two.hashCode())
				return one;
			else
				return two;
		}
		return one.version > two.version ? one : two;
	}

	public String toString()
	{
		return "(" + this.version + ") ";
	}

	@Override
	public boolean equals(Object that)
	{
		if (!(that instanceof Document))
			return false;
		Document thatbase = (Document) that;
		if (this.version != thatbase.version)
			return false;
		if (this.type != thatbase.type)
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
		if (doc1.type.valuetype && doc2.type.valuetype)
			return newest(doc1, doc2);
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
	public static Document newest(Iterable<Document> values)
	{
		Iterator<Document> it = values.iterator();
		Document newest = it.next();
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
}
