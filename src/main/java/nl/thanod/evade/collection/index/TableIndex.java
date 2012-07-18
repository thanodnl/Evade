/**
 * 
 */
package nl.thanod.evade.collection.index;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class TableIndex
{
	private static final Logger log = LoggerFactory.getLogger(TableIndex.class);

	private final Map<IndexDescriptor, CompoundIndex> indices = new HashMap<IndexDescriptor, CompoundIndex>();

	public TableIndex()
	{
	}

	public void add(SSIndex index)
	{
		if (index.desc == null) {
			log.error("Old style index found {}. Old indices cant be used any more", index);
			return;
		}

		CompoundIndex cidx = this.indices.get(index.desc);
		if (cidx == null)
			this.indices.put(index.desc, cidx = new CompoundIndex());
		cidx.add(index);
		
		log.info("Added index from {} on {}", index.file, index.desc);
	}

	/**
	 * @param desc
	 * @return
	 */
	public boolean contains(IndexDescriptor desc)
	{
		return this.indices.containsKey(desc);
	}

	/**
	 * @param desc
	 * @return
	 */
	public CompoundIndex get(IndexDescriptor desc)
	{
		return this.indices.get(desc);
	}
}
