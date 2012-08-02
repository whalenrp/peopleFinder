package com.vanderbilt.people.finder;


/**
 * Allows for compile-time type checking as opposed to 
 * error-prone raw strings. 
 */
public enum ConnectionType 
{
	CLIENT_SERVER,
	PEER_TO_PEER,
	MIXED;
	
	static ConnectionType getConnectionType(String name)
	{
		if (name != null)
		{
			if (name.equals(PEER_TO_PEER.name()))
			{
				return PEER_TO_PEER;
			}
			else if (name.equals(MIXED.name()))
			{
				return MIXED;
			}
			else if (name.equals(CLIENT_SERVER.name()))
			{
				return CLIENT_SERVER;
			}
		}
		
		return null;
	}
}
