/**
 * 
 */
package nl.thanod.evade.util;

import java.util.*;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.util.iterator.Peekerator;
import nl.thanod.evade.util.iterator.Sorterator;

/**
 * @author nilsdijk
 */
public class Documenter implements Iterable<Document.Entry>
{

	private final Iterable<? extends Iterable<Entry>> data;

	public Documenter(Collection<? extends Iterable<Document.Entry>> input)
	{
		this.data = input;
	}

	public Documenter(Iterable<Document.Entry> ide, Collection<? extends Iterable<Document.Entry>> input)
	{
		ArrayList<Iterable<Document.Entry>> list = new ArrayList<Iterable<Entry>>();
		list.add(ide);
		list.addAll(input);

		this.data = list;
	}

	public Documenter(Iterable<Document.Entry>... input)
	{
		ArrayList<Iterable<Entry>> list = new ArrayList<Iterable<Document.Entry>>();
		for (int i = 0; i < input.length; i++)
			list.add(input[i]);
		this.data = list;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		final Peekerator<Entry> peek = new Peekerator<Document.Entry>(new Sorterator<Document.Entry>(this.data, Document.Entry.COMPARATOR));
		return new Iterator<Document.Entry>() {
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Entry next()
			{
				if (!peek.hasNext())
					throw new NoSuchElementException();

				Entry e = peek.next();
				UUID id = e.id;
				Document doc = e.doc;
				while (peek.hasNext() && id.equals(peek.peek().id)) {
					e = null;
					doc = Document.merge(doc, peek.next().doc);
				}
				if (e != null) // reuse the entry when no documents are merged
					return e;
				return new Document.Entry(id, doc);
			}

			@Override
			public boolean hasNext()
			{
				return peek.hasNext();
			}
		};
	}
}
