/**
 * 
 */
package nl.thanod.evade.document.modifiers;


/**
 * @author nilsdijk
 */
public class LowerCase extends AbstractModifier
{
	@Override
	public String modify(String s)
	{
		return s.toLowerCase();
	}
}
