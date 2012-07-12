/**
 * 
 */
package nl.thanod.evade.remote;

import java.util.Map;

import nl.thanod.annotations.spi.ProviderFor;

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

		Object portObject = config.get("port");
		if (portObject != null && portObject instanceof Number) {
			return new JSONRemote(((Number) portObject).intValue());
		}

		return null;
	}
}
