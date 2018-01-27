package manet.communication.algorithms;

import manet.Message;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import manet.positioning.PositionProtocol;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;

/** Émetteur prenant en compte la distance du voisin de l'émetteur, dans le scope. (Exercice 2 question 7)

 */
public class DistanceEmitter extends EmitterCounter {

    public DistanceEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));

    }

    @Override
    public void emit(Node host, Message msg) {
        ArrayList<Node> list = new ArrayList<>();
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);

        for (int i = 0; i < Network.size(); i++) {
            double random = CommonState.r.nextDouble();


            /* Là on tire une fois par noeud voisin puis on check si on veut faire de l'émission avec */
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < getScope() && n.getID() != host.getID()) {
                if (random < (dist / getScope())) {
                    EDSimulator.add(
                            getLatency(),
                            new Message(msg.getIdSrc(),
                                    n.getID(),
                                    msg.getTag(),
                                    msg.getContent(),
                                    msg.getPid()),
                            n,
                            this_pid);

                }
            }
        }
    }
}
