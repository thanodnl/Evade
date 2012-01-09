/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.util.Iterator;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;

/**
 * @author nilsdijk
 *
 */
public class SSTable extends Collection {

	public SSTable(File file){
		
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator() {
		throw new UnsupportedOperationException();
	}
	
}
