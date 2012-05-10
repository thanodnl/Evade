/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import java.util.UUID;

/**
 * @author nilsdijk
 */
public abstract class AbstractModifier implements Modifier
{
	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.modifiers.Modifier#modify(nl.thanod.evade.document
	 * .StringDocument)
	 */
	@Override
	public String modify(String s)
	{
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(int)
	 */
	@Override
	public int modify(int value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(boolean)
	 */
	@Override
	public boolean modify(boolean value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(long)
	 */
	@Override
	public long modify(long value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(java.util.UUID)
	 */
	@Override
	public UUID modify(UUID value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(double)
	 */
	@Override
	public double modify(double value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.document.modifiers.Modifier#modify(float)
	 */
	@Override
	public float modify(float value)
	{
		return value;
	}
}
