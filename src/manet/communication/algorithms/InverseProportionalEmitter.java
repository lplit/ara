package manet.communication.algorithms;

import manet.Message;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/** Émitteur probabiliste aléatoire dans le scope. Exercice 5 question 5. */
public class InverseProportionalEmitter extends EmitterCounter {
    private static String PAR_VALUEOFK = "val_k";

    private double k = 0;


    public InverseProportionalEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

        this.k = Configuration.getDouble(prefix+"." + PAR_VALUEOFK);
    }

    @Override
    public void emit(Node host, Message msg) {
        number_of_sent = 0;
        nodes_received.clear();
        nodes_private.clear();

        double probability = k / get_neighbors_in_scope(host).size();

        double _prob = CommonState.r.nextDouble();
        if (_prob < probability) {
            for (Node n : get_neighbors_in_scope(host)) {
                number_of_sent++;
                has_finished = false;
                nodes_private.add(n);
            }

            for (Node n: nodes_private) {
                String new_tag = msg.getTag() + "//" + nodes_private;
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
                if (verbose != 0)
                    System.err.println("Node " + host.getID() +
                            " InverseProportionalEmitter emitted w/" + _prob + "<" + probability + " sent " + number_of_sent + " messages");


            }

        }
        else {
            if (verbose != 0) {
                System.err.println("Node " + host.getID() + " did NOT emit " + _prob + " !< " + probability);
            }
        }


    }

    @Override
    public Object clone() {
        InverseProportionalEmitter res=null;
        res=(InverseProportionalEmitter) super.clone();
        return res;
    }

}
