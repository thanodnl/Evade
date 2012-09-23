/**
 * 
 */
package nl.thanod.evade.document;

import nl.thanod.evade.document.visitor.DocumentVisitor;

/**
 * @author nilsdijk
 */
public class TupleDocument extends ValueDocument
{
	private final ValueDocument[] items;

	public TupleDocument(ValueDocument... documents)
	{
		this(0, documents);
	}

	public TupleDocument(long version, ValueDocument... documents)
	{
		super(version, Type.TUPLE);
		this.items = documents;
	}

	public int size()
	{
		return this.items.length;
	}

	public ValueDocument get(int index)
	{
		return this.items[index];
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.ValueDocument#compareValue(nl.thanod.evade.document
	 * .ValueDocument)
	 */
	@Override
	protected int compareValue(ValueDocument other)
	{
		TupleDocument td = (TupleDocument) other;
		int max = Math.min(this.size(), td.size());
		for (int i = 0; i < max; i++) {
			int diff = this.get(i).compareValue(td.get(i));
			if (diff != 0)
				return diff;
		}
		return this.size() - td.size();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * nl.thanod.evade.document.Document#accept(nl.thanod.evade.document.visitor
	 * .DocumentVisitor, java.lang.Object)
	 */
	@Override
	public <OUT, IN> OUT accept(DocumentVisitor<OUT, IN> visitor, IN data)
	{
		return visitor.visit(this, data);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof TupleDocument))
			return false;
		TupleDocument other = (TupleDocument) obj;
		if (this.size() != other.size())
			return false;
		for (int i = 0; i < this.size(); i++)
			if (this.get(i).equals(other.get(i)))
				return false;
		return true;
	}

	@Override
	protected String valueString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i = 0; i < this.size(); i++) {
			if (i > 0)
				sb.append(',');
			sb.append(this.get(i).valueString());
		}
		sb.append(')');
		return sb.toString();
	}

	public boolean equals(int granularity, TupleDocument other)
	{
		for (int i = 0; i < granularity; i++) {
			if (i > this.size() || i > other.size())
				return false;
			if (!this.get(i).equals(other.get(i)))
				return false;
		}
		return true;
	}

	/**
	 * @param data
	 * @return
	 */
	public TupleDocument granularity(int size)
	{
		if (size >= this.size())
			return this;
		ValueDocument[] docs = new ValueDocument[size];
		for (int i = 0; i < size; i++)
			docs[i] = this.get(i);
		return new TupleDocument(this.version, docs);
	}
}
