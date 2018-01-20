package manet.communication;

import manet.Message;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

public class ProbabilisticEmitter implements Emitter {

    private static String PAR_PROBABILITY = "probability";
    private EmitterImpl impl;
    private double probability = 0;
    private long this_pid;

    public ProbabilisticEmitter(String prefix) {
        String tmp[]=prefix.split("\\.");

        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        probability = Configuration.getDouble(PAR_PROBABILITY);

        impl = new EmitterImpl(
                Configuration.getPid(prefix+"."+ PAR_POSITIONPROTOCOL),
                Configuration.getInt(prefix + "." + PAR_LATENCY),
                Configuration.getInt(prefix + "." + PAR_SCOPE));
    }

    @Override
    public void emit(Node host, Message msg) {
        double _prob = CommonState.r.nextDouble();
        if (_prob < this.probability) {
            impl.emit(host, msg);
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
        ProbabilisticEmitter res=null;
        try {
            res=(ProbabilisticEmitter) super.clone();
        } catch (CloneNotSupportedException e) {}
        return res;
    }
}
