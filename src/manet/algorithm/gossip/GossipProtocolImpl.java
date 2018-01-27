package manet.algorithm.gossip;

import manet.Message;
import manet.communication.Emitter;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;



public class GossipProtocolImpl implements GossipProtocol, EDProtocol {

    private final static String tag_gossip = "Gossip";

    private class GossipData {
        public int id;
        public long id_initiator;
    };

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
        GossipData data = new GossipData();
        data.id = id;
        data.id_initiator = id_initiator;

        int emitter_pid = Configuration.lookupPid("emitter");
//        System.out.println("getting protocol " + emitter_pid);
        Emitter emitter = (Emitter) host.getProtocol(emitter_pid);

        Message msg = new Message(
                host.getID(),
                -1,
                "Gossip",
                data,
                this_pid);

        emitter.emit(host, msg);
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
        if (event instanceof Message) { // blablabla un peu comme neighbors
        }
    }
}
