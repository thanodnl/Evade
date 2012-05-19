/**
 * 
 */
package nl.thanod.evade.collection.index;

import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public class IndexDescriptor
{
	public final DocumentPath path;
	public final Modifier modifier;

	public IndexDescriptor(DocumentPath path)
	{
		this(path, null);
	}

	public IndexDescriptor(DocumentPath path, Modifier modifier)
	{
		this.path = path;
		this.modifier = modifier;
	}
}
