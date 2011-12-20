/**
 * 
 */
package nl.thanod.evade.collection;

import java.util.UUID;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 */
public interface Collection extends Iterable<Document.Entry> {
	boolean contains(UUID id);

	Document get(UUID id);
}
