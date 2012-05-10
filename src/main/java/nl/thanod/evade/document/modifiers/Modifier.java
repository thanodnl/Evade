/**
 * 
 */
package nl.thanod.evade.document.modifiers;

import java.util.UUID;

/**
 * @author nilsdijk
 */
public interface Modifier
{

	public abstract String modify(String doc);

	public abstract int modify(int value);

	public abstract boolean modify(boolean value);

	public abstract long modify(long value);

	public abstract UUID modify(UUID value);

	public abstract double modify(double value);

	public abstract float modify(float value);

}