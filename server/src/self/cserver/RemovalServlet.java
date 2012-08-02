package self.cserver;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONObject;

/**
 * Allows a user to unregister from the network. The user's
 * data does not get deleted, but rather it is marked as 
 * removed, allowing peers to be notified that the user 
 * is removed and thus allowing them to delete the user's
 * data from their devices. 
 */
@SuppressWarnings("serial")
public class RemovalServlet extends HttpServlet 
{
	private static final Logger Log = Logger.getLogger("RemovalServlet");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{
			Long removal_key = Long.parseLong(req.getParameter("removal_key"));
			DataModel d = (DataModel)pm.getObjectById(DataModel.class, removal_key);
			d.setMarkedRemoved(true);
			pm.makePersistent(d);
			
			// Returns JSON stating that the removal was a success.
			JSONObject responseObject = new JSONObject();
			responseObject.put(DataModel.REMOVED, true);
			resp.getWriter().write(responseObject.toString());
		}
		catch (Exception e)
		{
			Log.severe(e.toString());
		}
		finally
		{
			pm.close();
		}
	}
}
