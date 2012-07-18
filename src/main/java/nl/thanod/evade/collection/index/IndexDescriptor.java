/**
 * 
 */
package nl.thanod.evade.collection.index;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.DocumentBuilder;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.StringDocument;

/**
 * @author nilsdijk
 */
public class IndexDescriptor
{
	private static final DocumentPath CONFIG_PATH_PATH = new DocumentPath("path");

	public final DocumentPath path;

	public IndexDescriptor(DocumentPath path)
	{
		this.path = path;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		IndexDescriptor other = (IndexDescriptor) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	public Document serialize()
	{
		return DocumentBuilder.start(System.currentTimeMillis()).put("path", this.path.toString()).make();
	}

	public static IndexDescriptor deserialize(Document doc)
	{
		Document docPath = doc.get(IndexDescriptor.CONFIG_PATH_PATH);
		StringDocument strDocPath = (StringDocument) docPath;
		DocumentPath path = new DocumentPath(strDocPath.value);

		return new IndexDescriptor(path);
	}

	@Override
	public String toString()
	{
		return this.path.toString();
	}

}
