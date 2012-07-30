/**
 * 
 */
package nl.thanod.evade.remote;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;

import nl.thanod.evade.collection.Table;
import nl.thanod.evade.database.Database;
import nl.thanod.evade.document.*;
import nl.thanod.evade.document.visitor.DocumentVisitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class JSONRemote extends Remote
{
	private static Logger log = LoggerFactory.getLogger(JSONRemote.class);

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
		GET,
		WHERE,
		PUT;
	}

	private Database db;
	private final ServerSocket socket;

	public JSONRemote(ServerSocket socket)
	{
		this.socket = socket;
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
		log.info("got a connection");
		InputStream inputStream = s.getInputStream();
		JSONTokener jto = new JSONTokener(new InputStreamReader(inputStream));
		try {
			while (jto.more()) {
				JSONObject jso = new JSONObject(jto);
				log.debug("Request: {}", jso);

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
				Mode mode = null;
				try {
					mode = Mode.valueOf(modename);
				} catch (IllegalArgumentException ball) {
				}

				JSONObject response = new JSONObject();
				response.put("session", sessionid);

				String collection;
				Table table;
				switch (mode) {
					case GET:
						// read stuff from database
						collection = jso.getString("collection");
						table = db.getCollection(collection);
						if (table == null) {
							send(s, createError("No such collection (" + collection + ")", sessionid));
							continue;
						}

						try {
							UUID key = UUID.fromString(jso.getString("key"));
							Document doc = table.get(key);
							// if you have a doc put it in the response
							// otherwise the data is null
							response.put("id", key.toString());
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
					case WHERE:
						if (!where(s, jso, sessionid))
							continue;
						break;
					case PUT:
						if (!put(s, jso, sessionid))
							continue;
						break;
					default:
						send(s, createError("Unknown mode " + modename, sessionid));
						continue;
				}
			}
		} catch (JSONException ball) {
			send(s, createError("Error while parsing json in request", -1));
			log.error("Error while interperting the json data", ball);
		}
	}

	/**
	 * @param s
	 * @param jso
	 * @param sessionid
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	private boolean put(Socket s, JSONObject jso, int sessionid) throws JSONException, IOException
	{
		String collectionName = jso.getString("collection");
		String key = jso.getString("key");
		UUID uuid;
		try {
			uuid = UUID.fromString(key);
		} catch (IllegalArgumentException ball) {
			send(s, createError("The key is not an UUID", sessionid));
			return false;
		}

		Document doc = JSONToDocument(jso.get("data"), System.currentTimeMillis(), s, sessionid);
		if (doc == null) {
			return false;
		}

		Table table = this.db.getOrCreateCollection(collectionName);

		table.update(uuid, doc);

		JSONObject response = new JSONObject();
		response.put("session", sessionid);
		response.put("ok", true);
		send(s, response);

		return true;
	}

	/**
	 * @param object
	 * @param version
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	private static Document JSONToDocument(Object object, long version, Socket s, int session) throws JSONException, IOException
	{
		if (object == null)
			return new NullDocument(version);
		if (object instanceof JSONObject) {
			JSONObject jso = (JSONObject) object;
			Iterator<String> keys = jso.keys();

			Map<String, Document> map = new HashMap<String, Document>();
			while (keys.hasNext()) {
				String key = keys.next();
				map.put(key, JSONToDocument(jso.get(key), version, s, session));
			}

			return new DictDocument(map, false);
		}
		if (object instanceof Number) {
			double d = ((Number) object).doubleValue();
			return new DoubleDocument(version, d);
		}
		if (object instanceof String) {
			return new StringDocument(version, (String) object);
		}
		send(s, createError("Type " + object.getClass() + " not supported by evade", session));
		return null;
	}

	private boolean where(Socket s, JSONObject jso, int sessionid) throws JSONException, IOException
	{
		String collection = jso.getString("collection");
		Table table = db.getCollection(collection);
		if (table == null) {
			send(s, createError("No such collection (" + collection + ")", sessionid));
			return false;
		}
		JSONArray query = null;
		try {
			query = jso.getJSONArray("query");
		} catch (JSONException ball) {
			send(s, createError("Query is not there or it isn't an array", sessionid));
			return false;
		}

		if (query == null || query.length() != 2) {
			send(s, createError("No valid query found. A valid query contains 2 items in an array", sessionid));
			return false;
		}

		String field = query.getString(0);
		String content = query.getString(1);
		DocumentPath path = new DocumentPath(field);

		int limit = 0;
		if (jso.has("limit"))
			limit = jso.getInt("limit");
		int count = 0;

		Iterator<Document.Entry> it = table.iterator();
		while (it.hasNext()) {
			Document.Entry e = it.next();

			Document d = e.doc.get(path);
			if (d == null)
				continue;
			if (!(d instanceof StringDocument))
				continue;

			StringDocument sd = (StringDocument) d;
			if (sd.value.toLowerCase().startsWith(content)) {
				JSONObject response = new JSONObject();
				response.put("session", sessionid);
				response.put("data", e.doc.accept(JSONVisitor.INSTANCE));
				response.put("id", e.id.toString());
				send(s, response);
				count++;
				if (limit == count)
					break;
			}

		}

		JSONObject response = new JSONObject();
		response.put("session", sessionid);
		response.put("eof", true);
		response.put("count", count);
		send(s, response);

		return true;
	}

	public static void send(Socket s, JSONObject data) throws IOException
	{
		try {
			log.debug("send {}", data);
			Writer writer = new OutputStreamWriter(s.getOutputStream());
			data.write(writer);
			writer.flush();
		} catch (JSONException ball) {
			log.error("Unable to write json to socket", ball);
		}
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	private static JSONObject createError(String reason, int packetid)
	{
		JSONObject error = new JSONObject();
		try {
			error.put("session", packetid);
			error.put("error", reason);
		} catch (JSONException ball) {
			log.error("Error while creating json object", ball);
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
		if (this.socket == null) {
			log.error("Could not start JSON server because there is now socket to use");
			return;
		}

		try {
			while (!this.socket.isClosed()) {
				log.info("Waiting for connection");
				final Socket s = this.socket.accept();

				new Thread(new Runnable() {

					@Override
					public void run()
					{

						try {
							handleConnection(s);
						} catch (IOException ball) {
							ball.printStackTrace();
						} finally {
							// close connection affter beeing finished
							try {
								s.close();
							} catch (IOException ball) {
							}
						}
					}
				}).start();
				// connection is handled within this thread so only one connection is possible

			}
			log.info("Socket {} closed", this.socket);
		} catch (IOException ball) {
			log.error("Error while accepting socket", ball);
		} finally {
			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException ball) {
					log.error("could not close socket", ball);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "JSONRemote(socket:" + this.socket + ")";
	}
}
