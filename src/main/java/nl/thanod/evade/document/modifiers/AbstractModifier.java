/**
 * 
 */
package nl.thanod.evade.document.modifiers;


/**
 * @author nilsdijk
 */
public abstract class AbstractModifier implements Modifier
{
	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(nl.thanod.evade.document.StringDocument)
	 */
	@Override
	public String modify(String s)
	{
		return s;
	}
}
