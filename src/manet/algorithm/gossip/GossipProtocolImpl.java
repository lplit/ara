package manet.algorithm.gossip;

import manet.Message;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;


public class GossipProtocolImpl  extends Observable implements GossipProtocol, EDProtocol {
    private static int
            number_of_transits = 0,
            number_of_sent = 0,
            number_of_received = 0,
            number_of_delivered = 0;

    private static int last_id = -1;
    private static long last_initiator = -1;

    private Set<String> received_messages = new HashSet<>();

    private int verbose = 1;
    private final static String tag_gossip = "Gossip";

    private final int this_pid;

    public GossipProtocolImpl(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);
        number_of_transits = 0;
        number_of_sent = 0;

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
                tag_gossip,
                data,
                this_pid);

        if (!received_messages.contains(data.toString())) {
            received_messages.add(data.toString());
            emitter.emit(host, msg);
            number_of_transits += emitter.get_number_of_sent();
            if (verbose != 0)
                System.err.println("Node " + host.getID() + " added message " + data.toString());
        }
        else
        if (verbose != 0)
            System.err.println("Node " + host.getID() + " message " + msg + " already treated");

    }

    /**
     * Prepares the results structure, sets modified flag to true
     * and notifies observers with the results structure
     */
    public void notifyGossip() {
        int[] EndResults = {
                number_of_transits,
                number_of_sent,
                number_of_received,
                number_of_delivered
        };
        setChanged();
        notifyObservers(EndResults);
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

        if (event instanceof Message) {
            GossipData data = new GossipData();

            Message msg = (Message) event;
            if (EmitterCounter.get_neighbor_ids_in_scope(node).contains(msg.getIdSrc())) {
                // le voisin est toujours dans le scope, on délivre donc le GossipMessage
                data = (GossipData) msg.getContent();
                // À la première réception du GossipMessage, on ré-emet
                initiateGossip(node, data.id, data.id_initiator);



            }
            number_of_transits--;
            if (verbose != 0) {
                System.err.println("Node " + node.getID() + " GossipProtocol xfers " + number_of_transits);

                }
            if (number_of_transits == 0) {
                System.err.println("BROADCAST FINISHED\n\n\n\n");
                notifyGossip();
            }

        }

    }
}


