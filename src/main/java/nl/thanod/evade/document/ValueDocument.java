/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public class ValueDocument<T> extends Document
{

	public final T value;

	/**
	 * @param version
	 * @param type
	 */
	public ValueDocument(long version, T value)
	{
		super(version, Type.STRING);
		this.value = value;
	}

	@Override
	public boolean equals(Object that)
	{
		if (!super.equals(that))
			return false;
		if (!(that instanceof ValueDocument))
			return false;
		ValueDocument<?> thats = (ValueDocument<?>) that;
		return this.value.equals(thats.value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#test(nl.thanod.evade.query.Constraint)
	 */
	@Override
	public boolean test(Constraint c)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.document.Document#visit(nl.thanod.evade.document.visitor.DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		// TODO Auto-generated method stub
		
	}

}
