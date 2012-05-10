/**
 * 
 */
package nl.thanod.evade.document;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author nilsdijk
 */
public class DocumentBuilder
{

	private final long version;

	private Stack<Map<String, Document>> data = new Stack<Map<String, Document>>();
	private Stack<String> names = new Stack<String>();

	public DocumentBuilder(long version)
	{
		this.version = version;
		this.data.push(new TreeMap<String, Document>());
	}

	public DocumentBuilder tree(String name)
	{
		this.data.push(new TreeMap<String, Document>());
		this.names.push(name);
		return this;
	}

	public DocumentBuilder pop()
	{
		if (this.names.size() <= 0)
			throw new IllegalStateException("Nothing to pop");
		String name = this.names.pop();
		Map<String, Document> data = this.data.pop();
		this.data.peek().put(name, new DictDocument(data, false));
		return this;
	}

	public DictDocument make()
	{
		if (this.data.size() <= 0)
			throw new IllegalStateException("Document already made");
		while (this.names.size() > 0)
			this.pop();
		return new DictDocument(this.data.pop(), false);
	}

	public static DocumentBuilder start(long version)
	{
		return new DocumentBuilder(version);
	}

	/**
	 * @param name
	 * @return
	 */
	public DocumentBuilder putNull(String name)
	{
		this.data.peek().put(name, new NullDocument(this.version));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, String value)
	{
		this.data.peek().put(name, new StringDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, int value)
	{
		this.data.peek().put(name, new IntegerDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, long value)
	{
		this.data.peek().put(name, new LongDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, boolean value)
	{
		this.data.peek().put(name, new BooleanDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, double value)
	{
		this.data.peek().put(name, new DoubleDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, float value)
	{
		this.data.peek().put(name, new FloatDocument(this.version, value));
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public DocumentBuilder put(String name, UUID value)
	{
		this.data.peek().put(name, new UUIDDocument(this.version, value));
		return this;
	}
}
