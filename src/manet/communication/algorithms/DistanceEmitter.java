package manet.communication.algorithms;

import manet.Message;
import manet.communication.EmitterCounter;
import manet.communication.EmitterImpl;
import manet.positioning.PositionProtocol;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;

/** Émetteur prenant en compte la distance du voisin de l'émetteur, dans le scope. (Exercice 2 question 7)

 */
public class DistanceEmitter extends EmitterCounter {
    private Boolean first_sent;

    public DistanceEmitter(String prefix) {
        super(prefix, new EmitterImpl(prefix));
        first_sent = false;

    }


    public void setFirst_sent() {
        this.first_sent = !first_sent;
    }


    @Override
    public void emit(Node host, Message msg) {
        number_of_sent = 0;
        nodes_received.clear();
        nodes_private.clear();
        ArrayList<Node> list = new ArrayList<>();
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        double random = CommonState.r.nextDouble();

        /* Là on tire une fois pour l'émetteur puis on check si on veut faire de l'émission avec */
        PositionProtocol prot2 = (PositionProtocol) get_node_by_id(msg.getIdSrc()).getProtocol(position_protocol);
        double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());

        if (host.getID() == msg.getIdSrc())
            dist = getScope();

        if (random < (dist / getScope())) {
            if (verbose != 0)
                System.err.println("Node " + host.getID() + " DistanceEmitter rand " + random + "<" + dist / getScope());

            for (Node n : get_neighbors_in_scope(host)) {

                if (dist <= getScope() && n.getID() != host.getID()) {
                    number_of_sent++;
                    nodes_received.add(n.getID());
                    nodes_private.add(n);
                    has_finished = false;
                } else if (verbose != 0) {
                    System.err.println("Node " + host.getID() + " DistanceEmitter NOT emitting");
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
                    System.err.println("Node " + host.getID() + " DistanceEmitter emitting with tag " + new_tag);
                }
            }
        }
    }
}
