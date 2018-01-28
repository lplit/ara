package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.*;


/** Décorateur sur Emitter qui simplifie la vie et qui compte le nombre de messages en transit.
 *  La classe concrète s'occupe d'envoyer les messages, celle-ci s'occupe de la réception.
 */

public abstract class EmitterCounter extends Observable implements Emitter, EDProtocol {

    // # of messages
    protected static int
            number_of_transits = 0,
            number_of_received = 0,
            number_of_sent = 0,
            number_of_delivered = 0,
            number_not_delivered = 0;

    protected int
            position_protocol = -1,
            this_pid = -1,
            verbose = 0,
            pid_controller = -1;

    protected static Boolean has_finished = false;

    protected Emitter emitter_impl;

    private static Set<Long> nodes_received;

    public void clear_set() {
        nodes_received.clear();
    }

    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);

        emitter_impl = emitter;
        nodes_received = new HashSet<>();

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");
        this.pid_controller = Configuration.getPid(prefix + "." + "controller", -1);

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (verbose != 0)
            System.err.println("Node " + node.getID()
                    + " EmitterCounter pid " + this_pid
                    + " recvd event " + event.toString()
                    + " pid " + pid);


        info();

        // This message is for me
        if (pid == this_pid && event instanceof Message) {

            number_of_transits--;
            Message msg = (Message) event;
            long sender = msg.getIdSrc();
            Message inner_msg = (Message) msg.getContent();

            // The sender is still within scope
            if (get_neighbors_in_scope(node).contains(Network.get((int) sender))) {
                deliverMessage(inner_msg, node);
            }
            // We're not within reach any more, do not deliver message
            else {
                number_not_delivered++;
                if (verbose != 0)
                    System.err.println(this_pid + " out of scope. Message " + msg.getPid() + "not delivered.");
            }
        }
        check_for_end();
    }

    public void notifyGossip() {
        int[] EndResults = {
                number_of_transits,
                number_of_received,
                number_of_sent,
                number_of_delivered,
                nodes_received.size()
        };
        has_finished = true;
        setChanged();
        notifyObservers(EndResults);
    }

    public void deliverMessage(Message m, Node n) {
        // Deliver message
        EDSimulator.add(
                0,
                new Message(m.getIdSrc(),
                        m.getIdDest(),
                        m.getTag(),
                        m.getContent(),
                        m.getPid()),
                n,
                m.getPid());
//        int protocol_pid = m.getPid();
//        Protocol prot = n.getProtocol(protocol_pid);

        nodes_received.add(n.getID());
//        notifyObservers(m);

        // Message delivre
        number_of_delivered++;
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

    public void decrement_transits() {
        number_of_transits--;
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
//    public Node getParentNode();



    public void info() {
        if (verbose != 0)
            System.err.println("transits " + number_of_transits + "; sent " + number_of_sent
                    + "; rcvd " + number_of_received + " dlvd " + number_of_delivered
            + "; not dlvd " + number_not_delivered);
    }

    private void check_for_end() {
        if (number_of_transits == 0 && has_finished == false) {
            if (verbose != 0)
                System.err.println(CommonState.getTime() + ": message transit finished");

            notifyGossip();
        }
        // This wasn't the last message
        else
            info();
    }
}
