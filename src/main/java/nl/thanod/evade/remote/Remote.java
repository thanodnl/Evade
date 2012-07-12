/**
 * 
 */
package nl.thanod.evade.remote;

import nl.thanod.evade.database.Database;

/**
 * @author nilsdijk
 */
public abstract class Remote implements Runnable
{
	public abstract void setDB(Database db);
}
