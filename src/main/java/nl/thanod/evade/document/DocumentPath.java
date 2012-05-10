/**
 * 
 */
package nl.thanod.evade.document;

/**
 * @author nilsdijk
 */
public class DocumentPath
{
	private final String[] path;
	private final int offset;
	private final int length;

	private DocumentPath(String[] path, int offset, int length)
	{
		this.path = path;
		this.offset = offset;
		this.length = length;
	}

	public DocumentPath(String... path)
	{
		this.path = path;
		this.offset = 0;
		this.length = this.path.length;
	}

	public DocumentPath(DocumentPath parent, String... path)
	{
		this.path = new String[parent.path.length + path.length];
		System.arraycopy(parent.path, parent.offset, this.path, 0, parent.length);
		System.arraycopy(path, 0, this.path, parent.length, path.length);

		this.offset = 0;
		this.length = this.path.length;
	}

	public DocumentPath next()
	{
		if (this.length == 0)
			return this;
		return new DocumentPath(this.path, this.offset + 1, this.length - 1);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.length; i++) {
			if (i > 0)
				sb.append('.');
			sb.append(this.get(i));
		}
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		for (int i = 0; i < this.length; i++)
			result = prime * result + this.get(i).hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentPath other = (DocumentPath) obj;
		if (this.length != other.length)
			return false;
		for (int i = 0; i < this.length; i++)
			if (!this.get(i).equals(other.get(i)))
				return false;
		return true;
	}

	public String get(int index)
	{
		if (index < 0 || index >= this.length)
			throw new IndexOutOfBoundsException();
		return this.path[this.offset + index];
	}

	public int length()
	{
		return this.length;
	}
}
