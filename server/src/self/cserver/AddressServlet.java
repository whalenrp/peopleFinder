package self.cserver;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Returns the IP addresses (and other required info) of 
 * peers using MIXED and PEER_TO_PEER connections.
 */
@SuppressWarnings("serial")
public class AddressServlet extends HttpServlet 
{
	private static final Logger Log = Logger.getLogger("AddressServlet");
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(DataModel.class);
		
		// Filter by connection type and optionally by 
		// included key.
		String filter = ":p.contains(connectionType)";
		if (req.getParameter(DataModel.KEY) != null)
		{
			Log.info("Skey = " + req.getParameter(DataModel.KEY));
			filter += " && key != " + req.getParameter(DataModel.KEY);
		}
		Log.info(filter);
		q.setFilter(filter);
		
		try
		{
			@SuppressWarnings("unchecked")
			List<DataModel> results = (List<DataModel>) q.execute(
					Arrays.asList(ConnectionType.MIXED.name(), ConnectionType.PEER_TO_PEER.name()));
			List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
			if (!results.isEmpty())
			{
				for (DataModel d : results)
				{
					JSONObject o = new JSONObject();
					o.put(DataModel.KEY, d.getKey());
					o.put(DataModel.IP, d.getIpAddress());
					o.put(DataModel.CONN_TYPE, d.getConnectionType().name());
					jsonObjects.add(o);
				}
			}
			
			JSONArray a = new JSONArray(jsonObjects);
			resp.getWriter().write(a.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			q.closeAll();
			pm.close();
		}
	}
}
