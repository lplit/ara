package manet;

import manet.positioning.PositionProtocolImpl;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Node;
import peersim.core.Network;
import peersim.edsim.EDSimulator;

public class Initialisation implements Control {


    public Initialisation(String s) {

    }


    @Override
    public boolean execute() {
        for(int i = 0 ; i < Network.size() ; i++) {
            Node n = Network.get(i);
            int position = Configuration.lookupPid("position");
            PositionProtocolImpl p = (PositionProtocolImpl) n.getProtocol(position);
            p.initialiseCurrentPosition(n);
            EDSimulator.add(p.getTimePause(), PositionProtocolImpl.loop_event, n, position);
        }
        return false;
    }
}
