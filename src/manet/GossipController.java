package manet;

import manet.algorithm.gossip.GossipProtocolImpl;
import org.nfunk.jep.function.Str;
import peersim.config.Configuration;
import peersim.core.Control;

import java.util.ArrayList;

public class GossipController implements Control {

    private int diffs;  // Nombre de diffusions
    private static final String PAR_NB_DIFFS = "nb_diffs";

    public GossipController(String prefix) {
        this.diffs= Configuration.getInt(prefix + "." + PAR_NB_DIFFS);


    }


    @Override
    public boolean execute() {
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
