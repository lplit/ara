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
            number_of_delivered = 0,
            number_of_retransmits = 0,
            number_of_no_transmits = 0,
            number_of_nodes_retransmitted = 0,
            number_of_nodes_received = 0,
            last_id = -1;

    private int node_retransmitted = 0;

    private static long last_initiator = -1;

    private Set<String> received_messages = new HashSet<>();

    private static Set<Long> nodes_ids_received;

    private int verbose = 0;
    private final static String tag_gossip = "Gossip";

    private final int this_pid;

    public GossipProtocolImpl(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);
        number_of_transits = 0;
        number_of_sent = 0;
        node_retransmitted = 0;
        nodes_ids_received = new HashSet<>();

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
        number_of_delivered = 0;
        number_of_sent = 0;
        number_of_transits = 0;
        number_of_received = 0;
        number_of_retransmits = 0;
        number_of_no_transmits = 0;
        number_of_nodes_retransmitted = 0;
        nodes_ids_received.clear();


        node_retransmitted = 1; // initiator so transmits so retransmits

        GossipData data = new GossipData(id, id_initiator);
        data.id = id;
        data.id_initiator = id_initiator;
        last_id = id;
        int emitter_pid = Configuration.lookupPid("emitter");
        EmitterCounter emitter = (EmitterCounter) host.getProtocol(emitter_pid);



        emit_if_needed(host, data);
        if (number_of_sent == 0) { // émission nulle, broadcast terminé, le noeud est seultout :(
            if (verbose != 0) {
                System.err.println("Node " + host.getID() + " terminated broadcast alone");
            }
            notifyGossip();
        }

    }

    /**
     * Prepares the results structure, sets modified flag to true
     * and notifies observers with the results structure
     */
    public void notifyGossip() {
        int[] EndResults = {
                number_of_transits,
                number_of_received,
                number_of_sent,
                number_of_delivered,
                last_id,
                number_of_retransmits,
                number_of_no_transmits
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

    private void emit_if_needed(Node node, GossipData data) {
        int emitter_pid = Configuration.lookupPid("emitter");
        int local_sent = 0;
        EmitterCounter emitter = (EmitterCounter) node.getProtocol(emitter_pid);


        if (!received_messages.contains(data.toString())) {

            received_messages.add(data.toString());

            nodes_ids_received.add(node.getID());

            emitter.emit(node, new Message(
                    node.getID(),
                    -1,
                    tag_gossip,
                    data,
                    this_pid)
            );
            local_sent = emitter.get_number_of_sent();
            number_of_transits += local_sent;
            number_of_sent += local_sent;
            if (node.getID() != data.id_initiator) {
                node_retransmitted = 0;
                number_of_retransmits++;

                }


            if (verbose != 0)
                System.err.println("Node " + node.getID() + " emitted " + data.toString() + " "
                        + local_sent + " times " + number_of_retransmits + " retransmits");
        }
        else { // received_messages alread contains message.
            // Not retransmitting.
            if (node_retransmitted == 0) {
                number_of_no_transmits++;
                node_retransmitted = 1;
            }

            if (verbose != 0)
                System.err.println("Node " + node.getID() + " gossip " + data.toString() + " already treated");

        }
    }


    @Override
    public void processEvent(Node node, int pid, Object event) {
        int emitter_pid = Configuration.lookupPid("emitter");
        EmitterCounter emitter = (EmitterCounter) node.getProtocol(emitter_pid);
        if (verbose != 0) {
            System.err.println("Node " + node.getID() + " GossipProtocol xfers " + number_of_transits + " sent " + number_of_sent);

        }
        number_of_transits--;

        if (!nodes_ids_received.contains(node.getID())) {
            number_of_received++;
            nodes_ids_received.add(node.getID());
        }

        if (event instanceof Message) {

            Message msg = (Message) event;

            if (EmitterCounter.get_neighbor_ids_in_scope(node).contains(msg.getIdSrc())) {
                number_of_delivered++;

                // le voisin est toujours dans le scope, on délivre donc le GossipMessage
                    GossipData data = (GossipData) msg.getContent();
                    last_id = data.id;
                    // À la première réception du GossipMessage, on ré-emet
                    emit_if_needed(node, data);

            }
            else {
                if (verbose != 0)
                    System.err.println(node.getID() +" TOO FAR FROM " + msg.getIdSrc());
                  return;
            }
            }


            if (verbose != 0)
                System.err.println("Node broadcasted " + number_of_transits + " xfers");

        if (number_of_transits == 0) {
            if (verbose != 0)
                System.err.println("BROADCAST FINISHED\n\n\n\n");
            notifyGossip();
        }

    }

    }



