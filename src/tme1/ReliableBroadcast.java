package tme1;

import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class ReliableBroadcast implements EDProtocol, BroadcastProtocol {

	private static final String PAR_TRANSPORT = "transport";
	
	private int cur_num_seq=0;
	private final int protocol_id;
	private final int transport_id;
	
	
	private Set< ReliableBroadcastMessage> rec;
	public ReliableBroadcast(String prefix){
		rec= new HashSet<ReliableBroadcastMessage>();
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		transport_id=Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	}
	
	@Override
	public Object clone(){
		ReliableBroadcast rb = null;
		try { rb = (ReliableBroadcast) super.clone();
		rb.rec= new HashSet<ReliableBroadcastMessage>();}
		catch( CloneNotSupportedException e ) {} // never happens
		return rb;
	}
	
	@Override
	public void broadcast(Node src, Message m){
		Transport tr = (Transport)src.getProtocol(transport_id);
		long idsrc=src.getID();
		for(int i = 0 ; i< Network.size();i++){
			Node dst = Network.get(i);
			long idDest=Network.get(i).getID();
			ReliableBroadcastMessage broadcast_m = new ReliableBroadcastMessage(idsrc, idDest, idsrc, cur_num_seq, "ReliableBroadcast", m,protocol_id);
			tr.send(src, dst, broadcast_m,protocol_id);
			
		}
		cur_num_seq++;
	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		if(event instanceof ReliableBroadcastMessage){
			ReliableBroadcastMessage rbm = (ReliableBroadcastMessage)event;
			if(rbm.getTag().equals("ReliableBroadcast") ){
				//System.err.println("time "+CommonState.getTime()+" Node "+node.getID()+" receive message "+rbm.getTag()+" from "+rbm.getIdSrc()+" : (sender="+rbm.getIdSender()+",seq ="+rbm.getNumseq()+")");
				if(!rec.contains(rbm)){
					rec.add(rbm);
					if(rbm.getIdSender() != node.getID()){
						Transport tr = (Transport)node.getProtocol(transport_id);
						for(int i = 0 ; i< Network.size();i++){
							Node dst = Network.get(i);
							long idDest=Network.get(i).getID();
							if(idDest != node.getID()){
								ReliableBroadcastMessage broadcast_m = new ReliableBroadcastMessage(node.getID(), idDest, rbm.getIdSender(), rbm.getNumseq(), "ReliableBroadcast",rbm.getContent(),protocol_id);
								
								//System.err.println("time "+CommonState.getTime()+" Node "+node.getID()+" send message "+rbm.getTag()+" to "+dst.getID()+" : (sender="+rbm.getIdSender()+",seq ="+rbm.getNumseq()+")");
								tr.send(node, dst, broadcast_m,protocol_id);
							}							
						}
					}
					//on délivre le message
					deliver(node, rbm);

					
				}
				
				
			}else{
				throw new RuntimeException(node.getID()+" receives unknown message tag in ReliableBroadcast protocol");
			}
		}
	}


	@Override
	public void deliver(Node host, Message m) {
		int pid_dessus=((Message)m.getContent()).getPid();
		((EDProtocol)host.getProtocol(pid_dessus)).processEvent(host, pid_dessus, m.getContent()); 
		
	}

}
