/**
 * 
 */
package nl.thanod.evade.collection.index;

import nl.thanod.evade.document.Document;

/**
 * @author nilsdijk
 *
 */
public interface Index
{
	Iterable<Document.Entry> before(Document doc);
}
