package tme2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import tme2_shadow.Task3;


public class SharedMemoryControleur implements Control{


	private static final String PAR_SHARED_VARIABLE="sharedvariables";

	public static final Object big_lock= new Object();
	public static Task[] processes;
	
	private static final Map<String,Integer> shm_pids=new HashMap<String,Integer>();//name var -> id protocol shm
	
	
	public static Collection<String> getNamesVar(){
		return shm_pids.keySet();
	}
		
	public SharedMemoryControleur(String prefix) {
		String shared_variables=Configuration.getString(prefix+"."+PAR_SHARED_VARIABLE);
		String names_var[]=shared_variables.split("_");
		for(String s : names_var){
			shm_pids.put(s, Configuration.lookupPid(s));
		}
		processes= new Task[Network.size()];
		for(int i=0;i<Network.size();i++){
			processes[i]=new Task(Network.get(i));
			processes[i].setDaemon(true);
		}
	}
		
	@Override
	public boolean execute() {
		try {
			synchronized(big_lock){
				for(int index_node =0; index_node< Network.size();index_node++){
				
					Node n=Network.get(index_node);
					if(!processes[index_node].isAlive()){//on teste si le thread a démarré
						//on le demarre et on attend son prochain access mémoire
						processes[index_node].start();
						big_lock.wait();
					}
					
					for(String name : shm_pids.keySet()){
						SharedRegister shm_protocol = (SharedRegister)n.getProtocol(shm_pids.get(name));

						if(shm_protocol.isFinished() ){
								synchronized (processes[index_node]) {
									processes[index_node].notify();
								}
								big_lock.wait();
						}

					}
			
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static  int read(Node host, String name_var) throws InterruptedException, NotClosedOperationException {		
		synchronized (processes[host.getIndex()]) {
			SharedRegister var = (SharedRegister)host.getProtocol(shm_pids.get(name_var));
			synchronized (big_lock) {
				//System.out.println("Node "+host.getID()+" init operation READ "+var.getName());
				var.read(host);
				big_lock.notify();
				
			}
			
			processes[host.getIndex()].wait();
			synchronized (big_lock) {	
				
				int res=((SharedRegister)host.getProtocol(shm_pids.get(name_var))).getLastReadValue();
				System.out.println("Time"+CommonState.getTime()+" Node "+host.getID()+" END READ "+var.getName()+" = "+res);
				var.closeOperation();
				big_lock.notify();
				return res;
			}
		}
		
	}
	public static  void write(Node host,  String name_var, int val) throws InterruptedException, NotClosedOperationException {
		synchronized (processes[host.getIndex()]) {
			SharedRegister var = (SharedRegister)host.getProtocol(shm_pids.get(name_var));
			synchronized (big_lock) {
				//System.out.println("Node "+host.getID()+" init operation WRITE "+var.getName()+" with val ="+val);
				var.write(host, val);
				big_lock.notify();
			}
		
			processes[host.getIndex()].wait();
			
			synchronized (big_lock) {	
				System.out.println("Time"+CommonState.getTime()+" Node "+host.getID()+" END WRITE "+var.getName()+" with val "+val);
				var.closeOperation();
				big_lock.notify();
			}
		}
		
	}
	
	
	
}