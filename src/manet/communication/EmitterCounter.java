package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
import java.util.List;


/** Décorateur sur Emitter qui simplifie la vie et qui compte le nombre de messages en transit.
 *  La classe concrète s'occupe d'envoyer les messages, celle-ci s'occupe de la réception.
 */

public abstract class EmitterCounter implements Emitter, EDProtocol {

    // Nombre de messages en transit.
    protected static int number_of_transits = 0;
    protected static int number_of_received = 0;
    protected static Boolean has_finished = false;

    protected Emitter emitter_impl;


    protected int position_protocol;

    protected int this_pid;
    protected int verbose = 1;


    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);

        emitter_impl = emitter;

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");


    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (verbose != 0)
            System.err.println("Node " + node.getID() + " EmitterCounter recvd event " + event.toString());


        // Message for me
        Message msg = (Message) event;
        long sender = msg.getIdSrc();
        Message inner_msg = (Message) msg.getContent();
        // If we're still in reach of the sender
        info();
        if (pid == this_pid) {
//            System.err.println("Neighs " + get_neighbors_in_scope(node));
            if (get_neighbors_in_scope(node).contains(Network.get((int) sender))) {
                number_of_received++;
                if (verbose != 0) {
                    System.err.println(this_pid + "Decrementing transits to " + number_of_transits);
                    System.err.println(this_pid + "Incrementing number of received messages to " + number_of_received);
                }

                if (number_of_transits == 0) {
                    has_finished = true;
                    if (verbose != 0)
                        System.err.println("Message transit finished");
                } else {
                    has_finished = false;
                }

                if (verbose != 0)
                    System.err.println("EmitterCounter node " + node.getID() + " delivering " + event.toString() + " time " + CommonState.getTime());

                EDSimulator.add(
                        0,
                        new Message(inner_msg.getIdSrc(),
                                inner_msg.getIdDest(),
                                inner_msg.getTag(),
                                inner_msg.getContent(),
                                inner_msg.getPid()),
                        node,
                        inner_msg.getPid());
                number_of_transits--;
            }
            // We're not in reach any more
            else {
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
