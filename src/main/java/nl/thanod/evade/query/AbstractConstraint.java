/**
 * 
 */
package nl.thanod.evade.query;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public abstract class AbstractConstraint implements Constraint
{
	protected final Modifier m;

	/**
	 * @param m
	 */
	public AbstractConstraint(Modifier m)
	{
		this.m = m;
	}

	@Override
	public Modifier getModifier()
	{
		return this.m;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.query.Constraint#test(nl.thanod.evade.document.StringDocument
	 * )
	 */
	@Override
	public boolean test(StringDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(NullDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(DictDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(BooleanDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(IntegerDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(LongDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(UUIDDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(DoubleDocument doc)
	{
		return false;
	}

	@Override
	public boolean test(FloatDocument doc)
	{
		return false;
	}
}
