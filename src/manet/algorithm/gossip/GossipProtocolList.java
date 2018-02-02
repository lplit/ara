package manet.algorithm.gossip;

import manet.Message;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.HashSet;
import java.util.Observable;
import java.util.Scanner;
import java.util.Set;


public class GossipProtocolList extends Observable implements GossipProtocol, EDProtocol {
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

    private int
            timer_min = 0,
            timer_max = 0,
            tried_retransmit = 0;

    private int node_retransmitted = 0;

    private static long last_initiator = -1;

    private Set<String> received_messages = new HashSet<>();

    private Set<Long> neighbors_not_delivered;

    private GossipData current_data;

    private static Set<Long> nodes_ids_received;

    private int verbose = 0;
    private final static String tag_gossip = "Gossip";
    private final static String PAR_TIMER_MIN = "timer_min";
    private final static String PAR_TIMER_MAX = "timer_max";

    private final int this_pid;

    public GossipProtocolList(String prefix) {
        String tmp[] = prefix.split("\\.");
        this_pid = Configuration.lookupPid(tmp[tmp.length - 1]);
        number_of_transits = 0;
        number_of_sent = 0;
        node_retransmitted = 0;
        nodes_ids_received = new HashSet<>();
        current_data = new GossipData(-1, -1);
        neighbors_not_delivered = new HashSet<>();

        timer_min = Configuration.getInt(prefix + "." + PAR_TIMER_MIN);
        timer_max = Configuration.getInt(prefix + "." + PAR_TIMER_MAX);
        this.verbose = Configuration.getInt(prefix+".verbose");

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
        tried_retransmit = 0;

        int r = CommonState.r.nextInt(timer_max) + timer_min;


        GossipData data = new GossipData(id, id_initiator);
        data.id = id;
        data.id_initiator = id_initiator;
        current_data = data;
        last_id = id;
        int emitter_pid = Configuration.lookupPid("emitter");
        EmitterCounter emitter = (EmitterCounter) host.getProtocol(emitter_pid);



        emit_if_needed(host, data);
        if (emitter.get_number_of_sent() == 0) { // émission nulle, broadcast terminé, le noeud est seultout :(
            if (verbose != 0) {
                System.err.println("Node " + host.getID() + " terminated broadcast alone");
            }
//            notifyGossip();
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
        GossipProtocolList gpi = null;
        try {
            gpi = (GossipProtocolList) super.clone();
            gpi.received_messages = new HashSet<>();
            gpi.neighbors_not_delivered = new HashSet<>();
        } catch (CloneNotSupportedException e) {
        }
        return gpi;
    }

    private void emit_if_needed(Node node, GossipData data) {
        int emitter_pid = Configuration.lookupPid("emitter");
        int local_sent = 0;
        EmitterCounter emitter = (EmitterCounter) node.getProtocol(emitter_pid);
        int random_timer = CommonState.r.nextInt(timer_max) + timer_min;


        if (!received_messages.contains(data.toString())) {
            neighbors_not_delivered.clear();
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

            if (local_sent == 0) {
                /*  Rien ne se passe. Le noeud a reçu le message mais ne ré-emet pas, et ne retransmet pas donc.
                    En revanche, cette classe maintient la liste des voisins ayant potentiellement pas reçu le msg.
                    Initialiser cette liste.
                 */
                neighbors_not_delivered.addAll(EmitterCounter.get_neighbor_ids_in_scope(node));

            }



            if (verbose != 0)
                System.err.println("Node " + node.getID() + " emitted " + data.toString() + " "
                        + local_sent + " times " + number_of_retransmits + " retransmits");

            if (local_sent == 0 && tried_retransmit == 0) {
                if (verbose != 0)
                    System.err.println("Node " + node.getID() + " Gossip adding timer " + neighbors_not_delivered);
                // Avoiding the end of bcast. Decrement happens at timer receive.
                number_of_transits++;
                EDSimulator.add(random_timer, new Message(node.getID(), node.getID(), "GossipTimer", data, this_pid), node, this_pid);
            } else {

                if (node.getID() != data.id_initiator && node_retransmitted == 0) {
                    node_retransmitted = 1;
                    number_of_retransmits++;

                }
            }
        }
        else { // received_messages already contains message.
            // Not retransmitting. Arming a timer for another potential transmission.
            if (node_retransmitted == 0 && tried_retransmit == 1) {
                number_of_no_transmits++;
                node_retransmitted = 1;
            } else if (tried_retransmit == 0) {
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
                tried_retransmit = 1;

                if (tried_retransmit == 1 && local_sent == 0) {
                    if (verbose != 0)
                        System.err.println("Notifying of double broadcast termination, " + number_of_transits + " xfers");
                    notifyGossip(); // spaghetti case of emitter emitting alone twice
                }
                if (verbose != 0) {
                    System.err.println("Node " + node.getID() + " GossipList trying to re-emit, " + local_sent + " sent.");
                }
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


        if (verbose != 0) {
            System.err.println("Node " + node.getID() + " GossipList recvd " + event + " " + number_of_transits + "xfers");
        }

        if (event instanceof Message) {

            Message msg = (Message) event;
            if (msg.getTag().startsWith("GossipTimer")) {
                // Decrementing timer-related transit.
                number_of_transits--;
                if (!neighbors_not_delivered.isEmpty()) {
                    if (verbose != 0)
                        System.err.println("Node " + node.getID() + "GossipTimer recvd timer; list is " + neighbors_not_delivered);
                    emit_if_needed(node, (GossipData) msg.getContent());
                    tried_retransmit = 1;
                }

                return;
            }
            if (!nodes_ids_received.contains(node.getID())) {
                number_of_received++;
                nodes_ids_received.add(node.getID());
            }



            if (EmitterCounter.get_neighbor_ids_in_scope(node).contains(msg.getIdSrc())) {
                number_of_delivered++;

                // le voisin est toujours dans le scope, on délivre donc le GossipMessage
                    GossipData data = (GossipData) msg.getContent();

                    /*  On a reçu le message. Qu'on ré-emette ou pas, on enlève la liste des voisins ayant
                        déjà reçu le message.
                     */
                    Scanner s = new Scanner(msg.getTag());
                    while (s.hasNextLong()) {
                         neighbors_not_delivered.remove(s.nextLong());
                    }

                    // On met à jour le GossipData courant sur lequel on travaille, comme on l'a reçu pour la première fois
                    // On réinitialise par ailleurs les variables privées
                    if (current_data.id != data.id && current_data.id_initiator != data.id_initiator) {
                        current_data = data;
                        node_retransmitted = 0;
                        tried_retransmit = 0;
                        neighbors_not_delivered.clear();
                    }
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



