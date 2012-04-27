/**
 * 
 */
package nl.thanod.evade.document;

import java.util.Comparator;

import nl.thanod.evade.document.visitor.DocumentVisitor;
import nl.thanod.evade.query.Constraint;

/**
 * @author nilsdijk
 */
public class StringDocument extends Document
{

	public static final Comparator<StringDocument> VALUE_SORT = new Comparator<StringDocument>() {

		@Override
		public int compare(StringDocument o1, StringDocument o2)
		{
			return o1.value.compareTo(o2.value);
		}
	};
	public final String value;

	/**
	 * @param version
	 * @param type
	 */
	public StringDocument(long version, String value)
	{
		super(version, Type.STRING);
		this.value = value;
	}

	@Override
	public String toString()
	{
		return super.toString() + this.value;
	}

	@Override
	public boolean equals(Object that)
	{
		if (!super.equals(that))
			return false;
		if (!(that instanceof StringDocument))
			return false;
		StringDocument thats = (StringDocument) that;
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
		return c.test(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#visit(nl.thanod.evade.document.visitor
	 * .DocumentVisitor)
	 */
	@Override
	public void accept(DocumentVisitor visitor)
	{
		visitor.visit(this);
	}
}
