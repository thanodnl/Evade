/**
 * 
 */
package nl.thanod.evade.remote;

import java.util.Iterator;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

/**
 * @author nilsdijk
 */
public abstract class RemoteBuilder
{
	protected final String name;

	protected RemoteBuilder(String name)
	{
		this.name = name;
	}

	public final String getName()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return this.name + "Builder";
	}

	public abstract Remote build(Map<String, Object> config);

	public static Remote load(Map<String, Object> config)
	{
		Iterator<RemoteBuilder> builders = ServiceRegistry.lookupProviders(RemoteBuilder.class);
		while (builders.hasNext()) {
			RemoteBuilder builder = builders.next();
			Remote r = builder.build(config);
			if (r != null)
				return r;
		}
		return null;
	}
}
