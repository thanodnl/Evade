/**
 * 
 */
package nl.thanod;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import nl.thanod.evade.collection.kdindex.*;
import nl.thanod.evade.document.IntegerDocument;
import nl.thanod.evade.document.StringDocument;
import nl.thanod.evade.document.ValueDocument;

/**
 * @author nilsdijk
 */
public class KDIndexCreator
{
	public static void main(String... args) throws IOException
	{
		String name = "names";
		File data = new File("data", name);
		//		File ssname = new File(data, name + "0.sstable");
		//		SSTable table = new SSTable(ssname);

		//		Table table = Table.load(data, name);
		//		IndexDescriptor idx1 = new IndexDescriptor(new DocumentPath("name"), LowerCase.INSTANCE); // axis one will index the names in lower case
		//		IndexDescriptor idx2 = new IndexDescriptor(new DocumentPath("name"), LengthModifier.INSTANCE); // axis two will index the length of the names
		//
		//		List<KDEntry> list = KDEntry.list(table, idx1, idx2);
		//		
		//		Collections.sort(list, new KDEntry.Sorter(0));
		//		System.out.println("name: " + list.get(0));
		//		Collections.sort(list, new KDEntry.Sorter(1));
		//		System.out.println("length: " + list.get(0));
		//
		//		KDTree tree = KDTree.tree(list);

		KDTree tree = new SSKDTree(new File(data, name + "3.kidx"));

		//		KDNode min = min(root, 0);
		//		printPath(min, null);

		ValueDocument find = new IntegerDocument(0, 16);
		KDConstraint[] constraints = new KDConstraint[] { new KDConstraint(new IntegerDocument(0, 14), null, 1), new KDConstraint(new StringDocument(0, "z"), null, 0) };
		Iterator<KDEntry> it = KDNode.filter(tree, constraints);
		//		Iterator<KDEntry> it = KDNode.all(root);
		int c = 0, f = 0;
		while (it.hasNext()) {
			KDEntry e = it.next();
			c++;
			if (KDConstraint.test(e, constraints)) {
				System.out.println(e);
				f++;
			}
		}
		System.out.println(f + " of " + c);

		long took = System.nanoTime();
		KDSerializer.serialize(data, name, tree);
		took = System.nanoTime() - took;

		System.out.println("persisting the kdtree took: " + took + "ns (" + took / 1000000 + "ms)");

		//KDTraversing t = new KDTraversing(root, 2, 0);
	}

	public static void printPath(KDMemNode node, KDNode took)
	{
		if (node.parent != null)
			printPath(node.parent, node);

		if (took == null)
			System.out.println("self on " + node);
		else if (took == node.left())
			System.out.println("left on " + node);
		else if (took == node.right())
			System.out.println("right on" + node);
		else
			System.out.println("UNKNOWN");
	}

	public static KDNode min(KDNode node, int d)
	{
		KDNode doc = node;
		if (node.left() != null) {
			KDNode tmp = min(node.left(), d);
			if (ValueDocument.VALUE_COMPARE.compare(tmp.entry().get(d), doc.entry().get(d)) < 0) {
				doc = tmp;
			}
		}
		if (node.right() != null) {
			KDNode tmp = min(node.right(), d);
			if (ValueDocument.VALUE_COMPARE.compare(tmp.entry().get(d), doc.entry().get(d)) < 0) {
				doc = tmp;
			}
		}
		return doc;
	}

}
