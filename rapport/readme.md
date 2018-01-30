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

Average over 10 runs -+ standard deviation

| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 | 0.0900 +- 0.0148 | 2.0340 +- 0.2968 | 0.1940 +- 0.0220 |
| 250 | 1 | 1 | 0.3430 +- 0.0344 | 0.9840 +- 0.0822 | 0.1850 +- 0.0242 |
| 375 | 1 | 1 | 0.7130 +- 0.0447 | 0.6550 +- 0.0457 | 0.1790 +- 0.0230 |
| 500 | 1 | 1 | 1.1980 +- 0.0473 | 0.5700 +- 0.0460 | 0.2040 +- 0.0265 |
| 625 | 1 | 1 | 1.7760 +- 0.0967 | 0.4730 +- 0.0581 | 0.2120 +- 0.0366 |
| 750 | 1 | 1 | 2.2740 +- 0.1165 | 0.4630 +- 0.0518 | 0.2320 +- 0.0325 |
| 875 | 1 | 1 | 2.7320 +- 0.1012 | 0.4240 +- 0.0600 | 0.2270 +- 0.0514 |
| 1000 | 1 | 1 | 3.2600 +- 0.0892 | 0.4600 +- 0.0632 | 0.2740 +- 0.0541 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3 | 2.7820 +- 0.1310 | 0.4280 +- 0.0787 | 0.2630 +- 0.0639 |
| 250 | 3 | 3 | 2.5410 +- 0.0575 | 0.4630 +- 0.0520 | 0.2480 +- 0.0366 |
| 375 | 3 | 3 | 2.3370 +- 0.1246 | 0.4380 +- 0.0371 | 0.2200 +- 0.0241 |
| 500 | 3 | 3 | 2.3830 +- 0.0684 | 0.4320 +- 0.0426 | 0.2300 +- 0.0335 |
| 625 | 3 | 3 | 2.3250 +- 0.0745 | 0.4730 +- 0.0473 | 0.2450 +- 0.0472 |
| 750 | 3 | 3 | 2.3170 +- 0.0714 | 0.4590 +- 0.0532 | 0.2330 +- 0.0484 |
| 875 | 3 | 3 | 2.3390 +- 0.1023 | 0.4400 +- 0.0369 | 0.2140 +- 0.0201 |
| 1000 | 3 | 3 | 2.3450 +- 0.1551 | 0.4720 +- 0.0654 | 0.2480 +- 0.0531 |



Averages and standard deviations over 100 iterations with random seeds.

| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 | 0.0953 +- 0.0155 | 1.9218 +- 0.2210 | 0.1943 +- 0.0241 |
| 250 | 1 | 1 | 0.3441 +- 0.0302 | 0.9882 +- 0.0791 | 0.1949 +- 0.0296 |
| 375 | 1 | 1 | 0.7277 +- 0.0490 | 0.6890 +- 0.0724 | 0.1982 +- 0.0409 |
| 625 | 1 | 1 | 1.7340 +- 0.0904 | 0.4903 +- 0.0584 | 0.2188 +- 0.0418 |
| 500 | 1 | 1 | 1.1966 +- 0.0595 | 0.5586 +- 0.0468 | 0.2049 +- 0.0278 |
| 750 | 1 | 1 | 2.2763 +- 0.0774 | 0.4570 +- 0.0616 | 0.2306 +- 0.0435 |
| 875 | 1 | 1 | 2.7883 +- 0.0979 | 0.4585 +- 0.0743 | 0.2551 +- 0.0641 |
| 500 | 3 | 3 | 2.3528 +- 0.0868 | 0.4443 +- 0.0459 | 0.2220 +- 0.0379 |
| 1000 | 1 | 1 | 3.2539 +- 0.0870 | 0.4583 +- 0.0753 | 0.2760 +- 0.0793 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3 | 2.8025 +- 0.1088 | 0.4299 +- 0.0680 | 0.2435 +- 0.0559 |
| 250 | 3 | 3 | 2.4760 +- 0.0980 | 0.4381 +- 0.0446 | 0.2293 +- 0.0342 |
| 625 | 3 | 3 | 2.3267 +- 0.1107 | 0.4584 +- 0.0600 | 0.2376 +- 0.0425 |
| 375 | 3 | 3 | 2.3910 +- 0.1285 | 0.4527 +- 0.0567 | 0.2355 +- 0.0429 |
| 750 | 3 | 3 | 2.3350 +- 0.1005 | 0.4686 +- 0.0626 | 0.2413 +- 0.0490 |
| 875 | 3 | 3 | 2.3451 +- 0.0853 | 0.4703 +- 0.0600 | 0.2500 +- 0.0516 |
| 1000 | 3 | 3 | 2.3639 +- 0.0991 | 0.4614 +- 0.0698 | 0.2379 +- 0.0506 |


