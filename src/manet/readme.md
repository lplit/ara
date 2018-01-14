# Manet-Implem

# Exo1

## Q1

> En analysant le code de la classe `PositionProtocolImpl`, donnez l'algorithme général
de déplacement d'un noeud. Il ne vous est pas demandé de code dans cette question.

Movement protocol:

- if not moving
    - assign random speed lower than max
- moving 
    - use `PositioningStrategiesFactory` to get next destination
    - Calc distance to next destination
    - if too far to reach `destination` in one hop
        - calculate next `x` and `y`
        - move to that position
    - if destination reached, stop
    - else continue running

L'algorithme utilise le protocole de déplacement suivant:
Une valeur de la vitesse est aléatoirement choisie dans l'intervalle `[speed_min; speed_max]`.
Vu qu'on est en temps discretisé, `distance_to_next` représente la distance parcourue au
 Une fois la destination atteinte, le noeud s'arrête pendant un tic.

## Q2

```
simulation.endtime 50000
random.seed 5
network.size 10
init.initialisation Initialisation
control.graph GraphicalMonitor
control.graph.positionprotocol position
control.graph.time_slow 0.0002
control.graph.step 1
```

## Q3 

> Que fait strat 1?

La stratégie 1 choisit aléatoirement la prochaine destination dans les intervalles
[0; maxX] et [0; maxY].

## Q4

> Re-testez en prenant en SD, la stratégie 2 (la stratégie 1 reste la SPI). Que fait la stratégie 2 ?

La stratégie 2 choisit comme destination l'endroit courant du noeud, celui-ci reste immobile.

## Q5

```java
package manet.communication;

import manet.Message;
import manet.positioning.Position;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDSimulator;

public class EmitterImpl implements Emitter {

    private int latency;
    private int scope;
    private int this_pid;
    private int position_protocol;

    private static final String PAR_LATENCY = "latency";
    private static final String PAR_SCOPE = "scope";
    private static final String PAR_POSITIONPROTOCOL = "positionprotocol";

    public EmitterImpl(String prefix) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);
        this.position_protocol=Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
        this.scope = Configuration.getInt(prefix + "." + PAR_SCOPE);
    }

    @Override
    public void emit(Node host, Message msg) {
        PositionProtocol prot = (PositionProtocol) host.getProtocol(position_protocol);

        for (int i=0; i < Network.size(); i++) {
            Node n = Network.get(i);
            PositionProtocol prot2 = (PositionProtocol) n.getProtocol(position_protocol);
            double dist =prot.getCurrentPosition().distance(prot2.getCurrentPosition());
            if (dist < scope && n.getID() != host.getID()) {
                EDSimulator.add(latency, new Message(msg.getIdSrc(), n.getID(), msg.getTag(), msg.getContent(), msg.getPid()), n, msg.getPid());
            }
        }

    }

    @Override
    public int getLatency() { return latency; }

    @Override
    public int getScope() { return scope; }

    @Override
    public Object clone(){
        EmitterImpl res=null;
        try {
            res=(EmitterImpl)super.clone();
        } catch (CloneNotSupportedException e) {}
        return res;
    }
}
```

## Q6

```java
package manet.detection;

import manet.Message;
import manet.communication.EmitterImpl;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
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

    public NeighborProtocolImpl(String prefix) {
        neighbor_list = new ArrayList<>();

        String tmp[]=prefix.split("\\.");
        this_pid= Configuration.lookupPid(tmp[tmp.length-1]);
        this.period = Configuration.getInt(prefix+"."+PAR_PERIOD);
        this.timer_delay = Configuration.getInt(prefix + "." + PAR_TIMERDELAY);
        this.listener_pid = Configuration.getPid(prefix + "." + PAR_LISTENER_PID,-1);
        }

    @Override
    public List<Long> getNeighbors() { return neighbor_list; }

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

        if (event instanceof Message) {
            switch (msg.getTag()) {
                case "Heartbeat":
                    if (msg.getIdSrc() == msg.getIdDest()) {
                        EDSimulator.add(this.period, event, node, pid);
                        impl.emit(node, new Message(node.getID(), 0, "Heartbeat", "Heartbeat", this_pid));
                    }
                    else {
                        if(!neighbor_list.contains(msg.getIdSrc()))
                            neighbor_list.add(msg.getIdSrc());
                        break;
                    }
                    break;
                default:
                    System.out.println("IN DEFAULT");
            }
        }
        else {
            System.out.println("no good message");
        }
        return;
    }
}
```

## Q7

Oui.

## Q8

Strategy3InitNext fait converger les points vers le milieu, et assure que tous
les noeuds sont à portée les uns des autres, dans un rayon de `scope -
marge`.

| Stratégie de placement initial | stratégie de déplacement | Graphe | Commentaires |
|--------------------------------|---|---| --- |
| Strategy1InitNext | Strategy1InitNext | Connexe |
| Strategy1InitNext | Strategy2Next |  | bouge ap normal |
| Strategy1InitNext | Strategy3InitNext | Connexe | Converge vers le milieu |
| Strategy1InitNext | Strategy4Next | | bouge ap, ça bouge seulement si on a exactement deux noeuds dans le réseau |
| Strategy3InitNext | Strategy1InitNext | Connexe | Connexe dès le début
| Strategy3InitNext | Strategy2Next | Connexe | spawn au milieu et ne bouge pas |
| Strategy3InitNext | Strategy3InitNext | Connexe |
| Strategy3InitNext | Strategy4Next | Connexe | Ça chill dans le scope |
| Strategy5Init | Strategy1InitNext | Connexe| Connexe dès le début car apparaissent au même endroit, après les nodes se barrent de ouf |
| Strategy5Init | Strategy2Next | Connexe | Connexe et ne bouge pas |
| Strategy5Init | Strategy3InitNext | Connexe | cf Protocol3 |
| Strategy5Init | Strategy4Next | Connexe | Trenquille dans le scope |
| Strategy6Init | Strategy1InitNext | Connexe| `protocol.emitter.latency 0` |
| Strategy6Init | Strategy2Next | Connexe | `protocol.emitter.latency 0` |
| Strategy6Init | Strategy3InitNext | Connexe | `protocol.emitter.latency 0` |
| Strategy6Init | Strategy4Next | Connexe | `protocol.emitter.latency 0` |

Les stratégies font genre des formes. Étoile toussa, 'pis aussi un genre
de tas.


## Q10


| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 | 0.49 | 0.35 | 0.04 |
| 250 | 1 | 1 | 1.89 | 0.22 | 0.05 |
| 375 | 1 | 1 | 4.01 | 0.20 | 0.07 |
| 500 | 1 | 1 | 6.30 | 0.21 | 0.10 |
| 625 | 1 | 1 | 9.41 | 0.22 | 0.17 |
| 750 | 1 | 1 | 12.56 | 0.27 | 0.29 |
| 875 | 1 | 1 | 15.07 | 0.30 | 0.42 |
| 1000 | 1 | 1 | 17.67 | 0.36 | 0.71 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3 | 15.38 | 0.25 | 0.30 |
| 250 | 3 | 3 | 13.42 | 0.27 | 0.30 |
| 375 | 3 | 3 | 12.85 | 0.23 | 0.22 |
| 500 | 3 | 3 | 13.27 | 0.29 | 0.34 |
| 625 | 3 | 3 | 12.35 | 0.27 | 0.30 |
| 750 | 3 | 3 | 12.80 | 0.27 | 0.29 |
| 875 | 3 | 3 | 12.74 | 0.30 | 0.35 |
| 1000 | 3 | 3 | 12.67 | 0.33 | 0.43 |