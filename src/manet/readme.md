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

## Q4 

> Re-testez en prenant en SD, la stratégie 2 (la stratégie 1 reste la SPI). Que fait la
  stratégie 2 ?
  
  