/**
 * 
 */
package nl.thanod.evade.document;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author nilsdijk
 */
public class DictDocument extends Document {

	private final Map<String, Document> data;

	public final long clearedOn;

	public DictDocument(Map<String, Document> entries) {
		this(entries, 0, true);
	}

	public DictDocument(Map<String, Document> data, boolean clone) {
		this(data, 0, clone);
	}

	/**
	 * @param version
	 */
	public DictDocument(Map<String, Document> data, long clearedOn, boolean clone) {
		super(Document.newestVersion(data.values()), Type.DOCUMENT);
		this.clearedOn = clearedOn;
		if (clone) {
			this.data = new TreeMap<String, Document>();
			for (Map.Entry<String, Document> e : data.entrySet())
				this.data.put(e.getKey(), e.getValue());
		} else {
			this.data = data;
		}
	}

	public Document get(String name) {
		return this.data.get(name);
	}

	@Override
	public String toString() {
		return "("+this.version + '/' + this.clearedOn +  ") " + this.data.toString();
	}

	/**
	 * @return
	 */
	public Set<Map.Entry<String, Document>> entrySet() {
		return Collections.unmodifiableSet(this.data.entrySet());
	}

	@Override
	public boolean equals(Object that) {
		if (!super.equals(that))
			return false;
		if (!(that instanceof DictDocument))
			return false;

		DictDocument thatdoc = (DictDocument) that;
		return this.clearedOn == thatdoc.clearedOn && this.data.equals(thatdoc.data);
	}

	public DictDocument clearOn(long version) {
		if (version <= this.clearedOn)
			return this;
		Map<String, Document> map = new TreeMap<String, Document>();
		for (Map.Entry<String, Document> e:this.data.entrySet()){
			if (e.getValue().version < version)
				continue;
			if (e.getValue() instanceof DictDocument)
				map.put(e.getKey(), ((DictDocument)e.getValue()).clearOn(version));
			else
				map.put(e.getKey(), e.getValue());
		}
		return new DictDocument(map, version, false);
	}
}
