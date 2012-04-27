package nl.thanod.evade.util;

/**
 * @author nilsdijk
 */
public class Linked<T>
{
	public final Linked<T> next;
	public final T t;

	public Linked(Linked<T> next, T t)
	{
		this.next = next;
		this.t = t;
	}
}
