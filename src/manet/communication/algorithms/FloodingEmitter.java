package manet.communication.algorithms;

import manet.Message;
import manet.communication.Emitter;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import peersim.core.Node;


/** Émitteur de type flood. Exercice 2 question 4. */
public class FloodingEmitter extends EmitterCounter {

    public FloodingEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

    }

    @Override
    public void emit(Node host, Message msg) {

        // Diffusion à tous les noeuds dans le scope classique
        emitter_impl.emit(host, new Message(msg.getIdSrc(), msg.getIdDest(), msg.getTag(), msg, this_pid));
//        System.err.println("Node " + host.getID() + "Sup, FloodingEmitter emitting");
        // "Pour tous les noeuds dans le scope", ..

                // On rajoute l'évènement faisant décrémenter le compteur chez ceux qui reçoivent
/*                for (Node n : get_neighbors_in_scope(host)) {
//                    EDSimulator.add(getLatency(), msg.getIdSrc(), n, this_pid);
                    number_of_transits++;
                    if (verbose != 0)
                        System.err.println(host.getID() + " FloodingEmitter incrementing, left: " + number_of_transits);
                }*/
            }

    @Override
    public Object clone() {
        Emitter res = null;

        res = (FloodingEmitter) super.clone();

        return res;
    }

}
