package manet;

import manet.algorithm.gossip.GossipProtocolImpl;
import manet.communication.Emitter;
import manet.communication.EmitterCounter;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class GossipController implements Control, Observer {

    private static final String PAR_NB_DIFFUSIONS = "nb_diffusions";
    private static final String PAR_EMITTER = "emitter";

    private static int last_size;
    private int round_number;
    //private static Set<Long> nodes_received;

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
            diffs = 0,  // Nombre de diffusions
            emitter_pid = -1,
            verbose = 1, // Par default a zero, se change globalement dans le fichier de config
            id_diffusion = 0, // First
            id_originator = -1;

    private Boolean first_execute = true;

    public GossipController(String prefix) {
        this.diffs = Configuration.getInt(prefix + "." + PAR_NB_DIFFUSIONS);
        this.emitter_pid = Configuration.getPid(prefix + "." + PAR_EMITTER);
        id_diffusion = 0;

        last_size = -1;
/*
        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            Observable finisher = (EmitterCounter) n.getProtocol(emitter_pid);
            finisher.addObserver(this::update);
        }
*/
    }

    /**
     * Callback utilisé pour signaler à la classe qu'une diffusion s'est terminée.
     */
    public void notified_finished() {
        id_diffusion++;
        last_size = 0;

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

        System.err.println("New broadcast");
        System.err.println("\n\n\n\n\n");
        // On choisit un noeud random dans le réseau
        Node n = Network.get(rand_id);

        last_size = 1;
        int pid_gossip = Configuration.lookupPid("gossip");

        EmitterCounter emitter = (EmitterCounter) n.getProtocol(emitter_pid);
        emitter.clear_set();


        if(verbose !=0)
            System.err.println("Node " + n.getID() + " initiating gossip with " + n.getID() + " gossip_id " + id_diffusion);

        GossipProtocolImpl gos = (GossipProtocolImpl) n.getProtocol(pid_gossip);

        if(verbose !=0)
            System.err.println("Gossip impl: " + pid_gossip + " " + gos);



        gos.initiateGossip(n, id_diffusion, n.getID());
        Observable finisher = (EmitterCounter) n.getProtocol(emitter_pid);
        finisher.addObserver(this::update);
    }

    @Override
    public boolean execute() {
        int delivering_emitter_pid = Configuration.getPid("emitter", -1);

        Emitter emitter;
/*
        if (id_originator != -1 && delivering_emitter_pid != -1) {
            Node n = Network.get(id_originator);

            emitter = (Emitter) n.getProtocol(delivering_emitter_pid);
            if (emitter instanceof EmitterCounter) {
                if (EmitterCounter.get_has_finished() == true) {
                    nouvelle_diffusion();
                    return false;
                }
            }
        }
        else
            System.err.println("Not supposed to be here");
*/
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
    private double att(int sent, int rcvd) {
        double att = (rcvd*1.0)/sent;
        d_att.add(att);
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

    public ArrayList<Double> getD_att() {
        return d_att;
    }

    public ArrayList<Double> getD_er() {
        return d_er;
    }

    @Override
    public void update(Observable observable, Object o) {
        // Fait des bails ici quand le boolean dans EmitterCouter passe a true
        // notified_finished
        //if (verbose != 0)
        // donc quand une diffusion est termine
        // on doit chopper l'Att et ER ici
        // Object is an int[4]
        //  - [0] number_of_transits - usually 0
        //  - [1] number_of_received
        //  - [2] number_of_sent
        //  - [3] number_of_delivered
        //  - [4] number_of_nodes

        int[] results = (int[]) o;
        System.err.println("Controller notified of end, diff " + id_diffusion);
        System.err.println("last size " + last_size + " new size " + results[4]);


/*        System.err.println("Got results " + results);
        for (int i : Results)
            System.err.print(i+" ");
 */
        if (last_size == results[4]) { // le broadcast est terminé

            System.err.println("Going to initiate new broadcast from update()");
            notified_finished();
        }
        else { // une vague s'est terminée mais pas le broadcast. on a de nouveaux voisins quoi
            last_size = results[4];
        }


    }
}
