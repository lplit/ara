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

    private double probability = 0;


    public ProbabilisticEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

        this.probability = Configuration.getDouble(prefix+"." + PAR_PROBABILITY);
    }

    @Override
    public void emit(Node host, Message msg) {
        number_of_sent = 0;
        double _prob = CommonState.r.nextDouble();
        if (_prob < this.probability) {
            for (Node n : get_neighbors_in_scope(host)) {
                number_of_sent++;
                has_finished = false;

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

            }
        }

        if (verbose != 0)
            System.err.println("Node " + host.getID() +
                    " ProbabilisticEmitter emitted w/" + _prob + "<" + probability + " " + number_of_sent + " messages");

    }

    @Override
    public Object clone() {
        ProbabilisticEmitter res=null;
        res=(ProbabilisticEmitter) super.clone();
        return res;
    }

}