From Oskar, apres reparations script: 

| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 | 0.5080 +- 0.0256 | 0.3960 +- 0.0265 | 0.0440 +- 0.0049 |
| 250 | 1 | 1 | 1.9040 +- 0.0492 | 0.2320 +- 0.0279 | 0.0480 +- 0.0133 |
| 375 | 1 | 1 | 4.0220 +- 0.0279 | 0.2140 +- 0.0080 | 0.0860 +- 0.0049 |
| 500 | 1 | 1 | 6.5220 +- 0.1167 | 0.2120 +- 0.0204 | 0.1320 +- 0.0248 |
| 625 | 1 | 1 | 9.1280 +- 0.0880 | 0.2140 +- 0.0102 | 0.1740 +- 0.0150 |
| 750 | 1 | 1 | 11.8520 +- 0.2211 | 0.2080 +- 0.0160 | 0.2280 +- 0.0453 |
| 875 | 1 | 1 | 14.5820 +- 0.1886 | 0.2380 +- 0.0075 | 0.3540 +- 0.0287 |
| 1000| 1 | 1 | 16.4140 +- 0.2846 | 0.2820 +- 0.0147 | 0.6000 +- 0.0738 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3 | 14.3420 +- 0.2866 | 0.2180 +- 0.0214 | 0.2880 +- 0.0487 |
| 250 | 3 | 3 | 13.1540 +- 0.1328 | 0.2200 +- 0.0179 | 0.2720 +- 0.0331 |
| 375 | 3 | 3 | 12.4480 +- 0.1015 | 0.2340 +- 0.0120 | 0.2960 +- 0.0361 |
| 500 | 3 | 3 | 12.2320 +- 0.2936 | 0.2260 +- 0.0120 | 0.2700 +- 0.0329 |
| 625 | 3 | 3 | 12.3920 +- 0.0711 | 0.2260 +- 0.0080 | 0.2720 +- 0.0271 |
| 750 | 3 | 3 | 12.5840 +- 0.1606 | 0.2020 +- 0.0098 | 0.2220 +- 0.0256 |
| 875 | 3 | 3 | 12.5640 +- 0.2036 | 0.2320 +- 0.0075 | 0.3120 +- 0.0248 |
| 1000| 3 | 3 | 12.1020 +- 0.3415 | 0.2060 +- 0.0080 | 0.2200 +- 0.0261 |


Apres avoir repare le script, et avec 50 iterations:


| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 | 0.5058  +- 0.0261 | 0.3990 +- 0.0323 | 0.0428 +- 0.0063 |
| 250 | 1 | 1 | 1.8942  +- 0.0628 | 0.2376 +- 0.0196 | 0.0516 +- 0.0110 |
| 375 | 1 | 1 | 3.9176  +- 0.0967 | 0.2028 +- 0.0111 | 0.0718 +- 0.0105 |
| 500 | 1 | 1 | 6.4628  +- 0.1444 | 0.2012 +- 0.0149 | 0.1166 +- 0.0198 |
| 625 | 1 | 1 | 9.1208  +- 0.1883 | 0.2040 +- 0.0178 | 0.1654 +- 0.0386 |
| 750 | 1 | 1 | 11.8792 +- 0.2228 | 0.2180 +- 0.0126 | 0.2428 +- 0.0378 |
| 875 | 1 | 1 | 14.3908 +- 0.2073 | 0.2468 +- 0.0171 | 0.3892 +- 0.0585 |
| 1000| 1 | 1 | 16.4758 +- 0.3213 | 0.2810 +- 0.0193 | 0.6108 +- 0.0855 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3 | 14.5102 +- 0.2655 | 0.2202 +- 0.0198 | 0.3086 +- 0.0597 |
| 250 | 3 | 3 | 13.0276 +- 0.2380 | 0.2210 +- 0.0171 | 0.2756 +- 0.0480 |
| 375 | 3 | 3 | 12.5600 +- 0.2209 | 0.2216 +- 0.0171 | 0.2692 +- 0.0406 |
| 500 | 3 | 3 | 12.4170 +- 0.1680 | 0.2214 +- 0.0169 | 0.2688 +- 0.0412 |
| 625 | 3 | 3 | 12.3490 +- 0.2319 | 0.2242 +- 0.0186 | 0.2666 +- 0.0467 |
| 750 | 3 | 3 | 12.2988 +- 0.2397 | 0.2212 +- 0.0178 | 0.2550 +- 0.0443 |
| 875 | 3 | 3 | 12.2992 +- 0.2703 | 0.2174 +- 0.0171 | 0.2514 +- 0.0452 |
| 1000| 3 | 3 | 12.3032 +- 0.2481 | 0.2180 +- 0.0178 | 0.2492 +- 0.0420 |



