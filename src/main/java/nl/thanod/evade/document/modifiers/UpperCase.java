/**
 * 
 */
package nl.thanod.evade.document.modifiers;


/**
 * @author nilsdijk
 *
 */
public class UpperCase extends AbstractModifier
{

	@Override
	public String modify(String s){
		return s.toUpperCase();
	}
}
