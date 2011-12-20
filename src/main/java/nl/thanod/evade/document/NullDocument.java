/**
 * 
 */
package nl.thanod.evade.document;

/**
 * @author nilsdijk
 *
 */
public class NullDocument extends Document {

	/**
	 * @param version
	 */
	public NullDocument(long version) {
		super(version, Type.NULL);
	}

	@Override
	public String toString(){
		return super.toString() + "null";
	}
}
