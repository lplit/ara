package manet.communication;

import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.List;

public interface EmitterAtteignability extends Emitter {


    public Boolean has_received(int msg_id);

//    public Node getParentNode();

}
