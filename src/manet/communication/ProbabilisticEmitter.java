package manet.communication;

import manet.MANETGraph;
import manet.Message;
import manet.detection.NeighborProtocolImpl;
import manet.positioning.Position;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.graph.Graph;
import peersim.graph.GraphAlgorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProbabilisticEmitter extends EmitterCounter {
    // clé = msgid, valeur == initiateur du message
    public Map<Integer, Integer> messages_received;


    private Map<Long, Position> getPositions(){

        Map<Long, Position> res = new HashMap<>();
        for(int i = 0; i< Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol pos_proto_n = (PositionProtocol) n.getProtocol(position_protocol);
            Position cur = pos_proto_n.getCurrentPosition();
            res.put(n.getID(),cur);
        }
        return res;
    }

    public Boolean has_received(int msg_id) {
        Map<Long, Position> positions = getPositions();
        Graph g = new MANETGraph(positions, super.getScope());
        final GraphAlgorithms ga = new GraphAlgorithms();
        Map<Integer,Integer> m =ga.weaklyConnectedClusters(g);

        NeighborProtocolImpl impl;
        Node originateur;

        if (messages_received.containsKey(msg_id)) {
            originateur = Network.get(messages_received.get(msg_id));
//            impl = originateur.getProtocol();
        }

        return false;
    }

    private int next_waiting = 0;

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
        }
    }

    @Override
    public Object clone() {
        ProbabilisticEmitter res=null;
        res=(ProbabilisticEmitter) super.clone();
        return res;
    }

}
