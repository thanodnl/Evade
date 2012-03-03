/**
 * 
 */
package nl.thanod.evade.document;

/**
 * @author nilsdijk
 */
public class StringDocument extends Document
{

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

}
