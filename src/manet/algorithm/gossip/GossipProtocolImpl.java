package manet.algorithm.gossip;

import manet.Message;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;

public class GossipProtocolImpl implements GossipProtocol, EDProtocol {

    private final int this_pid;

    public GossipProtocolImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);
    }


    /**
     * @param host: noeud devant diffuser
     * @param id: identifiant du message
     * @param id_initiator: noeud à l'origine de la diffusion
     */
    @Override
    public void initiateGossip(Node host, int id, long id_initiator) {
            Node n = Network.get(CommonState.r.nextInt()% Network.size());

            EDSimulator.add(0, new Message(n.getID(), 0, "Algorithm", "Zero", 0), n, 0);



    }

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
        } catch (CloneNotSupportedException e) {}
        return gpi;
    }


    @Override
    public void processEvent(Node node, int pid, Object event) {

    }
}