After we realized our shit wasn't working, 5 iterations (27-1)



| Portee | SPI | SD | D | E/D | ED/D |
| --- | --- | --- | --- | --- | --- |
| 125 | 1 | 1 |  1.00 +- 0.02 | 0.27 +- 0.02 | 0.04 +- 0.00 |
| 250 | 1 | 1 |  3.81 +- 0.10 | 0.14 +- 0.00 | 0.04 +- 0.00 |
| 375 | 1 | 1 |  8.02 +- 0.20 | 0.13 +- 0.02 | 0.09 +- 0.02 |
| 500 | 1 | 1 | 12.83 +- 0.06 | 0.11 +- 0.02 | 0.09 +- 0.02 |
| 625 | 1 | 1 | 18.77 +- 0.54 | 0.11 +- 0.00 | 0.13 +- 0.01 |
| 750 | 1 | 1 | 24.49 +- 0.13 | 0.10 +- 0.00 | 0.16 +- 0.02 |
| 875 | 1 | 1 | 29.92 +- 0.47 | 0.09 +- 0.00 | 0.16 +- 0.02 |
| 1000| 1 | 1 | 35.66 +- 0.44 | 0.07 +- 0.01 | 0.11 +- 0.04 |
| --- | --- | --- | --- | --- | --- |
| 125 | 3 | 3| 29.94 +- 0.25 | 0.09 +- 0.00 | 0.17 +- 0.02 |
| 250 | 3 | 3| 26.78 +- 0.27 | 0.10 +- 0.01 | 0.21 +- 0.05 |
| 375 | 3 | 3| 25.82 +- 0.41 | 0.09 +- 0.00 | 0.16 +- 0.01 |
| 500 | 3 | 3| 25.75 +- 0.58 | 0.10 +- 0.00 | 0.15 +- 0.02 |
| 625 | 3 | 3| 25.52 +- 0.33 | 0.09 +- 0.00 | 0.15 +- 0.03 |
| 750 | 3 | 3| 25.80 +- 0.23 | 0.10 +- 0.00 | 0.16 +- 0.02 |
| 875 | 3 | 3| 25.61 +- 0.47 | 0.10 +- 0.00 | 0.16 +- 0.02 |
| 1000| 3 | 3| 25.21 +- 0.31 | 0.10 +- 0.00 | 0.16 +- 0.02 |





125 & 1 & 1&  1.00 +- 0.02& 0.27 +- 0.02& 0.04 +- 0.00 \\
250 & 1 & 1&  3.81 +- 0.10& 0.14 +- 0.00& 0.04 +- 0.00 \\
375 & 1 & 1&  8.02 +- 0.20& 0.13 +- 0.02& 0.09 +- 0.02 \\
500 & 1 & 1& 12.83 +- 0.06& 0.11 +- 0.02& 0.09 +- 0.02 \\
625 & 1 & 1& 18.77 +- 0.54& 0.11 +- 0.00& 0.13 +- 0.01 \\
750 & 1 & 1& 24.49 +- 0.13& 0.10 +- 0.00& 0.16 +- 0.02 \\
875 & 1 & 1& 29.92 +- 0.47& 0.09 +- 0.00& 0.16 +- 0.02 \\
1000& 1 & 1& 35.66 +- 0.44& 0.07 +- 0.01& 0.11 +- 0.04 \\
\hline
125 & && 3&29.94 +- 0.25 &0.09 +- 0.00 &0.17 +- 0.02   \\
250 & 3 & 3&26.78 +- 0.27 &0.10 +- 0.01 &0.21 +- 0.05  \\
375 & 3 & 3&25.82 +- 0.41 &0.09 +- 0.00 &0.16 +- 0.01  \\
500 & 3 & 3&25.75 +- 0.58 &0.10 +- 0.00 &0.15 +- 0.02  \\
625 & 3 & 3&25.52 +- 0.33 &0.09 +- 0.00 &0.15 +- 0.03  \\
750 & 3 & 3&25.80 +- 0.23 &0.10 +- 0.00 &0.16 +- 0.02  \\
875 & 3 & 3&25.61 +- 0.47 &0.10 +- 0.00 &0.16 +- 0.02  \\
1000& 3 & 3&25.21 +- 0.31 &0.10 +- 0.00 &0.16 +- 0.02  \\


