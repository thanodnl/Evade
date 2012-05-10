/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(int)
	 */
	@Override
	public int modify(int value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(boolean)
	 */
	@Override
	public boolean modify(boolean value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(long)
	 */
	@Override
	public long modify(long value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(java.util.UUID)
	 */
	@Override
	public UUID modify(UUID value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(double)
	 */
	@Override
	public double modify(double value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(float)
	 */
	@Override
	public float modify(float value)
	{
		for (Modifier m : this.modifiers)
			value = m.modify(value);
		return value;
	}
}
