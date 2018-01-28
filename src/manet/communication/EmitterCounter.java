package manet.communication;

import manet.Message;
import manet.algorithm.gossip.GossipProtocol;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


/** Décorateur sur Emitter qui simplifie la vie et qui compte le nombre de messages en transit.
 *  La classe concrète s'occupe d'envoyer les messages, celle-ci s'occupe de la réception.
 */

public abstract class EmitterCounter extends Observable implements Emitter, EDProtocol {

    // Nombre de messages en transit.
    protected static int number_of_transits = 0;
    protected static int number_of_received = 0;
    protected static Boolean has_finished = false;

    protected Emitter emitter_impl;


    protected int position_protocol;

    protected int this_pid;
    protected int verbose = 0;
    protected int pid_controller;

    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);

        emitter_impl = emitter;

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");
        this.pid_controller = Configuration.getPid(prefix + "." + "controller", -1);

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (verbose != 0)
            System.err.println("Node " + node.getID() + " EmitterCounter pid " + this_pid + " recvd event " + event.toString() + " pid " + pid);
        // Message for me
        // If we're still in reach of the sender
        info();
        if (pid == this_pid && event instanceof Message) {

            //System.out.println("INSTANCE OF MESSAGE");
            Message msg = (Message) event;

            if (msg.getTag() != "EMITTER") {
                System.err.println("Should not show this event " + event);
            }

            long sender = msg.getIdSrc();
            Message inner_msg = (Message) msg.getContent();

//            System.err.println("Neighs " + get_neighbors_in_scope(node));
            if (get_neighbors_in_scope(node).contains(Network.get((int) sender))) {
                number_of_transits--;

                if (verbose != 0) {
                    System.err.println(this_pid + "Decrementing transits to " + number_of_transits);
                    System.err.println(this_pid + "Incrementing number of received messages to " + number_of_received);
                }


            //    if (verbose != 0)
            //        System.err.println("EmitterCounter node " + node.getID() + " delivering " + inner_msg.toString() + " time " + CommonState.getTime());

                // Deliver message
                EDSimulator.add(
                        0,
                        new Message(inner_msg.getIdSrc(),
                                inner_msg.getIdDest(),
                                inner_msg.getTag(),
                                inner_msg.getContent(),
                                inner_msg.getPid()),
                        node,
                        inner_msg.getPid());

                // Message delivre, on increment
                number_of_received++;

                if (number_of_transits == 0) {
                    has_finished = true;
                    setChanged();
                    if (verbose != 0)
                        System.err.println("Message transit finished");
                    notifyObservers();
                } else {
                    has_finished = false;
                }
            }
            // We're not in reach any more
            else {
                System.out.println("Node" + node.getID() + " too far from ");

                int gpro = Configuration.lookupPid("gossip");
                GossipProtocol gossip = (GossipProtocol) node.getProtocol(gpro);
//                gossip.initiateGossip(node,((Message) msg.getContent()));

                // Message traite, mais pas a delivrer, faut decrement donc
                number_of_transits--;

                if (verbose != 0)
                    System.err.println(this_pid + " decrementing, left: " + number_of_transits);
            }
        }
    }

    @Override
    /** Cette méthode doit incrémenter number_of_transits au moment de l'émission.
     */
    public abstract void emit(Node host, Message msg);


    @Override
    public int getLatency() {
        return emitter_impl.getLatency();
    }

    @Override
    public int getScope() {
        return emitter_impl.getScope();
    }

    public static int get_number_of_transits(Message msg) {
        return number_of_transits;
    }

    public static Boolean get_has_finished() {
        return has_finished;
    }

    protected List<Node> get_neighbors_in_scope(Node host) {
        ArrayList<Node> list = new ArrayList<>();
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < getScope() && n.getID() != host.getID()) {
                list.add(n);
            }
        }
        return list;
    }


    @Override
    public Object clone(){
        EmitterCounter res=null;
        try {
            res=(EmitterCounter) super.clone();
        } catch (CloneNotSupportedException e) {}

        res.emitter_impl.clone();
        return res;
    }
//    public Node getParentNode();



    public void info() {
        if (verbose != 0)
            System.err.println("Transits: " + number_of_transits + " received " + number_of_received);
    }
}
