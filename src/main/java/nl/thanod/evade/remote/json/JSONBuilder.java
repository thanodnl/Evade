/**
 * 
 */
package nl.thanod.evade.remote.json;

import java.util.Map;

import nl.thanod.annotations.spi.ProviderFor;
import nl.thanod.evade.remote.Remote;
import nl.thanod.evade.remote.RemoteBuilder;
import nl.thanod.evade.remote.SocketProvider;

/**
 * @author nilsdijk
 */
@ProviderFor(RemoteBuilder.class)
public class JSONBuilder extends RemoteBuilder
{
	public JSONBuilder()
	{
		super("JSON");
	}

	/*
	 * (non-Javadoc)
	 * @see nl.thanod.evade.remote.RemoteBuilder#build(java.util.Map)
	 */
	@Override
	public Remote build(Map<String, Object> config)
	{
		if (!this.name.equals(config.get("type")))
			return null;
		return new JSONRemote(SocketProvider.open(config));
	}
}
