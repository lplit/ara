package manet;

import manet.algorithm.gossip.GossipData;
import manet.algorithm.gossip.GossipProtocolImpl;
import manet.communication.Emitter;
import manet.communication.EmitterCounter;
import manet.positioning.Position;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.graph.GraphAlgorithms;

import java.util.*;

public class GossipController implements Control, Observer {

    private int position_pid;
    private Observable last_observable = null;
    private static final String PAR_NB_DIFFUSIONS = "nb_diffusions";
    private static final String PAR_POSITION = "position";
    private static final String PAR_EMITTER = "emitter";

    private static Set<Integer> received;
    private static Map<Integer, GossipData> broadcasts;

    /* Système de rounds rondes genre. Tuple <id_séquence, size du set>
        Le set == tous les noeuds qui ont reçu le message <id> (Set<NodeId>)

        On stocke la valeur courante du set à la première itération (== 1 comme y'a déjà l'initiateur)
        - à chaque round, le receveur s'ajoute au set (incrémente la taille du set)
        - on compare la taille du set de l'itération courante avec la valeur stockée. Si ça a cbangé, ça veut dire qu'on
          est dans le même broadcast mais qu'on a atteint des nouveaux voisins entre-temps, donc c'est le même reund.
          - SINON si size(t-1) == size(t) c'est qu'on a plus rien et là notifyAll
     */



    // Used to calculate the average and stdev
    private ArrayList<Double>
            d_att   = new ArrayList<>(), // Historic data for `att`
            d_er    = new ArrayList<>(); // Historic data for `er`

    private int
            emitter_pid = -1,
            verbose = 0, // Par default a zero, se change globalement dans le fichier de config
            id_originator = -1,
            att_th = -1; // Theoretic reach at start of bcast

    private static int id_diffusion = 0;

    // Nombre de diffusions
    private static int diffs = 0;

    private Boolean first_execute = true;

    public GossipController(String prefix) {
        diffs = Configuration.getInt(prefix + "." + PAR_NB_DIFFUSIONS);
        this.emitter_pid = Configuration.getPid(prefix + "." + PAR_EMITTER);
        this.position_pid = Configuration.getPid(prefix + "." + PAR_POSITION);

        id_diffusion = 0;

        received = new HashSet<>();
        broadcasts = new HashMap<>();
    }

    /**
     * Callback utilisé pour signaler à la classe qu'une diffusion s'est terminée.
     */
    public void notified_finished(int last_broadcast_id) {

        id_diffusion++;



        if (id_diffusion < diffs) {

            if (verbose != 0) {
                System.err.println("notified; next gossip is " + id_diffusion);
            }
            nouvelle_diffusion();
        }
    }

    private void nouvelle_diffusion() {
        int rand_id = CommonState.r.nextInt(Network.size());
        id_originator = rand_id;

        if (last_observable != null) {
            last_observable.deleteObserver(this::update);
        }

        if (verbose != 0) {
            System.err.println("\n\nNew broadcast, round " + id_diffusion);
        }

        // On choisit un noeud random dans le réseau
        Node n = Network.get(rand_id);

        int pid_gossip = Configuration.lookupPid("gossip");

        EmitterCounter emitter = (EmitterCounter) n.getProtocol(emitter_pid);
        emitter.clear_set();


        if(verbose !=0)
            System.err.println("Node " + n.getID() + " initiating gossip #" + id_diffusion + " pid " + pid_gossip);

        GossipProtocolImpl gos = (GossipProtocolImpl) n.getProtocol(pid_gossip);

//        EDSimulator.add(0, new Message(-1, n.getID(), "Gossip", new GossipData(id_diffusion, n.getID()), pid_gossip), n, pid_gossip);

        Observable obs = (Observable) n.getProtocol(pid_gossip);
        obs.addObserver(this::update);

        broadcasts.put(id_diffusion, new GossipData(id_diffusion, n.getID()));



        this.att_th = attTheo(n);



        gos.initiateGossip(n, id_diffusion, n.getID());
/*
        for (int i =0; i < Network.size(); i++) {
            Node node_i = Network.get(i);
            Observable g_i = (Observable) node_i.getProtocol(pid_gossip);
            g_i.addObserver(this::update);
        }

        */
    }


