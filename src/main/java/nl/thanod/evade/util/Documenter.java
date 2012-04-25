/**
 * 
 */
package nl.thanod.evade.util;

import java.util.*;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;

/**
 * @author nilsdijk
 */
public class Documenter implements Iterable<Document.Entry>
{

	private final ArrayList<Iterable<Entry>> documentcontainers;

	public Documenter(Collection<? extends Iterable<Document.Entry>> input)
	{
		this.documentcontainers = new ArrayList<Iterable<Document.Entry>>(input.size());
		this.documentcontainers.addAll(input);
	}

	public Documenter(Iterable<Document.Entry> ide, Collection<? extends Iterable<Document.Entry>> input)
	{
		this.documentcontainers = new ArrayList<Iterable<Document.Entry>>(input.size() + 1);
		if (ide != null)
			this.documentcontainers.add(ide);
		this.documentcontainers.addAll(input);
	}

	public Documenter(Iterable<Document.Entry>... input)
	{
		this.documentcontainers = new ArrayList<Iterable<Document.Entry>>();
		for (int i = 0; i < input.length; i++)
			this.documentcontainers.add(input[i]);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		// if there is only one entry return that iterator
		if (this.documentcontainers.size() == 1)
			return this.documentcontainers.get(0).iterator();

		final List<Peekerator<Document.Entry>> peekers = new ArrayList<Peekerator<Entry>>(this.documentcontainers.size());
		for (Iterable<Document.Entry> c : this.documentcontainers)
			peekers.add(new Peekerator<Document.Entry>(c.iterator()));

		// remove empty peekerators
		Iterator<Peekerator<Entry>> it = peekers.iterator();
		while (it.hasNext()) {
			if (!it.next().hasNext())
				it.remove();
		}

		final Comparator<Peekerator<Entry>> sort = new Peekerator.Sorter<Document.Entry>(Document.Entry.COMPARATOR);
		return new Generator<Document.Entry>() {
			@Override
			protected Entry generate() throws NoSuchElementException
			{
				if (peekers.size() <= 0)
					throw new NoSuchElementException();
				Collections.sort(peekers, sort);
				Document.Entry v = peekers.get(0).next();
				Document doc = v.doc;

				// merge all documents with the same identifier
				for (int i = 1; i < peekers.size(); i++) {
					// see if the next peeker contains data for the same object
					if (v.id.equals(peekers.get(i).peek().id)) {
						doc = Document.merge(doc, peekers.get(i).next().doc);
					} else {
						// if not you may stop because the peekerators are sorted
						break;
					}
				}

				// remove empty peekerators
				Iterator<Peekerator<Entry>> it = peekers.iterator();
				while (it.hasNext()) {
					if (!it.next().hasNext())
						it.remove();
				}

				// generate a new entry
				return new Document.Entry(v.id, doc);
			}
		};
	}
}
