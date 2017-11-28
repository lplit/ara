package tme3;

import java.util.ArrayDeque;
import java.util.Queue;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import tme1.Message;

public class Tme3Application implements EDProtocol, Checkpointable {
	
	
	
	
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_TIME_CS = "timeCS";
	private static final String PAR_TIME_BETWEEN_CS = "timeBetweenCS";
	
	private static final long elected_node=0L;
	private static final long nil=-2L;
	
	private static final String REQUEST_TAG = "request";
	private static final String TOKEN_TAG = "token";
	
	private static enum State{tranquil, requesting, inCS}
	
	private final long timeCS;
	private final long timeBetweenCS;
	private final int transport_id;
	private final int protocol_id;
	
	//variables d'état de l'application
	private State state;
	private Queue<Long> next;
	private long last;
	private int nb_cs=0;

	
	//variable permettant de savoir si l'application est suspendue ou pas pour cause de recovery
	private boolean is_suspended=false;
	
	//permet d'identifier la dernière suspension d'exécution
	private int last_halt_id;
	
	@Override
	public NodeState getCurrentState(){
		NodeState res = new NodeState();
		res.saveVariable("state",state.name());
		res.saveVariable("next", new ArrayDeque<Long>(next) );
		res.saveVariable("last", new Long(last));
		res.saveVariable("nb_cs",nb_cs);
		return res;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(NodeState restored_state){
		
		this.next = new ArrayDeque<>( (Queue<Long> )restored_state.loadVariable("next" ))  ;
		this.last = (Long) restored_state.loadVariable("last");	
		this.nb_cs=(Integer) restored_state.loadVariable("nb_cs");
		changestate(CommonState.getNode(), State.valueOf(((String)restored_state.loadVariable("state"))));
		
		
	}
	
	
	public Tme3Application(String prefix) {
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		
		transport_id=Configuration.getPid(prefix+"."+PAR_TRANSPORT);
		timeCS=Configuration.getLong(prefix+"."+PAR_TIME_CS);
		timeBetweenCS=Configuration.getLong(prefix+"."+PAR_TIME_BETWEEN_CS);
		
	}
	
	public Object clone(){
		Tme3Application res= null;
		try { res = (Tme3Application) super.clone();}
		catch( CloneNotSupportedException e ) {} // never happens
		res.initialisation(CommonState.getNode());
		
		return res;
	}
	
	
	

	

	@SuppressWarnings("unchecked")
	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive an event for wrong protocol");
		}
		if(is_suspended){
			return;
		}
		if(event instanceof String){
			String ev= (String) event;
			String[] tmp = ev.split("_");
			int date_event = Integer.parseInt(tmp[1]);
			ev = tmp[0];
			if(date_event == last_halt_id){
				
				if(ev.equals("releaseCS")){
					nb_cs++;
					this.releaseCS(node);
				}else if(ev.equals("requestCS")){
					this.requestCS(node);
				}else{
					throw new RuntimeException("Receive unknown type event");
				}
			}else{
				System.out.println(node.getID()+" : ignoring obsolete event "+ev);
			}
		}else if(event instanceof Message) {
			Message m = (Message) event;
			if(m.getTag().equals(REQUEST_TAG)){
				this.receive_request(node, m.getIdSrc(), (Long)m.getContent());
			}else if(m.getTag().equals(TOKEN_TAG)){
				this.receive_token(node, m.getIdSrc(), (Queue<Long>) m.getContent());
			}else{
				throw new RuntimeException("Receive unknown type Message");
			}
			
		}else {
			throw new RuntimeException("Receive unknown type event");
		}
		

	}
	
	
	
	
	
	
	/////////////////////////////////////////// METHODES PRIVATE ////////////////////////////////////////////
	private void executeCS(Node host){
		System.out.println(host.getID()+" executing CS "+nb_cs+" : next= "+next.toString());
	}
	
	
	private void initialisation(Node host) {
		changestate(host,State.tranquil);
		next=new ArrayDeque<Long>();
		if(host.getID() == elected_node){
			last=nil;
		}else{
			last=elected_node;
		}
		
	}

	
	private void requestCS(Node host){
		changestate(host,State.requesting);
		if(last != nil){
			Transport tr= (Transport) host.getProtocol(transport_id);
			Node dest = Network.get((int)last);
			tr.send(host,dest, new Message(host.getID(), dest.getID(),  REQUEST_TAG, host.getID(), protocol_id), protocol_id);
			last=nil;
			return;//on simule un wait ici
		}
		changestate(host,State.inCS);
		//DEBUT CS
	}
	
	private void releaseCS(Node host){
		//System.out.println(host.getID()+" : releaseCS next="+next);
		changestate(host,State.tranquil);
		if(!next.isEmpty()){
			last=getLast(next);
			long next_holder = next.poll();//dequeue
			Transport tr= (Transport) host.getProtocol(transport_id);
			Node dest = Network.get((int)next_holder);
			tr.send(host,dest, new Message(host.getID(), dest.getID(),  TOKEN_TAG, new ArrayDeque<Long>(next), protocol_id), protocol_id);
			//System.out.println(host.getID()+" send token("+next+") to "+dest.getID());
			next.clear();
		}
	}
	
	
	private void  receive_request(Node host, long from, long requester){
		//System.out.println("Node "+host.getID()+" receive request from"+from+" for "+requester);
		Transport tr= (Transport) host.getProtocol(transport_id);
		if(last == nil){
			if(state != State.tranquil){
				next.add(requester);
				
			}else{
				Node dest = Network.get((int)requester);
				tr.send(host,dest, new Message(host.getID(), dest.getID(),  TOKEN_TAG, new ArrayDeque<Long>(), protocol_id), protocol_id);
				//System.out.println(host.getID()+" send token("+next+") to "+dest.getID()+" (no need)");
				last=requester;
			}
		}else{
			Node dest = Network.get((int)last);
			tr.send(host,dest, new Message(host.getID(), dest.getID(),  REQUEST_TAG, requester, protocol_id), protocol_id);
			last=requester;
		}
	}
	
	private void receive_token(Node host, long from,  Queue<Long> remote_queue){
		//System.out.println(host.getID()+" receive token("+remote_queue.toString()+") from "+from+" next ="+next.toString());
		remote_queue.addAll(next);
		next=remote_queue;
		changestate(host,State.inCS);
	}
	
	
	private void changestate(Node host, State s) {
		this.state=s;
		switch(this.state){
		case inCS:
			executeCS(host);
			schedule_release(host);
			break;
		case tranquil:
			schedule_Request(host);
			break;
		default:
		}
	}



	private static long getLast(Queue<Long> q) {
		Object tmp[] = q.toArray();
		return (Long)tmp[tmp.length-1];
	}

	
	private void schedule_release(Node host) {
		long min = (long )(timeCS * 0.8);
		long max = (long )(timeCS * 1.2);
		long res = CommonState.r.nextLong(max+min)+min;
		EDSimulator.add(res, "releaseCS_"+last_halt_id, host, protocol_id);
		
	}
	
	private void schedule_Request(Node host) {
		long min = (long )(timeBetweenCS * 0.8);
		long max = (long )(timeBetweenCS * 1.2);
		long res = CommonState.r.nextLong(max+min)+min;
		EDSimulator.add(res, "requestCS_"+last_halt_id, host, protocol_id);
		
	}


	
	@Override
	public void suspend() {
		is_suspended=true;		
	}


	
	@Override
	public void resume() {
		is_suspended=false;
		last_halt_id++;
	}
	

	
	

}
