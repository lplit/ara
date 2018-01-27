package manet.detection;

import manet.Message;
        import manet.communication.Emitter;
        import manet.communication.EmitterImpl;
        import manet.detection.NeighborProtocol;
        import peersim.config.Configuration;
        import peersim.core.CommonState;
        import peersim.core.Network;
        import peersim.core.Node;
        import peersim.edsim.EDProtocol;
        import peersim.edsim.EDSimulator;

        import java.util.ArrayList;
        import java.util.Map;
        import java.util.HashMap;
        import java.util.Iterator;
        import java.util.List;

public class NeighborProtocolImpl2 implements NeighborProtocol, EDProtocol {
    private int this_pid;
    private int period;
    private int timer_delay;
    private int listener_pid;
    private int verbose = 0;

    private static final String PAR_PERIOD = "period";
    private static final String PAR_TIMERDELAY = "timer_delay";
    private static final String PAR_LISTENER_PID = "listenerpid";

    private static final String tag_probe = "Probe";
    private static final String tag_heartbeat = "Heartbeat";
    private static final String tag_timer = "Timer";

    //Integer timeStamp = 0;

    private List<Long> neighbor_list;
    private Map<Long, Long> neighbor_timers; // Map<NodeID, Timestamp>

    public NeighborProtocolImpl2(String prefix) {
        neighbor_list = new ArrayList<>();
        neighbor_timers = new HashMap<Long, Long>();

        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);

        this.period = Configuration.getInt(prefix+"."+PAR_PERIOD);
        this.timer_delay = Configuration.getInt(prefix + "." + PAR_TIMERDELAY);
        this.listener_pid = Configuration.getPid(prefix + "." + PAR_LISTENER_PID,-1);
        this.verbose = Configuration.getInt(prefix+".verbose");


    }

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



    @Override
    public void processEvent(Node node, int pid, Object event) {
        int emitter_pid = Configuration.lookupPid("emitter");
//        System.out.println("getting protocol " + emitter_pid);
        Emitter impl = (Emitter) node.getProtocol(emitter_pid);
        Message msg = (Message) event;

        //neighbor_timers.replaceAll((k, v) -> (int) v - this.period);


        if (this.verbose != 0) {
            if (CommonState.getIntTime() % 100000 * 60 * 60 == 0) {
                System.err.println("Time " + CommonState.getIntTime());
            }
        }


        if (event instanceof Message) {
//            System.err.println("Node " + node.getID() + " msg src " + msg.getIdSrc() + " dest " + msg.getIdDest() + " " + msg.getTag() + " " + msg.getContent());
            /* on filtre déjà nos messages à nous
                - "Heartbeat" (self-bootstrap)
                - "Timer" pour déclencher les timers des gens
             */

            switch (msg.getTag()) {
                case tag_heartbeat: // self-message toutes les périodes
                        EDSimulator.add(this.period, new Message(node.getID(), -1, tag_heartbeat, tag_heartbeat, this_pid), node, this_pid);
                        //                        System.err.println("emitting from neighbor");

                        // Envoi d'un probe dans le scope pour les voisins, avec un timestamp
                        impl.emit(node, new Message(msg.getIdSrc(), -1, tag_probe, CommonState.getTime(), this_pid));
                        break;


                case tag_probe: // Réception d'un probe de quelqu'un d'autre

                        // rajout du voisin s'il n'est pas dans la liste des voisins
                        if (!neighbor_list.contains(msg.getIdSrc())) {
                            neighbor_list.add(msg.getIdSrc());
                        }
                        if (verbose==1)
                            System.err.println("Node " + node.getID() + ": received probe from " + msg.getIdSrc() + " with " + msg.getContent());

                        // rajout du voisin dans la liste des timers dans tous les cas
                        neighbor_timers.put(msg.getIdSrc(), (Long) msg.getContent());

                        EDSimulator.add(
                                this.timer_delay,
                                new Message(msg.getIdSrc(), node.getID(), tag_timer, msg.getContent(), this_pid),
                                node,
                                this_pid);

                    break;
                case tag_timer:
                    if (verbose==1)
                        System.err.println("Node " + node.getID() + " " + neighbor_list)         ;
                    if (neighbor_list.contains(msg.getIdSrc())) {
                        if ( neighbor_timers.get(msg.getIdSrc()) == msg.getContent()) {
                            neighbor_list.remove(msg.getIdSrc());
//                                System.out.println(node.getID() + ": list " + neighbor_list + "removed " + msg.getContent());
                        }
                    }
                    break;
                default:
                    System.out.println("IN DEFAULT");
                    break;
            }


        }

        return;
    }
}
