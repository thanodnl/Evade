/**
 * 
 */
package nl.thanod.evade.document;

import java.util.*;

/**
 * @author nilsdijk
 */
public class AllOrders<E> implements Iterable<List<E>>{
	private List<List<E>> orders = new LinkedList<List<E>>();
	private List<E> items;

	public AllOrders(List<E> items) {
		this.items = items;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<List<E>> iterator() {
		return allOrders(this.items).iterator();
	}

	private List<List<E>> allOrders(List<E> list) {
		if (list.size() == 1){
			return Collections.singletonList(list);
		}
		List<List<E>> lists = new LinkedList<List<E>>();

		for (int i=0; i<list.size(); i++){
			E working = list.remove(i);
			for (List<E> l:allOrders(list)){
				List<E> ll = new LinkedList<E>();
				ll.add(working);
				ll.addAll(l);
				lists.add(ll);
			}
			list.add(i, working);
		}
		return lists;
	}

	public static void main(String... args) {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		l.add(5);
		for (List<Integer> lo:new AllOrders<Integer>(l)){
			System.out.println(lo);
		}
	}
}