    private int attTheo(Node n) {
        Graph g = new MANETGraph(getPositions(), ((Emitter) n.getProtocol(emitter_pid)).getScope());
        final GraphAlgorithms ga = new GraphAlgorithms();
        Hashtable<Integer, Integer> connexes = (Hashtable) ga.weaklyConnectedClusters(g);
        int sum_theorique = 0; // Pcq source node
        for (Integer i: connexes.keySet()) {
            sum_theorique += connexes.get(i);
            System.err.println("Adding " + connexes.get(i));
        }
        return sum_theorique;
    }

    private Node get_node_by_id(long id) {
        Node n;
        for (int i=0; i < Network.size(); i++) {
            n = Network.get(i);
            if (n.getID() == id) return n;
        }
        return null;
    }

    @Override
    public boolean execute() {

        if (first_execute) {
            nouvelle_diffusion();
            first_execute = false;
            return false;
        }
        return false;
    }


    /**
     * Calcule l' atteignabilité: le pourcentage de noeuds atteignables ayant
     * reçu le message
     * nd recu / nombre d'sent
     * @return % of reachable nodes
     */
    private double att(int retransmits) {

        double att = (this.att_th*1.0/retransmits);
        d_att.add(att);
        if (verbose != 0)
            System.err.println("Reach : " + att);
        return att;
    }

    /**
     * l'economie de rediffusion qui est le pourcentage de noeuds qui ont recu
     * le message et qui n'ont pas rediffuser le message.
     * Cette metrique peut se calculer par la formule suivante (r - t)/r
     * ou `r` est le nombre de noeuds ayant recu le message
     * `r` et `t` le nombre de noeuds qui ont retransmis le messages
     * @return % of reachable nodes that did not rebroadcast
     */
    private double er() {
        // TODO: Implement er method
        return 0.0;
    }


    /**
     * The method is called when EmitterCounter flips
     * Observer callback function, activated when a bach of transits ends
     * @param o is an int[5] containing the following info:
     *  - [0] number_of_transits - usually 0
     *  - [1] number_of_received
     *  - [2] number_of_sent
     *  - [3] number_of_delivered
     *  - [4] identifier of gossip bcast
     *  - [5] number_of_retransmits
     *  - [6] number_of_no_transmits <- each time a node has recvd a message it has already
     * @param observable The calling instance
     * @param o int[] containing data from EmitterCounter
     */
    @Override
    public void update(Observable observable, Object o) {

        int[] results = (int[]) o;
        observable.deleteObserver(this::update);

        if (!received.contains(results[4])) {
            if (verbose != 0) {
                System.err.println("Controller notified of end, diff " + id_diffusion);
            }

            GossipData bcast_data = broadcasts.get(results[4]);

            received.add(results[4]);
            double reached = att(results[5]+1); // retransmits + root


            System.err.println(
                    "Controller: transits " + results[0] + " rcvd " + results[1] + " sent " + results[2]
                            + " delivered " + results[3] + " id " + results[4] + " retransmits " + results[5]
                            + " no_transmits " + results[6] + " att " + reached);

            notified_finished(results[4]);
        }
    }


    /**
     * Getters
     */

    public ArrayList<Double> getD_att() {
        return d_att;
    }
    public ArrayList<Double> getD_er() {
        return d_er;
    }

    private Map<Long, Position> getPositions(){
        Map<Long, Position> res = new HashMap<>();
        for(int i=0; i< Network.size();i++) {
            Node n = Network.get(i);
            PositionProtocol pos_proto_n = (PositionProtocol) n.getProtocol(position_pid);
            Position cur = pos_proto_n.getCurrentPosition();
            res.put(n.getID(),cur);
        }
        return res;
    }
}
