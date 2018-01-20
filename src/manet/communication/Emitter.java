package manet.communication;

import manet.Message;
import peersim.core.Node;
import peersim.core.Protocol;


public interface Emitter extends Protocol {

	public static final String PAR_LATENCY = "latency";
	public static final String PAR_SCOPE = "scope";
	public static final String PAR_POSITIONPROTOCOL = "positionprotocol";

	/*désigne tous les voisins accessibles*/
	public static final int ALL=-2;
	
	/*permet la simulation de l'envoie d'un message msg depuis host*/
	public void emit(Node host, Message msg);
	
	/*Renvoie la latence entre le moment où un site envoi un message et le moment où un voisin direct le reçoit*/
	public int getLatency();
	
	/*Renvoie le rayon d'emission/réception d'un noeud*/
	public int getScope();
	
}
