package manet.communication;

import manet.Message;
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

        probability = Configuration.getDouble(prefix+"." + PAR_PROBABILITY);
    }

    @Override
    public void emit(Node host, Message msg) {
        double _prob = CommonState.r.nextDouble();
        if (_prob < this.probability) {
            emitter_impl.emit(host, msg);

            for (Node n : get_neighbors_in_scope(host)) {
                EDSimulator.add(getLatency(), msg.getIdSrc(), n, this_pid);
                number_of_transits++;
            }

            if (verbose != 0)
                System.err.println("Node " + host.getID() + " ProbabilisticEmitter emitting " + _prob + " < " + probability);
        }
    }

    @Override
    public Object clone() {
        ProbabilisticEmitter res=null;
        res=(ProbabilisticEmitter) super.clone();
        return res;
    }

}
