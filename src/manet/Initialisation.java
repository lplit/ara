package manet;

import manet.algorithm.gossip.GossipProtocol;
import manet.algorithm.gossip.GossipProtocolImpl;
import manet.detection.NeighborProtocolImpl;
import manet.positioning.PositionProtocolImpl;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import sun.nio.ch.Net;

public class Initialisation implements Control {
    public Initialisation(String prefix) {

    }

    @Override
    public boolean execute() {
        Node n;
        for (int i=0; i < Network.size(); i++) {
            n = Network.get(i);
            int pid = Configuration.lookupPid("position");
            PositionProtocolImpl pos = (PositionProtocolImpl) n.getProtocol(pid);
            pos.initialiseCurrentPosition(n); // chelou un peu mais bon
            EDSimulator.add(0, PositionProtocolImpl.loop_event, n, pid);
            int pid2 = Configuration.lookupPid("neighbor");
            EDSimulator.add(0, new Message(n.getID(), -1, "Heartbeat", "Heartbeat", pid2), n, pid2);

//            System.err.println("new tick");
        }

        n =  Network.get(CommonState.r.nextInt(Network.size()));


        int pid_gossip = Configuration.lookupPid("gossip");
        System.err.println("Node " + n.getID() + " initiating gossip with 0 0");
        GossipProtocolImpl gos = (GossipProtocolImpl) n.getProtocol(pid_gossip);
        System.err.println("Gossip impl: " + pid_gossip  + " " + gos);
        gos.initiateGossip(n, 0, n.getID());
        return false;
    }
}
