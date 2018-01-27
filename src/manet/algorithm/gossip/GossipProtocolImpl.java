package manet.algorithm.gossip;

import manet.Message;
import manet.communication.Emitter;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.HashMap;
import java.util.Map;

;


public class GossipProtocolImpl implements GossipProtocol, EDProtocol {

    private int verbose = 0;
    private final static String tag_gossip = "Gossip";

  private final int this_pid;
    private Map<GossipData, Integer> received_messages;

    public GossipProtocolImpl(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);

        received_messages = new HashMap<GossipData, Integer>();

        if (verbose != 0) {
            System.err.println("Gossip up with pid " + this_pid);
        }
    }

    public String show_map() {
        if (verbose != 0) {
            System.err.println("Gossip map is '" + received_messages + "'");
        }

        if (received_messages.isEmpty()) {
            return "";
        }
        else return received_messages.toString();
    }

    public Integer received(GossipData data) {
        if (received_messages.containsKey(data)) {
            return received_messages.get(data);
        } else
            return 0;
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
        if (!received_messages.containsKey(data)) {
            received_messages.put(data, 0);
            System.err.println("Re-emitting");
            emitter.emit(host, msg);
            System.err.println("Node " + host.getID() + "Gossip: Done re-emitting");
        }
        else {
            received_messages.put(data, received_messages.get(data) + 1);
        }
    }

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
            gpi.received_messages = new HashMap<GossipData, Integer>();
        } catch (CloneNotSupportedException e) {
        }
        return gpi;
    }


    @Override
    public void processEvent(Node node, int pid, Object event) {
        GossipData data;
        if (event instanceof GossipData) {
            data = (GossipData) event;
            // À la première réception du GossipMessage, on ré-emet
            initiateGossip(node, data.id, data.id_initiator);
        }

    }
}


