package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/** Décorateur sur Emitter qui simplifie la vie et qui compte le nombre de messages en transit.
 *  La classe concrète s'occupe d'envoyer les messages, celle-ci s'occupe de la réception.
 */

public abstract class EmitterCounter implements Emitter {

    protected int number_of_sent;

    protected int
            position_protocol = -1,
            this_pid = -1,
            verbose = 1,
            pid_controller = -1;

    protected static Boolean has_finished = false;

    protected Emitter emitter_impl;

    private static Set<Long> nodes_received;


    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        number_of_sent = 0;

        emitter_impl = emitter;
        nodes_received = new HashSet<>();

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");
        this.pid_controller = Configuration.getPid(prefix + "." + "controller", -1);

    }


    /**
     * The do-it-all function.
     * @param node Current node
     * @param pid Some PID
     * @param event Current event (message)
     */

    public void processEvent(Node node, int pid, Object event) {
        if (verbose != 0)
            System.err.println("Node " + node.getID()
                    + " EmitterCounter pid " + this_pid
                    + " recvd event " + event.toString()
                    + " pid " + pid);


        // This message is for me
        if (pid == this_pid && event instanceof Message) {

            Message msg = (Message) event;
            long sender = msg.getIdSrc();

            // The sender is still within scope
/*            if (get_neighbors_in_scope(node).contains(Network.get((int) sender)))
                deliverMessage(inner_msg, node);
            // We're not within reach any more, do not deliver message
            else {
                number_not_delivered++;
                if (verbose != 0)
                    System.err.println(this_pid + " out of scope. Message " + msg.getPid() + "not delivered.");
            }*/
        }
    }

    @Override
    /** Cette méthode doit incrémenter number_of_transits au moment de l'émission.
     */
    public abstract void emit(Node host, Message msg);




    public static List<Long> get_neighbor_ids_in_scope(Node host) {
        ArrayList<Long> list = new ArrayList<>();
        int position_protocol = Configuration.lookupPid("position");
        int emitter_protocol = Configuration.lookupPid("emitter");
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        Emitter emitter = (Emitter) host.getProtocol(emitter_protocol);

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < emitter.getScope() && n.getID() != host.getID()) {
                list.add(n.getID());
            }
        }
        return list;
    }

    public static List<Node> get_neighbors_in_scope(Node host) {
        ArrayList<Node> list = new ArrayList<>();
        int position_protocol = Configuration.lookupPid("position");
        int emitter_protocol = Configuration.lookupPid("emitter");
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        Emitter emitter = (Emitter) host.getProtocol(emitter_protocol);

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < emitter.getScope() && n.getID() != host.getID()) {
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
            nodes_received = new HashSet<>();
        } catch (CloneNotSupportedException e) {}

        res.emitter_impl.clone();
        return res;
    }

    /**
     * Print info if verbose
     *//*
    public void info() {
        if (verbose != 0)
            System.err.println("transits " + number_of_transits + "; sent " + number_of_sent
                    + "; rcvd " + number_of_received + " dlvd " + number_of_delivered
            + "; not dlvd " + number_not_delivered);
    }
    */



    /**
     * Clears the set of nodes that received the message
     * Used in-between broadcasts
     */
    public void clear_set() {
        nodes_received.clear();
    }

    @Override
    public int getLatency() {
        return emitter_impl.getLatency();
    }

    @Override
    public int getScope() {
        return emitter_impl.getScope();
    }

    public int get_number_of_sent() { return number_of_sent; }

    public static Boolean get_has_finished() {
        return has_finished;
    }

}

