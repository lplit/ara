package tme1;

import peersim.core.Node;
import peersim.core.Protocol;

public interface BroadcastProtocol extends Protocol{
	
	public void broadcast(Node src, Message m);
	public void deliver(Node host, Message m);
	
}
