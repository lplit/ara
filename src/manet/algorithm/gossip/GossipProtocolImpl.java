package manet.algorithm.gossip;

import peersim.config.Configuration;
import peersim.core.Node;

public class GossipProtocolImpl implements GossipProtocol {

    /**
     * lancer les N diusions séquentielles
     * - calculer pour chaque diusion Att et ER .
     * - calculer la moyenne des Att et des ER (notés respectivement Att N et ER N ) avec
     * leur écart-type, une fois que les N diusions sont terminées. Ce sont ces deux
     * valeurs qui permettront d'évaluer les performances d'un algorithme.
     */
    private int this_pid;
    private int diffs;

    private static final String PAR_NB_DIFFS = "nb_diffs";

    public GossipProtocolImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);
        this.diffs= Configuration.getInt(prefix + "." + PAR_NB_DIFFS);
    }

    @Override
    public void initiateGossip(Node host, int id, long id_initiator) {

    }

    @Override
    public Object clone() {
        GossipProtocolImpl gpi = null;
        try {
            gpi = (GossipProtocolImpl) super.clone();
        } catch (CloneNotSupportedException e) {}
        return gpi;
    }
}
