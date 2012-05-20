/**
 * 
 */
package nl.thanod.evade.collection.kdindex;



/**
 * @author nilsdijk
 */
public class KDMemNode extends KDNode
{

	public KDMemNode parent;
	final KDEntry entry;
	final KDMemNode left;
	final KDMemNode right;

	protected KDMemNode(int depth, KDEntry entry, KDMemNode left, KDMemNode right)
	{
		super(depth);
		
		this.entry = entry;
		this.left = left;
		this.right = right;
		if (this.left != null)
			this.left.parent = this;
		if (this.right != null)
			this.right.parent = this;
	}

	@Override
	public String toString()
	{
		return this.entry + "<left: " + (left != null ? left.entry : "none") + " | right: " + (right != null ? right.entry : "null") + ">";
	}

	public int size()
	{
		int c = 1;
		if (left != null)
			c += left.size();
		if (right != null)
			c += right.size();
		return c;
	}
	
	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.kdindex.KDNode#left()
	 */
	@Override
	public KDNode left()
	{
		return this.left;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.kdindex.KDNode#right()
	 */
	@Override
	public KDNode right()
	{
		return this.right;
	}

	/* (non-Javadoc)
	 * @see nl.thanod.evade.collection.kdindex.KDNode#entry()
	 */
	@Override
	public KDEntry entry()
	{
		return this.entry;
	}
}
