/**
 * 
 */
package nl.thanod.evade.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import nl.thanod.evade.collection.DocumentCollection;
import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.util.DocumentUnduplicator;
import nl.thanod.evade.util.iterator.Sorterator;
import nl.thanod.evade.util.iterator.Unduplicator;

/**
 * @author nilsdijk
 */
public class Documenter implements Iterable<Document.Entry>
{

	private final Collection<? extends DocumentCollection> data;

	public Documenter(Collection<? extends DocumentCollection> input)
	{
		this.data = input;
	}

	public Documenter(DocumentCollection ide, Collection<? extends DocumentCollection>... inputs)
	{
		ArrayList<DocumentCollection> list = new ArrayList<DocumentCollection>();
		list.add(ide);
		for (Collection<? extends DocumentCollection> input : inputs)
			list.addAll(input);

		this.data = list;
	}

	public Documenter(DocumentCollection ide, Iterable<? extends DocumentCollection> input)
	{
		ArrayList<DocumentCollection> list = new ArrayList<DocumentCollection>();
		list.add(ide);
		for (DocumentCollection e : input)
			list.add(e);

		this.data = list;
	}

	public Documenter(DocumentCollection... input)
	{
		ArrayList<DocumentCollection> list = new ArrayList<DocumentCollection>();
		for (int i = 0; i < input.length; i++)
			list.add(input[i]);
		this.data = list;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Document.Entry> iterator()
	{
		@SuppressWarnings("unchecked")
		final Iterator<Document.Entry> peek = new Sorterator<Document.Entry>(Document.Entry.COMPARATOR, this.data);
		return new Unduplicator<Document.Entry>(peek, DocumentUnduplicator.get());
	}
}
