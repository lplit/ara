package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.HashMap;
import java.util.Map;

public class DistanceEmitter implements Emitter, EmitterAtteignability, EDProtocol {

    private int this_pid;
    private int position_protocol;

    Map<Integer, Integer> received_messages;

    public Boolean has_received(int msg_id) {
        return (msg_id < next_waiting);
    }

    private int next_waiting = 0;

    private EmitterImpl impl;

    public DistanceEmitter(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);

        this.position_protocol = Configuration.getPid(prefix +"."+ PAR_POSITIONPROTOCOL);

        received_messages = new HashMap<Integer, Integer>();

        System.out.println("distanceEmitter pid " + this_pid + " position pid " + position_protocol);

        impl = new EmitterImpl(
                Configuration.getInt(prefix + "." + PAR_LATENCY),
                Configuration.getInt(prefix + "." + PAR_SCOPE),
                Configuration.getPid(prefix +"."+ PAR_POSITIONPROTOCOL));
        //System.out.println("Created impl " + impl.getLatency() + " " + impl.getScope());
    }

    @Override
    public void emit(Node host, Message msg) {
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            double _prob = CommonState.r.nextDouble();
            if (dist < getScope()&& n.getID() != host.getID() && _prob < dist / getScope()) {
                if (msg.getIdDest() == -1) {
                    EDSimulator.add(getLatency(), new Message(msg.getIdSrc(), n.getID(), msg.getTag(), msg.getContent(), msg.getPid()), n, msg.getPid());
                }
                //
                //                EDSimulator.add(0, msg, n, );
            }
        }
    }

    @Override
    public int getLatency() {
        return impl.getLatency();
    }

    @Override
    public int getScope() {
        return impl.getScope();
    }

    @Override
    public Object clone() {
        DistanceEmitter res=null;
        try {
            res=(DistanceEmitter) super.clone();
        } catch (CloneNotSupportedException e) {}
        return res;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        Message m;
        Integer message_id;

        if (!(event instanceof  Message))
            return;

        m = (Message) event;
        message_id = Integer.parseInt (m.getTag());

        if (pid == this_pid) {
            if (received_messages.get(message_id) == null) {
                emit(node, m);
                received_messages.put(message_id, 0);
            }
        }
    }
}
