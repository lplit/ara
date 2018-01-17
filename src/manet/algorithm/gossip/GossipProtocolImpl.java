package manet.algorithm.gossip;

import peersim.config.Configuration;
import peersim.core.Node;

import java.util.ArrayList;

public class GossipProtocolImpl implements GossipProtocol {

    /**
     * lancer les N diusions séquentielles
     * - calculer pour chaque diusion Att et ER .
     * l' atteignabilité noté Att : le pourcentage de n÷uds atteignables ayant reçu le
     message -> (nb_voisins / network.size()).
     *
     * - calculer la moyenne des Att et des ER (notés respectivement Att N et ER N ) avec
     * leur écart-type, une fois que les N diusions sont terminées. Ce sont ces deux
     * valeurs qui permettront d'évaluer les performances d'un algorithme.
     */
    private final int this_pid;
    private int diffs;  // Nombre de diffusions

    // Used to calculate the average and stdev
    private ArrayList<Double>
        d_att   = new ArrayList<Double>(), // Historic data for `att`
        d_er    = new ArrayList<Double>(); // Historic data for `er`

    private static final String PAR_NB_DIFFS = "nb_diffs";

    public GossipProtocolImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);
        this.diffs= Configuration.getInt(prefix + "." + PAR_NB_DIFFS);
    }

    /**
     * Noeud source choisi aléatoirement
     * @param host
     * @param id
     * @param id_initiator
     */
    @Override
    public void initiateGossip(Node host, int id, long id_initiator) {

        for (int i = 0 ; i < this.diffs ; i++) {
// aight
        }

    }

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

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
        } catch (CloneNotSupportedException e) {}
        return gpi;
    }


    public ArrayList<Double> getD_att() {
        return d_att;
    }

    public ArrayList<Double> getD_er() {
        return d_er;
    }
}
