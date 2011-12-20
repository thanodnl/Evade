/**
 * 
 */
package nl.thanod.evade.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author nilsdijk
 */
public class Peekerator<E> implements Iterator<E> {

	private final Iterator<E> it;
	private E peek;
	private boolean peeked;

	public Peekerator(Iterator<E> it) {
		this.it = it;
	}

	public E peek() {
		if (peeked)
			return peek;
		if (!it.hasNext())
			throw new NoSuchElementException();
		peek = it.next();
		peeked = true;
		return peek;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.peeked || this.it.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next() {
		if (this.peeked){
			this.peeked = false;
			return this.peek;
		}
		return this.it.next();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
