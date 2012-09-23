/**
 * 
 */
package nl.thanod.evade.document;

import java.util.Comparator;

/**
 * @author nilsdijk
 */
public abstract class ValueDocument extends Document
{

	public static final Comparator<ValueDocument> VALUE_COMPARE = new Comparator<ValueDocument>() {

		@Override
		public int compare(ValueDocument o1, ValueDocument o2)
		{
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return 1;
			if (o2 == null)
				return -1;
			if (o1.type != o2.type)
				return o1.type.code - o2.type.code;
			return o1.compareValue(o2);
		}
	};

	/**
	 * @param version
	 * @param type
	 */
	public ValueDocument(long version, Type type)
	{
		super(version, type);
	}

	protected abstract int compareValue(ValueDocument other);

	@Override
	public String toString()
	{
		return super.toString() + this.valueString();
	}

	/**
	 * @return
	 */
	protected abstract String valueString();
}
