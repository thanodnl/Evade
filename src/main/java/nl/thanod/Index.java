/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.collection.index.Memdex;
import nl.thanod.evade.document.modifiers.LowerCase;
import nl.thanod.evade.query.Constraint;
import nl.thanod.evade.query.string.StartsWithConstraint;

/**
 * @author nilsdijk
 */
public class Index
{
	public static void main(String... args) throws IOException
	{
		
		Table t = Table.load(new File("data"), "out");

		Constraint c = new StartsWithConstraint(new LowerCase(), "zh1");
		List<String> path = new ArrayList<String>();
		path.add("name");
		Memdex.persistSortedIndex(t, path, c);
	}
}
