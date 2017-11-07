package tme1;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class BasicBroadcast implements EDProtocol, BroadcastProtocol {

	private static final String PAR_TRANSPORT = "transport";
	
	
	private final int transport_id;
	private final int protocol_id;
	
	public BasicBroadcast(String prefix) {
		transport_id = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
	}
	
	
	@Override
	public void broadcast(Node src, Message m) {
		
		for(int i = 0 ; i< Network.size();i++){
			Node dst = Network.get(i);
			long idDest=Network.get(i).getID();
			Object ev = new Message(src.getID(), idDest, "basicbroadcast", m,protocol_id);
			Transport tr = (Transport)src.getProtocol(transport_id);
			tr.send(src, dst, ev, protocol_id);
			
		}

	}
	
	@Override
	public Object clone(){
		BasicBroadcast bb = null;
		try { bb = (BasicBroadcast) super.clone();}
		catch( CloneNotSupportedException e ) {} // never happens
		return bb;
	}
	

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		if(event instanceof Message ){
			Message m = (Message)event;
			deliver(node,m);
		}

	}
	
	
	@Override
	public void deliver(Node host, Message m) {
		int pid_dessus=((Message)m.getContent()).getPid();
		((EDProtocol)host.getProtocol(pid_dessus)).processEvent(host, pid_dessus, m.getContent()); 

	}

}
