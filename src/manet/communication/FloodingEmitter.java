package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;


/** Émitteur de type flood. Exercice 2 question 4. */
public class FloodingEmitter extends EmitterCounter {

    public FloodingEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

    }

    @Override
    public void emit(Node host, Message msg) {

        // Diffusion à tous les noeuds dans le scope classique
        emitter_impl.emit(host, msg);
//        System.err.println("Node " + host.getID() + "Sup, FloodingEmitter emitting");
        // "Pour tous les noeuds dans le scope", ..

                // On rajoute l'évènement faisant décrémenter le compteur
                for (Node n : get_neighbors_in_scope(host)) {
                    EDSimulator.add(getLatency(), null, n, this_pid);
                    number_of_transits++;
                    if (verbose != 0)
                        System.err.println(host.getID() + " FloodingEmitter incrementing, left: " + number_of_transits);
                }
            }

    @Override
    public Object clone() {
        Emitter res = null;

        res = (FloodingEmitter) super.clone();

        return res;
    }

}
