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

@SuppressWarnings("serial")
public class AddressServlet extends HttpServlet 
{
	private static final Logger Log = Logger.getLogger("AddressServlet");
	
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
			Log.info("Number of addresses returned: " + results.size());
			List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
			if (!results.isEmpty())
			{
				for (DataModel d : results)
				{
					JSONObject o = new JSONObject();
					o.put("skey", d.getKey());
					o.put("ip", d.getIpAddress());
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
