package manet.communication.algorithms;

import manet.Message;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/** Émitteur probabiliste aléatoire dans le scope. Exercice 5 question 5. */
public class ProbabilisticEmitter extends EmitterCounter {
    private static String PAR_PROBABILITY = "probability";
    private Boolean first_sent;
    private double probability = 0;


    public void setFirst_sent() {
        this.first_sent = !first_sent;
    }

    public ProbabilisticEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));
        first_sent = false;
        this.probability = Configuration.getDouble(prefix+"." + PAR_PROBABILITY);
    }

    @Override
    public void emit(Node host, Message msg) {
        number_of_sent = 0;
        nodes_received.clear();
        nodes_private.clear();
        double _prob = CommonState.r.nextDouble();
        if (_prob < this.probability || first_sent == false) {
            first_sent = true;
            for (Node n : get_neighbors_in_scope(host)) {
                number_of_sent++;
                nodes_received.add(n.getID());
                nodes_private.add(n);
                has_finished = false;
/*
                EDSimulator.add(
                        getLatency(),
                        new Message(
                                msg.getIdSrc(),
                                n.getID(),
                                msg.getTag(),
                                msg.getContent(),
                                msg.getPid()),
                        n,
                        msg.getPid());
                        */
                if (verbose != 0)
                    System.err.println("Node " + host.getID() +
                            " ProbabilisticEmitter emitted w/" + _prob + "<" + probability + " " + number_of_sent + " messages");

            }

        }

        for (Node n : nodes_private) {
            String new_tag = msg.getTag() + "//" + nodes_received;
            EDSimulator.add(
                    getLatency(),
                    new Message(
                            host.getID(),
                            n.getID(),
                            new_tag,
                            msg.getContent(),
                            msg.getPid()),
                    n,
                    msg.getPid());
            if (verbose != 0) {
                System.err.println("Node " + host.getID() + " ProbabilisticEmitter emitting with tag " + new_tag);
            }
        }


    }

    @Override
    public Object clone() {
        ProbabilisticEmitter res=null;
        res=(ProbabilisticEmitter) super.clone();
        return res;
    }

}
