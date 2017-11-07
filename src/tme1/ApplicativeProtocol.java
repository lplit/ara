package tme1;

import java.util.HashMap;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;



public  class ApplicativeProtocol implements EDProtocol{
	
	private static final String PAR_BROADCASTID = "broadcast";
	private long variable=0;
	private boolean broadcast_done=false;
	private Map<Long,Integer> nbMessReceived;
	
	
	private final int protocol_id;
	private final int broadcast_protocol_id;
	
	
	public  ApplicativeProtocol(String prefix) {
		nbMessReceived = new HashMap<Long, Integer>();
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		broadcast_protocol_id=Configuration.getPid(prefix+"."+PAR_BROADCASTID);
	}
	
	public long getVariable(){
		return variable;
	}
		
	public Object clone(){
		ApplicativeProtocol ap = null;
		try { ap = (ApplicativeProtocol) super.clone();
			ap.nbMessReceived=new HashMap<Long, Integer>();
		
		}
		catch( CloneNotSupportedException e ) {} // never happens
		return ap;
	}
	
	
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive Message for wrong protocol");
		}
		
		if(event instanceof Message){
			Message mess=(Message)event;
			
			System.out.println("time "+CommonState.getTime()+" Node "+node.getID()+" receive message "+mess.getTag()+" from "+mess.getIdSrc()+" : "+mess.getContent());
			
			if(mess.getTag().equals("plus")){
				variable+=(Long)mess.getContent();	
			}else if (mess.getTag().equals("fois")){
				variable*=(Long)mess.getContent();			
			}else{
				throw new RuntimeException(node.getID()+" receives unknown message tag");
			}
			if(this.nbMessReceived.containsKey(mess.getIdSrc())){
				int val = nbMessReceived.get(mess.getIdSrc());
				this.nbMessReceived.put(mess.getIdSrc(), val+1 );
			}else{
				this.nbMessReceived.put(mess.getIdSrc(), 1 );
			}
			
			if(shouldBroadcast(node)){
				
				BroadcastModif(node);
			}		
		}
		
	}
	
	private boolean shouldBroadcast(Node host) {
		
		if(broadcast_done){
			return false;
		}
		
		//trouver le noeud fiable d'ID inférieur le plus élevé
		Node winner = null;
		for(int i=0;i< Network.size();i++){
			Node n =Network.get(i);
			if(n.getID()>= host.getID()){
				continue;
			}
			if(n.getFailState() == Fallible.OK ){//on triche ici sur la détection de défaillance
				if(winner == null){
					winner=n;
				}else if(n.getID() > winner.getID()){
					winner=n;
				}
			}
		}
		if (winner == null){//personne avant moi n'est vivant
			return true;
		}
		
		
		
		if(!nbMessReceived.containsKey(winner.getID())){
			return false;
		}
		if(nbMessReceived.get(winner.getID()) < 2  ){
			return false;
		}
				
		return true;
	}

	public void BroadcastModif(Node src) {
		System.out.println("time "+CommonState.getTime()+" Node "+src.getID()+" make broadcast");
		long idSrc=src.getID();
		BroadcastProtocol b= (BroadcastProtocol) src.getProtocol(broadcast_protocol_id);
		b.broadcast(src, new Message(idSrc, -2, "plus", new Long(idSrc+1), protocol_id));
		b.broadcast(src, new Message(idSrc, -2, "fois", new Long(idSrc+1), protocol_id));
		broadcast_done=true;
		
	}	
}