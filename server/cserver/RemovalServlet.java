package self.cserver;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class RemovalServlet extends HttpServlet 
{
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{
			Long removal_key = Long.parseLong(req.getParameter("removal_key"));
			DataModel d = (DataModel)pm.getObjectById(DataModel.class, removal_key);
			d.setMarkedRemoved(true);
			pm.makePersistent(d);
			
			JSONObject responseObject = new JSONObject();
			responseObject.put("removed", true);
			resp.getWriter().write(responseObject.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			pm.close();
		}
	}
}
