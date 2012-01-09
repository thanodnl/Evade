/**
 * 
 */
package nl.thanod.evade.util;

import java.util.*;

import nl.thanod.evade.collection.Memtable;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;
import nl.thanod.evade.document.DocumentBuilder;

/**
 * @author nilsdijk
 */
public class Documenter implements Iterable<Document.Entry> {

	private final ArrayList<Iterable<Entry>> documentcontainers;

	public Documenter(Iterable<Document.Entry>... input) {
		this.documentcontainers = new ArrayList<Iterable<Document.Entry>>();
		for (int i = 0; i < input.length; i++)
			this.documentcontainers.add(input[i]);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator() {
		// if there is only one entry return that iterator
		if (this.documentcontainers.size() == 1)
			return this.documentcontainers.get(0).iterator();

		final List<Peekerator<Document.Entry>> peekers = new ArrayList<Peekerator<Entry>>(this.documentcontainers.size());
		for (Iterable<Document.Entry> c : this.documentcontainers)
			peekers.add(new Peekerator<Document.Entry>(c.iterator()));

		final Comparator<Peekerator<Entry>> sort = new Peekerator.Sorter<Document.Entry>(Document.Entry.COMPARATOR);
		return new Generator<Document.Entry>() {
			@Override
			protected Entry generate() throws NoSuchElementException {
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

	public static void main(String... args) {
		Memtable m1 = new Memtable();
		Memtable m2 = new Memtable();
		Memtable m3 = new Memtable();

		UUID id1 = UUID.randomUUID();
		Document doc1 = DocumentBuilder.start(1).put("name", "nils").put("age", 23).make();
		m1.update(id1, doc1);

		doc1 = DocumentBuilder.start(2).put("age", 24).make();
		m2.update(id1, doc1);

		UUID id2 = UUID.randomUUID();
		Document doc2 = DocumentBuilder.start(1).put("name", "koen").put("age", 24).make();
		m1.update(id2, doc2);
		
		doc2 = DocumentBuilder.start(2).put("age", 26).make();
		m3.update(id2, doc2);

		UUID id3 = UUID.randomUUID();
		Document doc3 = DocumentBuilder.start(1).put("name", "sam").put("age", 25).make();
		m1.update(id3, doc3);
		
		doc3 = DocumentBuilder.start(2).put("chair", "wheel").make();
		m2.update(id3, doc3);
		
		doc3 = DocumentBuilder.start(2).put("hair", "black").make();
		m3.update(id3, doc3);

		System.out.println(m1);
		System.out.println("-----------");
		System.out.println(m2);
		System.out.println("-----------");
		System.out.println(m3);
		System.out.println("-----------");

		Memtable r = new Memtable();
		for (Document.Entry e : new Documenter(m1, m2, m3)) {
			r.update(e.id, e.doc);
		}
		System.out.println(r);
	}
}
