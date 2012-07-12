/**
 * 
 */
package nl.thanod.evade.remote;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.document.*;
import nl.thanod.evade.document.visitor.DocumentVisitor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author nilsdijk
 */
public class JSONRemote extends Remote
{
	public static class JSONVisitor extends DocumentVisitor<Object, Void>
	{

		public static final JSONVisitor INSTANCE = new JSONVisitor();

		private JSONVisitor()
		{
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.StringDocument, java.lang.Object)
		 */
		@Override
		public Object visit(StringDocument doc, Void data)
		{
			return doc.value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.NullDocument, java.lang.Object)
		 */
		@Override
		public Object visit(NullDocument doc, Void data)
		{
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.DictDocument, java.lang.Object)
		 */
		@Override
		public Object visit(DictDocument doc, Void data)
		{
			Map<String, Object> content = new TreeMap<String, Object>();
			for (Entry<String, Document> e : doc.entrySet())
				content.put(e.getKey(), e.getValue().accept(this));
			return new JSONObject(content);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.BooleanDocument, java.lang.Object)
		 */
		@Override
		public Object visit(BooleanDocument doc, Void data)
		{
			return doc.value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.IntegerDocument, java.lang.Object)
		 */
		@Override
		public Object visit(IntegerDocument doc, Void data)
		{
			return doc.value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.LongDocument, java.lang.Object)
		 */
		@Override
		public Object visit(LongDocument doc, Void data)
		{
			return doc.value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.UUIDDocument, java.lang.Object)
		 */
		@Override
		public Object visit(UUIDDocument doc, Void data)
		{
			return doc.value.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.DoubleDocument, java.lang.Object)
		 */
		@Override
		public Object visit(DoubleDocument doc, Void data)
		{
			return doc.value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * nl.thanod.evade.document.visitor.DocumentVisitor#visit(nl.thanod.
		 * evade.document.FloatDocument, java.lang.Object)
		 */
		@Override
		public Object visit(FloatDocument doc, Void data)
		{
			return doc.value;
		}
	}

	public enum Mode
	{
		GET;
	}

	private Database db;
	private final int port;

	public JSONRemote(int port)
	{
		this.port = port;
	}

	@Override
	public void setDB(Database db)
	{
		if (this.db != null)
			throw new IllegalStateException("The database is already provided");
		this.db = db;
	}

	public void handleConnection(Socket s) throws IOException
	{
		InputStream inputStream = s.getInputStream();
		JSONTokener jto = new JSONTokener(new InputStreamReader(inputStream));
		try {
			while (jto.more()) {
				JSONObject jso = new JSONObject(jto);
				System.out.println("received: " + jso);

				// interpert the request
				// test for request identifier
				if (!jso.has("session")) {
					send(s, createError("No session provided", -1));
					continue;
				}
				int sessionid = jso.getInt("session");

				// test for mode
				if (!jso.has("mode")) {
					send(s, createError("No mode specified", sessionid));
					continue;
				}

				String modename = jso.getString("mode");
				Mode mode = Mode.valueOf(modename);

				JSONObject response = new JSONObject();
				response.put("session", sessionid);

				switch (mode) {
					case GET:
						// read stuff from database
						String collection = jso.getString("collection");
						Table table = db.getCollection(collection);
						if (table == null) {
							send(s, createError("No such collection (" + collection + ")", sessionid));
							continue;
						}

						try {
							UUID key = UUID.fromString(jso.getString("key"));
							Document doc = table.get(key);
							// if you have a doc put it in the response
							// otherwise the data is null
							if (doc != null)
								response.put("data", doc.accept(JSONVisitor.INSTANCE));
							else
								response.put("data", (Object) null);
						} catch (IllegalArgumentException ball) {
							// Not an UUID
							response.put("error", "Key was not a UUID");
						}

						send(s, response);
						break;
					default:
						send(s, createError("Unknown mode " + modename, sessionid));
						continue;
				}
			}
		} catch (JSONException ball) {
			send(s, createError("Error while parsing json in request", -1));

			// TODO Auto-generated catch block
			System.err.println("Error while interperting the json data");
			ball.printStackTrace();
		}
	}

	public void send(Socket s, JSONObject data) throws IOException
	{
		try {
			Writer writer = new OutputStreamWriter(s.getOutputStream());
			data.write(writer);
			writer.flush();
		} catch (JSONException ball) {
			System.err.println("Unable to write json to socket");
			ball.printStackTrace();
		}
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	private JSONObject createError(String reason, int packetid)
	{
		JSONObject error = new JSONObject();
		try {
			error.put("session", packetid);
			error.put("error", reason);
		} catch (JSONException ball) {
			// TODO Auto-generated catch block
			ball.printStackTrace();
			return null;
		}
		return error;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		ServerSocket ss = null;

		try {
			ss = new ServerSocket(this.port);
		} catch (IOException ball) {
			// TODO Auto-generated catch block
			System.err.println("Unable to start the server");
			ball.printStackTrace();
			return;
		}

		try {
			while (true) {
				System.out.println("Waiting for connection");
				Socket s = ss.accept();

				// connection is handled within this thread so only one connection is possible
				handleConnection(s);

				// close connection affter beeing finished
				try {
					s.close();
				} catch (IOException ball) {
				}
			}
		} catch (IOException ball) {
			// TODO Auto-generated catch block
			System.err.println("Error while accepting socket");
			ball.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException ball) {
					ball.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "JSONRemote(port:" + this.port + ")";
	}

	public static void main(String... args)
	{
		Database db = new Database();

		// load github data into the database
		String name = "github";
		File data = new File("data", name);
		db.addCollection("github", Table.load(data, name));

		// make a JSON server and start it
		JSONRemote remote = new JSONRemote(2225);
		remote.setDB(db);
		remote.run();
	}
}
