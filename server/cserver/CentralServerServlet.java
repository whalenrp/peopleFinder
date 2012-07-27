package self.cserver;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class CentralServerServlet extends HttpServlet 
{	
	private static final Logger Log = Logger.getLogger("CentralServerServlet");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			JSONObject json_package = new JSONObject(req.getParameter("json_package"));
			DataModel d = new DataModel(json_package);
			pm.makePersistent(d);
			
			JSONObject responseObject = new JSONObject();
			responseObject.put("skey", d.getKey());
			resp.getWriter().write(responseObject.toString());
		} 
		catch (JSONException e) 
		{
			Log.severe(e.toString());
		}
		finally
		{
			pm.close();
		}
		
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
