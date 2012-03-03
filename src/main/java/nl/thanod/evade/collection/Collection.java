/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.UUID;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public abstract class Collection implements Iterable<Document.Entry>
{
	public abstract boolean contains(UUID id);

	public abstract Document get(UUID id);
	
	public abstract int size();
}
