/**
 * 
 */
package nl.thanod.evade.database;

import java.io.File;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * @author nilsdijk
 */
public class Parameters
{
	public static class FileConverter implements IStringConverter<File>
	{
		@Override
		public File convert(String value)
		{
			return new File(value);
		}
	}

	@Parameter(names = { "--config" }, description = "Path to the configuration file", converter = FileConverter.class)
	private File config;

	/**
	 * Look at default places to find the configuration file Currently it just
	 * returns a static file wether it exists or not.
	 * <p>
	 * Future: look at /etc/evade/config.yml and ~/.evade/config.yml etc.
	 * @return
	 */
	public File getConfigFile()
	{
		if (this.config != null)
			return this.config;

		return new File("config", "config.yml");
	}

	@Override
	public String toString()
	{
		return "Parameters [config=" + config + "]";
	}

	public static Parameters parse(String... args)
	{
		Parameters params = new Parameters();
		new JCommander(params, args);
		return params;
	}
}
