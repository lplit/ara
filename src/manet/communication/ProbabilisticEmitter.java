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

public class ProbabilisticEmitter extends EmitterImpl implements  EmitterAtteignability, EDProtocol {
    private static final String PAR_POSITIONPID = "positionprotocol";

    private int position_pid;

    // clé = msgid, valeur == initiateur du message
    public Map<Integer, Integer> messages_received;


    private Map<Long, Position> getPositions(){

        Map<Long, Position> res = new HashMap<>();
        for(int i = 0; i< Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol pos_proto_n = (PositionProtocol) n.getProtocol(position_pid);
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
        super(prefix);
        String tmp[]=prefix.split("\\.");

        position_pid=Configuration.getPid(prefix+"."+PAR_POSITIONPID);
        probability = Configuration.getDouble(prefix+"." + PAR_PROBABILITY);

    }

    @Override
    public void emit(Node host, Message msg) {
        double _prob = CommonState.r.nextDouble();
        if (_prob < this.probability) {
            super.emit(host, msg);
        }
    }

    @Override
    public int getLatency() {
        return super.getLatency();
    }

    @Override
    public int getScope() {
        return super.getScope();
    }

    @Override
    public Object clone() {
        ProbabilisticEmitter res=null;
        res=(ProbabilisticEmitter) super.clone();
        return res;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        Message m;

        if (pid != super.this_pid)
            return;

        m = (Message) event;

        Integer msg_id = Integer.parseInt((String) m.getTag());
        if (messages_received.get(msg_id) != null) {
            // do nothing
        }
        else {
            messages_received.put(msg_id, Integer.parseInt((String) m.getContent()));
            this.emit(node, new Message(node.getID(), 0, m.getTag(), m.getContent(), this_pid));
        }
    }

}
