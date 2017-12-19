# Manet-Implem

# Exo1

## Q1
> En analysant le code de la classe `PositionProtocolImpl`, donnez l'algorithme général
de déplacement d'un noeud. Il ne vous est pas demandé de code dans cette question.

```java
private void move(Node host){

// If not moving
if(!isMoving()){
    moving=true;
    // Random speed
    this.current_speed= (int) (CommonState.r.nextDouble()*((double)speed_max-(double)speed_min)+(double)speed_min);
    // Make it slower than max in case
    this.current_speed= Math.min(current_speed, speed_max);
    // Gets next destination from the factory
    this.current_destination = PositioningStrategiesFactory.getNextDestinationStrategy().getNextDestination(host, this.current_speed);
}

// Distance to next destination
double distance = this.getCurrentPosition().distance(this.current_destination);//en metre
// Distance par time slice
double distance_to_next = (double)current_speed/1000.0;//on le traduit en metre par milisecondes

// If too far to reach in one hop
if(distance_to_next - distance < 0.0){
    double next_x =  ( distance_to_next * ( (current_destination.getX() - current_position.getX()) / distance )) +current_position.getX();
    double next_y =  ( distance_to_next * ( (current_destination.getY() - current_position.getY()) / distance )) +current_position.getY();
    this.current_position=new Position(next_x, next_y);
}else{
    this.current_position=this.current_destination;
}		

// Destination reached, stop
if(current_position.equals(current_destination)){
    moving=false;
    
    EDSimulator.add(pause, loop_event, host, my_pid);
// Continue moving
}else{
    EDSimulator.add(1, loop_event, host, my_pid);
}

}
```

Movement protocol:

- if not moving
    - assign random speed lower than max
    - use ``
    