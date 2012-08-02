package self.cserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

/**
 * Servlet used to download data from the server. There are two
 * use cases. First, if a simple GET request is received, the 
 * servlet will return all data stored in the server database. 
 * However, if the 'key' parameter is used, then all data 
 * except the data stored under the 'key' value will be returned.
 * This is useful if a client wants to get all updates from peers.
 */
@SuppressWarnings("serial")
public class DownloadDataServlet extends HttpServlet 
{	
	private static final Logger Log = Logger.getLogger("DownloadDataServlet");
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(DataModel.class);
		
		String filter = ":p.contains(connectionType)";
		if (req.getParameter(DataModel.KEY) != null)
		{
			Log.info("key = " + req.getParameter(DataModel.KEY));
			filter += " && key != " + req.getParameter(DataModel.KEY);
		}
		q.setFilter(filter);
		
		// Network types Mixed and Client-Server poll this endpoint. If Client-Server,
		// don't send back data from Peer-to-Peer items.
		List<String> containsList = new ArrayList<String>();
		containsList.add(ConnectionType.MIXED.name());
		containsList.add(ConnectionType.CLIENT_SERVER.name());
		if (ConnectionType.getConnectionType(req.getParameter(DataModel.CONN_TYPE)) == ConnectionType.MIXED)
		{
			containsList.add(ConnectionType.PEER_TO_PEER.name());
		}
		
		try
		{
			@SuppressWarnings("unchecked")
			List<DataModel> results = (List<DataModel>) q.execute(containsList);
			List<JSONObject> toReturn = new ArrayList<JSONObject>();
			if (!results.isEmpty())
			{
				for (DataModel d : results)
				{
					toReturn.add(d.toJSON());
				}
			}
			
			JSONArray a = new JSONArray(toReturn);
			resp.getWriter().write(a.toString());
		}
		catch (Exception e)
		{
			Log.severe(e.toString());
		}
		finally
		{
			q.closeAll();
			pm.close();
		}
	}
}
