/**
 * 
 */
package nl.thanod.evade.document;


/**
 * @author nilsdijk
 *
 */
public abstract class ValueDocument extends Document
{

	/**
	 * @param version
	 * @param type
	 */
	public ValueDocument(long version, Type type)
	{
		super(version, type);
	}

}