# Ex2

## Question 1

| Taille | D-end | ED/D end |
| --- | --- | --- |
| 10  | 4.7300 +- 0.0000 | 0.9000 +- 0.0000 |
| 20  | 3.8680 +- 0.9007 | 0.6580 +- 0.2134 |
| 30  | 3.9380 +- 0.5319 | 0.6940 +- 0.1995 |
| 40  | 3.6680 +- 0.7796 | 0.5200 +- 0.2904 |
| 50  | 4.5080 +- 1.1221 | 0.9380 +- 0.4756 |
| 60  | 4.0560 +- 1.0740 | 0.5080 +- 0.2393 |
| 70  | 3.9260 +- 0.5985 | 0.8520 +- 0.2531 |
| 80  | 3.8720 +- 0.7195 | 0.6260 +- 0.2407 |
| 90  | 4.5860 +- 0.6865 | 0.7240 +- 0.2003 |
| 100 | 4.1340 +- 0.2911 | 0.7020 +- 0.1132 |
| 120 | 4.1900 +- 0.5124 | 0.7740 +- 0.1500 |
| 140 | 4.4380 +- 1.0513 | 0.8580 +- 0.2485 |
| 160 | 3.9820 +- 0.6756 | 0.6860 +- 0.2604 |
| 180 | 4.2860 +- 0.5810 | 0.7640 +- 0.2772 |
| 200 | 3.4300 +- 0.5758 | 0.5040 +- 0.1252 |

Sur 5 iterations: 

| Taille | D-end | ED/D end |
| --- | --- | --- |
| 10  | 4.0300 +- 0.0000 | 0.9900 +- 0.0000 |
| 20  | 3.6940 +- 0.8183 | 0.9580 +- 0.4665 |
| 30  | 3.7540 +- 0.5469 | 1.0900 +- 0.2637 |
| 40  | 3.5780 +- 0.6461 | 0.8700 +- 0.2920 |
| 50  | 3.9140 +- 0.5338 | 1.0300 +- 0.2771 |
| 60  | 4.1180 +- 0.9222 | 0.8960 +- 0.2509 |
| 70  | 3.6060 +- 0.8543 | 0.9860 +- 0.3185 |
| 80  | 3.8800 +- 0.3610 | 1.0160 +- 0.2172 |
| 90  | 4.1360 +- 0.2751 | 1.4400 +- 0.3731 |
| 100 | 3.9200 +- 0.4682 | 0.9620 +- 0.1287 |
| 120 | 3.7000 +- 0.3265 | 0.9260 +- 0.1496 |
| 140 | 4.2880 +- 0.9401 | 1.2660 +- 0.2651 |
| 160 | 3.6800 +- 0.5139 | 0.9880 +- 0.1694 |
| 180 | 3.9380 +- 0.3698 | 0.8900 +- 0.1375 |
| 200 | 3.3260 +- 0.1739 | 0.7600 +- 0.1000 |


Sur 100 iterations:


| Taille | D-end | ED/D end |
| --- | --- | --- |
| 10  | 4.0300 +- 0.0000 | 0.5400 +- 0.0000 | 0.9900 +- 0.0000 |
| 20  | 3.9023 +- 0.7477 | 0.5381 +- 0.0313 | 1.0086 +- 0.3287 |
| 30  | 4.0611 +- 0.8702 | 0.5441 +- 0.0288 | 1.0650 +- 0.3057 |
| 40  | 3.9356 +- 0.7541 | 0.5446 +- 0.0304 | 1.0600 +- 0.3549 |
| 50  | 4.0178 +- 0.7891 | 0.5447 +- 0.0349 | 1.0771 +- 0.3783 |
| 60  | 3.9735 +- 0.8343 | 0.5426 +- 0.0352 | 1.0388 +- 0.3300 |
| 70  | 4.0045 +- 0.8162 | 0.5408 +- 0.0268 | 1.0538 +- 0.3727 |
| 80  | 4.0018 +- 0.8210 | 0.5447 +- 0.0308 | 1.0733 +- 0.3529 |
| 90  | 4.0329 +- 0.7609 | 0.5434 +- 0.0299 | 1.0800 +- 0.3699 |
| 100 | 3.9024 +- 0.6445 | 0.5401 +- 0.0293 | 0.9913 +- 0.2628 |
| 120 | 4.0446 +- 0.7920 | 0.5446 +- 0.0362 | 1.0739 +- 0.3254 |
| 140 | 4.0047 +- 0.7523 | 0.5449 +- 0.0360 | 1.0735 +- 0.3470 |
| 160 | 3.9442 +- 0.8509 | 0.5441 +- 0.0304 | 1.0329 +- 0.3229 |
| 180 | 4.0554 +- 0.9028 | 0.5433 +- 0.0300 | 1.0683 +- 0.3055 |
| 200 | 4.1141 +- 0.8804 | 0.5423 +- 0.0309 | 1.0831 +- 0.3207 |




