package manet.detection;

import manet.Message;
import manet.communication.Emitter;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeighborProtocolImpl2 implements NeighborProtocol, EDProtocol {

    private int
            this_pid = -1,
            period = -1,
            timer_delay = -1,
            listener_pid = -1,
            verbose = 0;

    private static final String PAR_PERIOD = "period";
    private static final String PAR_TIMERDELAY = "timer_delay";
    private static final String PAR_LISTENER_PID = "listenerpid";

    private static final String tag_probe = "Probe";
    private static final String tag_heartbeat = "Heartbeat";
    private static final String tag_timer = "Timer";


    private List<Long> neighbor_list;
    private Map<Long, Long> neighbor_timers; // Map<NodeID, Timestamp>


    /**
     * Constructor peersim
     * @param prefix prefix
     */
    public NeighborProtocolImpl2(String prefix) {
        neighbor_list = new ArrayList<>();
        neighbor_timers = new HashMap<Long, Long>();

        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);

        if (verbose != 0)
            System.err.println("NeighborImpl2 pid " + this_pid);

        this.period = Configuration.getInt(prefix+"."+PAR_PERIOD);
        this.timer_delay = Configuration.getInt(prefix + "." + PAR_TIMERDELAY);
        this.listener_pid = Configuration.getPid(prefix + "." + PAR_LISTENER_PID,-1);
        this.verbose = Configuration.getInt(prefix+".verbose");


    }

    /**
     * Returns the list of neighbours at current code
     * @return Neighbor list (list of Nodes)
     */
    @Override
    public List<Long> getNeighbors() {
        return neighbor_list;
    }

    @Override
    public Object clone() {
        NeighborProtocolImpl2 res = null;
        try {
            res = (NeighborProtocolImpl2) super.clone();
            res.neighbor_list= new ArrayList<>();
            res.neighbor_timers = new HashMap<Long, Long>();
            res.period = this.period;
        } catch (CloneNotSupportedException e) {
            System.err.println("Neighbor clone error");
        }
        return res;
    }


    /**
     * The proper processing method, works with "Heartbeat" and "Timer" message tags.
     * "Heartbeat" messages are self-bootstrapping
     * "Timer" activates the timers for others
     * @param node Current node
     * @param pid Someone's PID
     * @param event The arriving event (Message)
     */
    @Override
    public void processEvent(Node node, int pid, Object event) {
        int emitter_pid = Configuration.lookupPid("emitter");
        Emitter impl = (Emitter) node.getProtocol(emitter_pid);
        Message msg = (Message) event;

        if (this.verbose != 0) {
            System.err.println("NeighborImpl2 node " + node.getID() + " received event " + event.toString());
            if (CommonState.getIntTime() % 100000 * 60 * 60 == 0)
                System.err.println("Time " + CommonState.getIntTime());
        }


        if (event instanceof Message) {
            switch (msg.getTag()) {
                case tag_heartbeat: // self-message toutes les périodes
                    EDSimulator.add(this.period, new Message(node.getID(), -1, tag_heartbeat, tag_heartbeat, this_pid), node, this_pid);
                    // Envoi d'un probe dans le scope pour les voisins, avec un timestamp
                    impl.emit(node, new Message(node.getID(), -1, tag_probe, CommonState.getTime(), this_pid));
                    if (verbose != 0)
                        System.err.println("NeighborImpl2 node " + node.getID() + " emitting w/pid " + this_pid);
                    break;

                case tag_probe: // Réception d'un probe de quelqu'un d'autre
                    // rajout du voisin s'il n'est pas dans la liste des voisins
                    if (!neighbor_list.contains(msg.getIdSrc()))
                        neighbor_list.add(msg.getIdSrc());

                    // rajout du voisin dans la liste des timers dans tous les cas
                    neighbor_timers.put(msg.getIdSrc(), (Long) msg.getContent());

                    EDSimulator.add(
                            this.timer_delay,
                            new Message(msg.getIdSrc(), node.getID(), tag_timer, msg.getContent(), this_pid),
                            node,
                            this_pid);

                    if (verbose!=0)
                        System.err.println("Node " + node.getID() + ": received probe from " + msg.getIdSrc() + " with " + msg.getContent());

                    break;

                case tag_timer:
                    if (neighbor_list.contains(msg.getIdSrc()))
                        if ( neighbor_timers.get(msg.getIdSrc()) == msg.getContent())
                            neighbor_list.remove(msg.getIdSrc());

                    if (verbose==1)
                        System.err.println("Node " + node.getID() + " " + neighbor_list)         ;

                    break;

                default:
                    System.out.println("IN DEFAULT");
                    break;
            }


        }

        return;
    }
}
