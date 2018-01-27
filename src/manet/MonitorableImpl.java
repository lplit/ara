package manet;

import manet.algorithm.gossip.GossipProtocol;
import manet.algorithm.gossip.GossipProtocolImpl;
import peersim.config.Configuration;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

public class MonitorableImpl implements Monitorable {
    private int gossip_pid;

    public MonitorableImpl(String prefix) {
        this.gossip_pid= Configuration.getPid(prefix+"."+ GossipProtocol.PAR_GOSSIPPROTOCOL);
        System.err.println("MonitorableImpl up with gossip " + gossip_pid);
    }

    @Override
    public List<String> infos(Node host) {
        List<String> res = new ArrayList<String>();
        res.add("Node "+host.getID());

        GossipProtocolImpl gossip = (GossipProtocolImpl) host.getProtocol(gossip_pid);
        if (gossip_pid != 0) {
            res.add(gossip.show_map());
        }

        //System.err.println(res.toString());

        return res;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
