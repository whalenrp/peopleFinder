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
 * However, if the 'skey' parameter is used, then all data 
 * except the data stored under the 'skey' value will be returned.
 * This is useful if a client wants to get all updates from peers.
 * 
 * @author lane
 *
 */
@SuppressWarnings("serial")
public class DownloadDataServlet extends HttpServlet 
{	
	private static final Logger Log = Logger.getLogger("DownloadDataServlet");
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(DataModel.class);
		if (req.getParameter("skey") != null)
		{
			Log.info("Skey = " + req.getParameter("skey"));
			q.setFilter("key != " + req.getParameter("skey"));
		}
		
		try
		{
			@SuppressWarnings("unchecked")
			List<DataModel> results = (List<DataModel>) q.execute();
			Log.info("Number of entities returned: " + results.size());
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
			e.printStackTrace();
		}
		finally
		{
			q.closeAll();
			pm.close();
		}
	}
}
