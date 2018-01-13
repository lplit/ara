package manet.detection;

import manet.Message;
import manet.communication.EmitterImpl;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NeighborProtocolImpl implements NeighborProtocol, EDProtocol {
    private int this_pid;
    private int period;
    private int timer_delay;
    private int listener_pid;



    private static final String PAR_PERIOD = "period";
    private static final String PAR_TIMERDELAY = "timer_delay";
    private static final String PAR_LISTENER_PID = "listenerpid";
    private static final String PAR_INTERNALTIMER = "internal_timer"; // 0 (par défaut) sans timers. 1 avec
    Integer timeStamp = 0;

    private List<Long> neighbor_list;

    private int internal_timer = 0;
    private Map<Long, Integer> neighbor_timers;

    public NeighborProtocolImpl(String prefix) {
        neighbor_list = new ArrayList<>();
        neighbor_timers = new HashMap<Long, Integer>();

        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);

        this.period = Configuration.getInt(prefix+"."+PAR_PERIOD);
        this.timer_delay = Configuration.getInt(prefix + "." + PAR_TIMERDELAY);
        this.listener_pid = Configuration.getPid(prefix + "." + PAR_LISTENER_PID,-1);
        this.internal_timer = Configuration.getInt(prefix + "." + PAR_INTERNALTIMER, 0);
        System.out.println("internal timer period " + this.period + " delay " + this.timer_delay + " internal " + this.internal_timer);


        }

    @Override
    public List<Long> getNeighbors() {
        return neighbor_list;
    }

    @Override
    public Object clone() {
        NeighborProtocolImpl res = null;
        try {
            res = (NeighborProtocolImpl) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        return res;
    }


    @Override
    public void processEvent(Node node, int pid, Object event) {
        int emitter_pid = Configuration.lookupPid("emitter");
        EmitterImpl impl = (EmitterImpl) node.getProtocol(emitter_pid);
        Message msg = (Message) event;

        neighbor_timers.replaceAll((k, v) -> (int) v - this.period);


        if (event instanceof Message) {
//           System.err.println("Node " + node.getID() + " msg src " + msg.getIdSrc() + " dest " + msg.getIdDest() + " " + msg.getTag() + " " + msg.getContent());
            /* on filtre déjà nos messages à nous
                - "Heartbeat" (self-bootstrap)
                - "Timer" pour déclencher les timers des gens
             */

            switch (msg.getTag()) {
                case "Heartbeat":
                if (msg.getIdDest() == -1) {
                    EDSimulator.add(this.period, new Message(node.getID(), -1, "Heartbeat", "Heartbeat", this_pid), node, this_pid);
//                        System.err.println(("recvd msg src" + msg.getIdSrc() + " dest " + msg.getIdDest()) + " " + msg.getTag() + neighbor_list);
                    // salut y'a-t-il des nouveaux potos dans mon scope
//                        System.err.println("emitting from neighbor");
                    impl.emit(node, new Message(msg.getIdSrc(), -1, "Heartbeat", "Heartbeat", this_pid));

                    if (this.internal_timer != 0) {
                        this.neighbor_timers.replaceAll((k, v) -> v - this.period);
                    }

                }
                else if (msg.getIdDest() == node.getID()){ // heartbeat de quelqu'un d'autre
                    System.err.println((node.getID() + ": recvd msg src" + msg.getIdSrc() + " dest " + msg.getIdDest()) + " " + msg.getTag() + neighbor_list);
                    if (!neighbor_list.contains(msg.getIdSrc())) {
                        neighbor_list.add(msg.getIdSrc());

                        if (this.internal_timer != 0) {
                            neighbor_timers.put(msg.getIdSrc(), this.timer_delay);

                            for (Long l : neighbor_list) {
                                System.err.println("Adding node " + l);
                                // pour tous les voisins dans la liste, on s'ajoute un timer
                                EDSimulator.add(this.timer_delay, new Message(-1, msg.getIdDest(), "Timer", l, this_pid), node, this_pid);
                            }
                        }

                    }
                }
                else {
                    System.err.println("Not supposed to be here");
                }
                break;
                case "Timer":
//                        System.err.println("TIMER should not be here");
                    System.err.println("TIMER Node " + node.getID() + " msg src " + msg.getIdSrc() + " dest " + msg.getIdDest() + " " + msg.getTag() + " " + msg.getContent());
                    if (this.internal_timer == 0) {
                        if (neighbor_list.contains(msg.getContent())) {
                        neighbor_list.remove(msg.getContent());
                        }
                    }
                    else {
                        if (neighbor_timers.containsKey(msg.getContent()))
                            if (neighbor_timers.get(msg.getContent())  < 0) {
                                System.err.println("Removing a node");
                                neighbor_list.remove(msg.getContent());
                                neighbor_timers.remove(msg.getContent());
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