28-01 apres modifs sur 5 iterations:

| Taille | D-end | ED/D end |
| --- | --- | --- |
| 10  |  3.3200 +- 0.0000 | 0.2600 +- 0.0000 |
| 20  |  6.8880 +- 1.0349 | 0.5540 +- 0.3022 |
| 30  | 10.9260 +- 2.1148 | 0.2820 +- 0.2052 |
| 40  | 16.0460 +- 4.0076 | 0.1760 +- 0.1100 |
| 50  | 21.9580 +- 4.9567 | 0.0800 +- 0.0190 |
| 60  | 23.3700 +- 6.0258 | 0.1320 +- 0.0549 |
| 70  | 23.1180 +- 4.7414 | 0.0880 +- 0.0387 |
| 80  | 27.2640 +- 1.7603 | 0.0700 +- 0.0369 |
| 90  | 37.5960 +- 3.8381 | 0.0640 +- 0.0287 |
| 100 | 35.3820 +- 3.2318 | 0.0420 +- 0.0160 |
| 120 | 37.7860 +- 5.2780 | 0.0380 +- 0.0117 |
| 140 | 47.4220 +- 12.208 | 0.0400 +- 0.0167 |
| 160 | 49.1500 +- 10.291 | 0.0380 +- 0.0264 |
| 180 | 62.3760 +- 5.3164 | 0.0380 +- 0.0133 |
| 200 | 50.3340 +- 7.2594 | 0.0240 +- 0.0136 |


meme tableau mais pour latex et avec %.2f

 10  &  3.32 +- 0.00 & 0.26 +- 0.00 \\ \hline
 20  &  6.88 +- 1.03 & 0.55 +- 0.30 \\ \hline
 30  & 10.92 +- 2.11 & 0.28 +- 0.20 \\ \hline
 40  & 16.04 +- 4.00 & 0.17 +- 0.11 \\ \hline
 50  & 21.95 +- 4.95 & 0.08 +- 0.01 \\ \hline
 60  & 23.37 +- 6.02 & 0.13 +- 0.05 \\ \hline
 70  & 23.11 +- 4.74 & 0.08 +- 0.03 \\ \hline
 80  & 27.26 +- 1.76 & 0.07 +- 0.03 \\ \hline
 90  & 37.59 +- 3.83 & 0.06 +- 0.02 \\ \hline
 100 & 35.38 +- 3.23 & 0.04 +- 0.01 \\ \hline
 120 & 37.78 +- 5.27 & 0.03 +- 0.01 \\ \hline
 140 & 47.42 +- 12.2 & 0.04 +- 0.01 \\ \hline
 160 & 49.15 +- 10.2 & 0.03 +- 0.02 \\ \hline
 180 & 62.37 +- 5.31 & 0.03 +- 0.01 \\ \hline
 200 & 50.33 +- 7.25 & 0.02 +- 0.01 \\ \hline


## Question 2

> Expliquez votre démarche pour régler ce problème. Votre solution devra se faire de manière non intrusive, ni dans le code applicatif, ni dans le code qui vous a été fourni.

Decorateur sur `Emitter`, ca se solve avec un `bool`, mais je sais pas trop commment.

On implémente une classe encapsulant la classe `EmitterImpl` avec le Design
Pattern `Decorator`. Cette classe entretient une liste de voisins
atteignables pendant un broadcast, et l'identifiant du noeud parent lui
ayant envoyé le message.

### Algo end of bcast 


 Système de rounds rondes genre. Tuple <id_séquence, size du set>
 Le set == tous les noeuds qui ont reçu le message <id> (Set<NodeId>)

 On stocke la valeur courante du set à la première itération (== 1 comme y'a déjà l'initiateur)
 - à chaque round, le receveur s'ajoute au set (incrémente la taille du set)
 - on compare la taille du set de l'itération courante avec la valeur stockée. 
 Si ça a cbangé, ça veut dire qu'on est dans le même broadcast mais qu'on a atteint des nouveaux voisins entre-temps, donc c'est le même reund.
 - SINON si size(t-1) == size(t) c'est qu'on a plus rien et là notifyAll
 