package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class FloodingEmitter extends EmitterCounter {

    public FloodingEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

        System.err.println("Sup, FloodingEmitter here");

    }

    @Override
    public void emit(Node host, Message msg) {

        // Diffusion à tous les noeuds dans le scope classique
        emitter_impl.emit(host, msg);
//        System.err.println("Sup, FloodingEmitter emitting");
        // "Pour tous les noeuds dans le scope", ..
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < getScope() && n.getID() != host.getID()) {
                number_of_transits++;

                if (this.verbose == 1)
                    System.err.println(this_pid + " incrementing, left: " + number_of_transits);
                // On rajoute l'évènement faisant décrémenter le compteur
                EDSimulator.add(getLatency(), null, n, this_pid);
            }
        }
    }

    @Override
    public Object clone() {
        FloodingEmitter res = null;

        res = (FloodingEmitter) super.clone();

        return res;
    }
}
