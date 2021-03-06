package manet.algorithm.gossip;

import peersim.core.Node;
import peersim.core.Protocol;

public interface GossipProtocol extends Protocol {

	String PAR_GOSSIPPROTOCOL = "gossipprotocol";

	/*Permet de déclencher une diffusion identifiée par id, dont le noeud source
	 *  est le noeud id_initiator*/
    void initiateGossip(Node host, int id, long id_initiator);

	String show_list();
}
