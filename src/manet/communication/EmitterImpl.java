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
    protected int this_pid;
    private int position_protocol;

    public static int messa;


    /**
     * Standard peersim constructor
     * @param prefix
     */
    public EmitterImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        //System.out.println(prefix + "." + PAR_POSITIONPROTOCOL);
        this.position_protocol=Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
        this.scope = Configuration.getInt(prefix + "." + PAR_SCOPE);
    }

    /**
     * Copy constructor
     * @param latency latency
     * @param scope scope
     * @param position_protocol position protocol
     */
    public EmitterImpl(int latency, int scope, int position_protocol) {
        this.latency = latency;
        this.scope = scope;
        this.position_protocol = position_protocol;
    }


    /**
     * This method sends the messages
     * @param host The current host
     * @param msg The message it's sending
     */
    @Override
    public void emit(Node host, Message msg) {
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);
        for (int i=0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist = prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < scope && n.getID() != host.getID()) {
                if (msg.getIdDest() == -1)
                    EDSimulator.add(latency,
                            new Message(msg.getIdSrc(), n.getID(), msg.getTag(), msg.getContent(), msg.getPid()),
                            n, msg.getPid());
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
