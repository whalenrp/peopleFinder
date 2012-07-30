package com.vanderbilt.people.finder;

public enum ConnectionType 
{
	CLIENT_SERVER,
	PEER_TO_PEER,
	MIXED;
	
	static ConnectionType getConnectionType(String name)
	{
		if (name.equals(PEER_TO_PEER.name()))
		{
			return PEER_TO_PEER;
		}
		else if (name.equals(MIXED.name()))
		{
			return MIXED;
		}
		else
		{
			return CLIENT_SERVER;
		}
	}
}
