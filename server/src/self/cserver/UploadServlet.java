package self.cserver;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

/**
 * General endpoint for uploading user data to the
 * server. All connection types use this servlet.
 * Clients may send varying amounts of user data 
 * depending on their connection type.
 */
@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet 
{	
	private static final Logger Log = Logger.getLogger("UploadServlet");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			JSONObject json_package = new JSONObject(req.getParameter("json_package"));
			DataModel d = new DataModel(json_package);
			pm.makePersistent(d);
			
			// Returns the generated key, which can be used
			// to access the user's data from the server's
			// database.
			JSONObject responseObject = new JSONObject();
			responseObject.put(DataModel.KEY, d.getKey());
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
