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

    protected int
            position_protocol = -1,
            this_pid = -1,
            verbose = 0,
            pid_controller = -1,
            number_of_sent = -1;

    protected static Boolean has_finished = false;
    protected Emitter emitter_impl;

    protected Set<Long> nodes_received;

    protected Set<Node> nodes_private;


    /**
     * PeerSim-compliant constructor
     * @param prefix PeerSim Config stuffs
     * @param emitter PeerSim Config stuffs
     */
    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        number_of_sent = 0;

        emitter_impl = emitter;
        nodes_received = new HashSet<>();
        nodes_private = new HashSet<>();

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");
        this.pid_controller = Configuration.getPid(prefix + "." + "controller", -1);

    }

    @Override
    /** Cette méthode doit incrémenter number_of_transits au moment de l'émission.
     */
    public abstract void emit(Node host, Message msg);


    /**
     * Gets the unique IDs of nodes in scope of @param host
     * @param host Node whose neighbours will be returned
     * @return List of node ids
     */
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


    /**
     * Returns Nodes in scope
     * @param host Source node
     * @return List of Node
     */
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
            nodes_private = new HashSet<>();
        } catch (CloneNotSupportedException e) {}

        res.emitter_impl.clone();
        return res;
    }

    /**
     * Clears the set of nodes that received the message
     * Used in-between broadcasts
     */
    public void clear_set() {
        nodes_received.clear();
    }


    // Getters

    @Override
    public int getLatency() {
        return emitter_impl.getLatency();
    }

    @Override
    public int getScope() {
        return emitter_impl.getScope();
    }

    public int get_number_of_sent() { return number_of_sent; }

    public Set<Long> get_ids_of_sent() { return nodes_received; }

    public static Boolean get_has_finished() {
        return has_finished;
    }


    /**
     * Since Network.get(i) does not guarantee the same not to be returned at two different times,
     * this function does exactly that but by working with the unique node IDs.
     * @param id
     * @return
     */
    public static Node get_node_by_id(long id) {
        Node n;
        for (int i=0; i < Network.size(); i++) {
            n = Network.get(i);
            if (n.getID() == id) return n;
        }
        return null;
    }
}

