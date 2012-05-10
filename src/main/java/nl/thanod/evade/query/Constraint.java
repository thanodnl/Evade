/**
 * 
 */
package nl.thanod.evade.query;

import nl.thanod.evade.document.*;
import nl.thanod.evade.document.modifiers.Modifier;

/**
 * @author nilsdijk
 */
public interface Constraint
{

	public Modifier getModifier();

	public boolean test(BooleanDocument doc);

	public boolean test(StringDocument doc);

	public boolean test(NullDocument doc);

	public boolean test(DictDocument doc);

	public boolean test(IntegerDocument doc);

	public boolean test(LongDocument doc);

	public boolean test(UUIDDocument doc);

	public boolean test(DoubleDocument doc);

	public boolean test(FloatDocument doc);

}