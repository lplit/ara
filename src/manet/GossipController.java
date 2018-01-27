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

public class GossipController implements Control {

    private int diffs;  // Nombre de diffusions
    private static final String PAR_NB_DIFFUSIONS = "nb_diffusions";
    private int verbose = 1;

    private int id_diffusion = 0;
    private int id_originator = -1;

    private Boolean first_execute = true;

    public GossipController(String prefix) {
        this.diffs = Configuration.getInt(prefix + "." + PAR_NB_DIFFUSIONS);
        id_diffusion = 0;

    }

    /**
     * Callback utilisé pour signaler à la classe qu'une diffusion s'est terminée.
     */
    public void notified_finished() {
        id_diffusion++;

        if (verbose != 0) {
            System.err.println("notified; next gossip is " + id_diffusion);
        }

        if (id_diffusion < diffs) {
            nouvelle_diffusion();
        }
    }

    private void nouvelle_diffusion() {
        int rand_id = CommonState.r.nextInt(Network.size());
        id_originator = rand_id;

        // On choisit un noeud random dans le réseau
        Node n = Network.get(rand_id);


        int pid_gossip = Configuration.lookupPid("gossip");

                if(verbose !=0)

        {
            System.err.println("Node " + n.getID() + " initiating gossip with " + n.getID() + " gossip_id " + id_diffusion);
        }

        GossipProtocolImpl gos = (GossipProtocolImpl) n.getProtocol(pid_gossip);

                if(verbose !=0)

        {
            System.err.println("Gossip impl: " + pid_gossip + " " + gos);
        }
                gos.initiateGossip(n, id_diffusion, n.getID());

    }

    @Override
    public boolean execute() {
        int delivering_emitter_pid = Configuration.getPid("emitter", -1);

        Emitter emitter;

        if (first_execute) {
            nouvelle_diffusion();
            first_execute = false;
            return false;
        }

        if (id_originator != -1 && delivering_emitter_pid != -1) {
            Node n = Network.get(id_originator);

            emitter = (Emitter) n.getProtocol(delivering_emitter_pid);
            if (emitter instanceof EmitterCounter) {
                if (((EmitterCounter) emitter).get_has_finished() == true) {
                    nouvelle_diffusion();
                }


            }

        }
        else {
            System.err.println("Not supposed to be here");
        }

        return false;
    }



    // Used to calculate the average and stdev
    private ArrayList<Double>
            d_att   = new ArrayList<>(), // Historic data for `att`
            d_er    = new ArrayList<>(); // Historic data for `er`





    /**
     * Calcule l' atteignabilité: le pourcentage de noeuds atteignables ayant
     * reçu le message, soit ( #acks / network.size()).
     * @return % of reachable nodes
     */
    private double att() {

        return 0.0;
    }

    /**
     * l'economie de rediffusion qui est le pourcentage de noeuds qui ont recu
     * le message et qui n'ont pas rediffuser le message.
     * Cette metrique peut se calculer par la formule suivante (r - t)
     * ou `r` est le nombre de noeuds ayant recu le message
     * `r` et `t` le nombre de noeuds qui ont retransmis le messages
     * @return % of reachable nodes that did not rebroadcast
     */
    private double er() {

        return 0.0;
    }

    public ArrayList<Double> getD_att() {
        return d_att;
    }

    public ArrayList<Double> getD_er() {
        return d_er;
    }

}
