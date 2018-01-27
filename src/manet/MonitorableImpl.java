package manet;

import manet.algorithm.gossip.GossipProtocol;
import manet.algorithm.gossip.GossipProtocolImpl;
import manet.detection.NeighborProtocol;
import manet.detection.NeighborProtocolImpl;
import peersim.config.Configuration;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

public class MonitorableImpl implements Monitorable {
    private int gossip_pid;
    private int neighbor_pid;

    private static final String PAR_NEIGHBORPID = "neighborprotocol";


    public MonitorableImpl(String prefix) {
        this.gossip_pid= Configuration.getPid(prefix+"."+ GossipProtocol.PAR_GOSSIPPROTOCOL);
        this.neighbor_pid = Configuration.getPid(prefix + "." + "neighbor", -1);
        System.err.println("MonitorableImpl up with gossip " + gossip_pid + " neighbor " + neighbor_pid);
    }

    @Override
    public List<String> infos(Node host) {
        List<String> res = new ArrayList<String>();
        res.add("Node "+host.getID());

        if (neighbor_pid != -1) {
            NeighborProtocol impl = (NeighborProtocol) host.getProtocol(neighbor_pid);
            res.add("Neighbors " + impl.getNeighbors());
        }

        GossipProtocolImpl gossip = (GossipProtocolImpl) host.getProtocol(gossip_pid);
        if (gossip_pid != 0) {
            res.add("Gossip ");
            res.add(gossip.show_list());
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
