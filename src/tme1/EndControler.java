package tme1;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;

public class EndControler implements Control {

	public EndControler(String prefix) {
		
	}
	
	@Override
	public boolean execute() {
		
		int pid = Configuration.lookupPid("applicative");
		System.out.println("################################# AFFICHAGE DES VALEURS ###########################");
		for(int i=0;i<Network.size();i++){
			Node node =Network.get(i);
			ApplicativeProtocol prot = (ApplicativeProtocol)node.getProtocol(pid);
			System.out.println("On node "+node.getID()+" variable = "+prot.getVariable()+"  ("+ (node.getFailState()==Fallible.OK?"alive":"dead")+")");
		}
				
		
		return false;
	}

}
