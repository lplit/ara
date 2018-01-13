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
    Integer timeStamp = 0;

    private List<Long> neighbor_list;
    private Map<Node, Integer> neighbor_timers;

    public NeighborProtocolImpl(String prefix) {
        neighbor_list = new ArrayList<>();
        neighbor_timers = new HashMap<Node, Integer>();

        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);

        this.period = Configuration.getInt(prefix+"."+PAR_PERIOD);
        this.timer_delay = Configuration.getInt(prefix + "." + PAR_TIMERDELAY);
        this.listener_pid = Configuration.getPid(prefix + "." + PAR_LISTENER_PID,-1);


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
            neighbor_list = new ArrayList<>();
            timeStamp = new Integer(0);
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
            /* on filtre déjà nos messages à nous
                - "Heartbeat" (self-bootstrap)
                - "Timer" pour déclencher les timers des gens
             */
            if (msg.getIdSrc() == msg.getIdDest()) {
                switch (msg.getTag()) {
                    case "Heartbeat":
                        EDSimulator.add(this.period, event, node, pid);

                        // salut y'a-t-il des nouveaux potos dans mon scope
                        impl.emit(node, new Message(node.getID(), 0, "Heartbeat", "Heartbeat", this_pid));


                        break;
                    case "Timer":
                        if (neighbor_list.contains(msg.getContent())) {
                            neighbor_list.remove(msg.getContent());
                        }

                        break;
                }
            } else { // du coup là on ne traite plus que les messages des autres
                switch (msg.getTag()) {
                    case "Heartbeat": // salutations mon nouveau poto
                        if (!neighbor_list.contains(msg.getIdSrc())) {
                            neighbor_list.add(msg.getIdSrc());
                            for (Long l : neighbor_list) {
                                // pour tous les voisins dans la liste, on s'ajoute un timer

                                EDSimulator.add(this.timer_delay, new Message(l, pid, "Timer", l, pid), node, pid);
                                //   neighbor_timers.forEach((k, v) -> System.out.println("node " + k + " ttl " + v));
                            }
                        }
                        //System.out.println(neighbor_list);
                        break;
                    case "Timer":
                    default:
                        System.out.println("IN DEFAULT");
                        break;
                }
            }

        }

        return;
    }
}
