/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nilsdijk
 */
public class ChainedModifier implements Modifier
{
	private final List<Modifier> modifiers;

	public ChainedModifier(Modifier... modifiers)
	{
		this.modifiers = new ArrayList<Modifier>(modifiers.length);
		for (int i = 0; i < modifiers.length; i++)
			this.modifiers.add(modifiers[i]);
	}

	@Override
	public String modify(String s)
	{
		for (Modifier m : this.modifiers)
			s = m.modify(s);
		return s;
	}
}
