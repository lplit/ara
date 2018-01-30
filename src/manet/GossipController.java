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
    private Boolean first_execute = true;
    private Observable last_observable = null;
    private static final String PAR_NB_DIFFUSIONS = "nb_diffusions";
    private static final String PAR_POSITION = "position";
    private static final String PAR_EMITTER = "emitter";

    private static Set<Integer> received;
    private static Map<Integer, GossipData> broadcasts;

    // Used to calculate the average and stdev
    private ArrayList<Double>
            d_att           = new ArrayList<>(), // Historic data for `att`
            d_er            = new ArrayList<>(); // Historic data for `er`

    private double
            avg_att         = 0.0,  // Last average over d_att
            stdev_att       = 0.0,  // Standard deviation over d_att
            avg_er          = 0.0,  // Last average over d_er
            stdev_er        = 0.0;  // Standard deviation over d_er

    private int
            emitter_pid     = -1,
            verbose         = 0,    // Par default a zero, se change globalement dans le fichier de config
            id_originator   = -1,   // Originator's PID
            att_th          = -1;   // Theoretic reach at start of bcast

    private static int
            diffs           = 0,    // Nombre de diffusions (config)
            id_diffusion    = 0;    // Current diffusion number








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
        // Still some bcasts to run
        if (id_diffusion < diffs) {

            if (verbose != 0)
                System.err.println("notified; next gossip is " + id_diffusion);
            nouvelle_diffusion();
            return;
        }

        if (id_diffusion >= diffs ) { // Last
            // Calc avg, stdev pour att et er
            double
                    avg = 0.0,
                    tmp = 0.0,
                    stdev = 0.0;

            // Avg att
            for (Double d : d_att)
                tmp += d;
            this.avg_att = tmp / d_att.size();
            tmp=0.;

            // Stdev att
            for (Double d: d_att)
                tmp += (d-this.avg_att)*(d-this.avg_att);
            tmp = tmp / d_att.size();
            this.stdev_att = Math.sqrt(tmp);
            tmp = 0.;

            // Avg er
            for (Double d : d_er)
                tmp += d;
            this.avg_er = tmp / d_er.size();
            tmp=0.;

            // Stdev er
            for (Double d: d_er)
                tmp += (d-this.avg_er)*(d-this.avg_er);
            tmp = tmp / d_er.size();
            this.stdev_er = Math.sqrt(tmp);
            tmp = 0.;

        }

        System.out.format("%.2f;%.2f;%.2f;%.2f\n", avg_att, stdev_att, avg_er, stdev_er);


    }


    /**
     * Function responsible for launching new bcast
     */
    private void nouvelle_diffusion() {
        int rand_id = CommonState.r.nextInt(Network.size());
        id_originator = rand_id;

        if (last_observable != null)
            last_observable.deleteObserver(this::update);


        // On choisit un noeud random dans le réseau
        Node n = Network.get(rand_id);
        int pid_gossip = Configuration.lookupPid("gossip");
        EmitterCounter emitter = (EmitterCounter) n.getProtocol(emitter_pid);
        emitter.clear_set();
        GossipProtocolImpl gos = (GossipProtocolImpl) n.getProtocol(pid_gossip);
        Observable obs = (Observable) n.getProtocol(pid_gossip);
        obs.addObserver(this::update);
        broadcasts.put(id_diffusion, new GossipData(id_diffusion, n.getID()));
        this.att_th = attTheo(n); // New broadcast, overwrite the value

        if(verbose !=0)
            System.err.println("Node " + n.getID() + " initiating gossip #" + id_diffusion + " pid " + pid_gossip);

        gos.initiateGossip(n, id_diffusion, n.getID());
    }


    /**
     * Calcualtes the theoretical reach of node @param n.
     * Meaning that it calculates the weakly connected clusters of @param n
     * and sums their size, plus adds one for the initiator node
     * @param n
     * @return
     */
    private int attTheo(Node n) {
        Graph g = new MANETGraph(getPositions(), ((Emitter) n.getProtocol(emitter_pid)).getScope());
        final GraphAlgorithms ga = new GraphAlgorithms();
        Hashtable<Integer, Integer> connexes = (Hashtable) ga.weaklyConnectedClusters(g);
        int sum_theorique = 1; // Pcq source node
        for (Integer i: connexes.keySet()) {
            sum_theorique += connexes.get(i);
            if (verbose != 0)
                System.err.println("Adding " + connexes.get(i));
        }
        if (verbose == 0)
            System.err.println("Theoretic reach at start: " + sum_theorique);
        return sum_theorique;
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
    private double er(int received, int retransmitted) {
        double d_r = received;
        double d_t = retransmitted;

        double ret = ((d_r - d_t)/d_r);
        d_er.add(ret);

        return ret;
    }


    /**
     * The method is called when EmitterCounter flips
     * Observer callback function, activated when a bach of transits ends
     * @param o is an int[5] containing the following info:
     *  - [0] number_of_transits - usually 0
     *  - [1] Number of nodes having received the message.
     *  - [2] Total number of messages sent
     *  - [3] Total number of messages delivered
     *  - [4] Broadcast id
     *  - [5] Number of nodes that retransmitted the message
     *  - [6] Total number of messages not-retransmited (i.e. received redundantly)
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
            double reached = att(results[5]); // retransmits + root
            double eco_redif = er(results[1], results[5]);

            if (verbose == 0) {
                System.err.println(
                        "Controller: transits " + results[0] + " nodes_rcvd " + results[1] + " messages_sent " + results[2]
                                + " delivered_messages " + results[3] + " gossip_id " + results[4] + " nodes_retransmitted " + results[5]
                                + " nodes_no_transmitted " + results[6] + " attainability " + reached + " economy " + eco_redif);
            }
//            System.out.format("%f;%f\n", reached, eco_redif);

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

    /**
     * Since Network.get(i) does not guarantee the same not to be returned at two different times,
     * this function does exactly that but by working with the unique node IDs.
     * @param id
     * @return
     */
    private Node get_node_by_id(long id) {
        Node n;
        for (int i=0; i < Network.size(); i++) {
            n = Network.get(i);
            if (n.getID() == id) return n;
        }
        return null;
    }

}
