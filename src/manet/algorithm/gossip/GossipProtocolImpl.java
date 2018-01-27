package manet.algorithm.gossip;

import manet.Message;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

;


public class GossipProtocolImpl implements GossipProtocol, EDProtocol {

    private int verbose = 1;
    private final static String tag_gossip = "Gossip";

  private final int this_pid;
    private List<String> received_messages;

    public GossipProtocolImpl(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);

        received_messages = new ArrayList<String>();

        if (verbose != 0) {
            System.err.println("Gossip up with pid " + this_pid);
        }
    }

    public String show_list() {
        return received_messages.toString();
    }

    public Boolean received(GossipData data) {
        if (received_messages.contains(data.toString())) {
            return true;
        } else
            return false;
    }


    /**
     * @param host:         noeud devant diffuser
     * @param id:           identifiant du message
     * @param id_initiator: noeud à l'origine de la diffusion
     */
    @Override
    public void initiateGossip(Node host, int id, long id_initiator) {
        GossipData data = new GossipData();
        data.id = id;
        data.id_initiator = id_initiator;

        int emitter_pid = Configuration.lookupPid("emitter");
//        System.out.println("getting protocol " + emitter_pid);
        EmitterCounter emitter = (EmitterCounter) host.getProtocol(emitter_pid);

        Message msg = new Message(
                host.getID(),
                -1,
                "Gossip",
                data,
                this_pid);
        if (!received_messages.contains(data.toString())) {
            received_messages.add(data.toString());
            emitter.emit(host, msg);
            if (verbose != 0)
                System.err.println("Node " + host.getID() + "Gossip: Done re-emitting");
        }
        else {
            System.err.println("Node " + host.getID() + " Gossip not re-emitting existing message " + msg);
        }
    }

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
            gpi.received_messages = new ArrayList<String>();
        } catch (CloneNotSupportedException e) {
        }
        return gpi;
    }


    @Override
    public void processEvent(Node node, int pid, Object event) {
        GossipData data;
        if (event instanceof Message) {
            data = (GossipData) ((Message) event).getContent();
            // À la première réception du GossipMessage, on ré-emet
            initiateGossip(node, data.id, data.id_initiator);
        }

    }
}


