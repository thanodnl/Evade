package net.thepinguin.evade.query;

import java.util.NoSuchElementException;

import net.thepinguin.evade.exceptions.NoSuchIndexException;
import nl.thanod.evade.collection.index.CompoundIndex;
import nl.thanod.evade.collection.index.Index;
import nl.thanod.evade.collection.index.Index.Entry;
import nl.thanod.evade.collection.index.IndexDescriptor;
import nl.thanod.evade.collection.index.TableIndex;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.document.DocumentPath;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.ValueDocument;
import nl.thanod.evade.util.iterator.Generator;

/**
 * @author dblommesteijn
 */
public class Query
{
	public final String collection;
	public final DocumentPath path;
	public final String content;

	public Query(String collection, DocumentPath path, String content)
	{
		this.collection = collection;
		this.path = path;
		this.content = content;
	}

	public Iterable<Index.Entry> solve(Database db) throws NoSuchIndexException
	{
		TableIndex tableIndex = db.getTableIndex(this.collection);
		CompoundIndex compoundIndex = tableIndex.get(new IndexDescriptor(this.path));
		if (compoundIndex == null)
			throw new NoSuchIndexException();
		
		final ValueDocument find = new StringDocument(this.content);
		final Entry before = compoundIndex.before(new Comparable<Index.Entry>() {
			@Override
			public int compareTo(Entry o)
			{
				return ValueDocument.VALUE_COMPARE.compare(find, o.match);
			}
		});
		
		return new Generator<Index.Entry>() {
			Index.Entry e = before;
			@Override
			protected Entry generate() throws NoSuchElementException
			{
				if (e == null)
					throw new NoSuchElementException();
				if (ValueDocument.VALUE_COMPARE.compare(find, e.match) != 0)
					throw new NoSuchElementException();
				Entry e2 = e;
				e = e.next();
				return e2;
			}
			
		};
	}

}
