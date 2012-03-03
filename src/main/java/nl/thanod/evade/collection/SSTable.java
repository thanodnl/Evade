/**
 * 
 */
package nl.thanod.evade.collection;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import nl.thanod.evade.document.Document;
import nl.thanod.evade.document.Document.Entry;

/**
 * @author nilsdijk
 */
public class SSTable extends Collection
{

	public SSTable(File file)
	{

	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#contains(java.util.UUID)
	 */
	@Override
	public boolean contains(UUID id)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.collection.Collection#get(java.util.UUID)
	 */
	@Override
	public Document get(UUID id)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator()
	{
		throw new UnsupportedOperationException();
	}

	public static List<File> save(Iterable<Document.Entry> data)
	{
		return SSTable.save(data, Long.MAX_VALUE);
	}

	public static List<File> save(Iterable<Document.Entry> data, long maxsize)
	{
		return Collections.emptyList();
	}

}
