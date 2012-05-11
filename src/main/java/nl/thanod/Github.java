/**
 * 
 */
package nl.thanod;

import java.io.*;
import java.util.UUID;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.document.DocumentBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author nilsdijk
 */
public class Github
{
	public static void main(String... args) throws IOException, JSONException
	{
		File data = new File("data");
		Table table = Table.load(data, "github");

		File dir = new File(System.getProperty("user.home"), "githubdata");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".json");
			}
		});

		for (int i = 0; i < files.length; i++) {
			System.out.println("Loaded " + i + "/" + files.length + " (" + table.size() + ")");
			load(table, files[i]);
		}
		table.persist();
	}

	private static void load(Table t, File f) throws FileNotFoundException, JSONException
	{
		FileInputStream fin = new FileInputStream(f);
		JSONTokener jto = new JSONTokener(new InputStreamReader(fin));
		while (jto.more()) {
			JSONObject jso = new JSONObject(jto);
			DocumentBuilder b = DocumentBuilder.start(System.currentTimeMillis());
			translate(b, jso);
			t.update(UUID.randomUUID(), b.make());
		}
		try {
			fin.close();
		} catch (IOException ball) {
			ball.printStackTrace();
		}
	}

	public static void dispatch(DocumentBuilder builder, String name, Object o) throws JSONException
	{
		if (o == null)
			return;

		if (o == JSONObject.NULL) {
			builder.putNull(name);
		} else if (o instanceof String) {
			String s = o.toString();
			if (s.length() > Short.MAX_VALUE)
				s = s.substring(0, Short.MAX_VALUE);
			builder.put(name, s);
		} else if (o instanceof Boolean) {
			builder.put(name, (Boolean) o);
		} else if (o instanceof Integer) {
			builder.put(name, (Integer) o);
		} else if (o instanceof JSONObject) {
			builder.tree(name);
			translate(builder, (JSONObject) o);
			builder.pop();
		} else if (o instanceof JSONArray) {
			builder.tree(name);
			translate(builder, (JSONArray) o);
			builder.pop();
		} else {
			System.err.println("not handeled class: " + o.getClass().getCanonicalName());
			System.out.println(o);
		}
	}

	public static void translate(DocumentBuilder builder, JSONObject jso) throws JSONException
	{
		JSONArray names = jso.names();
		if (names == null)
			return;
		for (int i = 0; i < names.length(); i++) {
			String name = names.getString(i).toString();
			dispatch(builder, name, jso.get(name));
		}
	}

	public static void translate(DocumentBuilder builder, JSONArray arr) throws JSONException
	{
		for (int i = 0; i < arr.length(); i++)
			dispatch(builder, Integer.toString(i), arr.get(i));
	}
}
