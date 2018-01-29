package manet.algorithm.gossip;

import manet.Message;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.HashSet;
import java.util.Set;


public class GossipProtocolImpl implements GossipProtocol, EDProtocol {
    private static int number_recvd = 0;

    private int verbose = 1;
    private final static String tag_gossip = "Gossip";

    private final int this_pid;
    private Set<String> received_messages = new HashSet<>();


    public GossipProtocolImpl(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);

        if (verbose != 0) {
            System.err.println("Gossip up with pid " + this_pid);
        }
    }

    public String show_list() {
        return received_messages.toString();
    }
    public Boolean received(GossipData data) {
        return received_messages.contains(data.toString());
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
                System.err.println("Node " + host.getID() + " added message " + data.toString());

        }
        else
        if (verbose != 0)
            System.err.println("Node " + host.getID() + " message " + msg + " already treated");

    }

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
            gpi.received_messages = new HashSet<>();
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


