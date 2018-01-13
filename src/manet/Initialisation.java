package manet;

import manet.detection.NeighborProtocolImpl;
import manet.positioning.PositionProtocolImpl;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Initialisation implements Control {
    public Initialisation(String prefix) {}

    @Override
    public boolean execute() {
        for (int i=0; i < Network.size(); i++) {
            Node n = Network.get(i);
            int pid = Configuration.lookupPid("position");
            PositionProtocolImpl pos = (PositionProtocolImpl) n.getProtocol(pid);
            pos.initialiseCurrentPosition(n); // chelou un peu mais bon
            EDSimulator.add(0, PositionProtocolImpl.loop_event, n, pid);
            int pid2 = Configuration.lookupPid("neighbor");
            EDSimulator.add(0, new Message(n.getID(), 0, "Heartbeat", "Heartbeat", pid2), n, pid2);
            System.err.println("new tick");
        }
        return false;
    }
}
