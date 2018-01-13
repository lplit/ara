package manet.communication;

import manet.Message;
import manet.positioning.Position;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDSimulator;

public class EmitterImpl implements Emitter {

    private int latency;
    private int scope;
    private int this_pid;
    private int position_protocol;

    private static final String PAR_LATENCY = "latency";
    private static final String PAR_SCOPE = "scope";
    private static final String PAR_POSITIONPROTOCOL = "positionprotocol";

    public EmitterImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        //System.out.println(prefix + "." + PAR_POSITIONPROTOCOL);
        this.position_protocol=Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
        this.scope = Configuration.getInt(prefix + "." + PAR_SCOPE);
        //System.out.println("WOAH");
    }

    @Override
    public void emit(Node host, Message msg) {
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);

        for (int i=0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist =prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < scope && n.getID() != host.getID()) {
                EDSimulator.add(latency, new Message(msg.getIdSrc(), n.getID(), msg.getTag(), msg.getContent(), msg.getPid()), n, msg.getPid());
                //                System.out.println("yeah lele woah");
                //                EDSimulator.add(0, msg, n, );
            }
        }

    }

    @Override
    public int getLatency() {
        return latency;
    }

    @Override
    public int getScope() {
        return scope;
    }

    @Override
    public Object clone(){
        EmitterImpl res=null;
        try {
            res=(EmitterImpl)super.clone();
        } catch (CloneNotSupportedException e) {}
        return res;
    }
}