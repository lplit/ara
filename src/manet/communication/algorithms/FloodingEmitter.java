package manet.communication.algorithms;

import manet.Message;
import manet.communication.Emitter;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import peersim.core.Node;
import peersim.edsim.EDSimulator;


/** Émitteur de type flood. Exercice 2 question 4. */
public class FloodingEmitter extends EmitterCounter {

    public FloodingEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

    }

    @Override
    public void emit(Node host, Message msg) {
        number_of_sent = 0;
        nodes_received.clear();

        for (Node n : get_neighbors_in_scope(host)) {
            number_of_sent++;
            nodes_received.add(n.getID());

            has_finished = false;

            EDSimulator.add(
                    getLatency(),
                    new Message(
                        host.getID(),
                        n.getID(),
                        msg.getTag(),
                        msg.getContent(),
                        msg.getPid()),
                     n,
                    msg.getPid());

            }

            if (verbose != 0)
                System.err.println("Node " + host.getID() + ": FloodingEmitter sent " + number_of_sent + " messages");
        }

    @Override
    public Object clone() {
        Emitter res = null;

        res = (FloodingEmitter) super.clone();

        return res;
    }

}
