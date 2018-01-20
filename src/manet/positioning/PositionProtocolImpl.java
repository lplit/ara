package manet.positioning;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PositionProtocolImpl implements PositionProtocol, EDProtocol {

	
	public static final String loop_event="LOOPEVENT";
	
	private static final String PAR_MAXSPEED="maxspeed";
	private static final String PAR_MINSPEED="minspeed";
	private static final String PAR_MAXX="width";
	private static final String PAR_MAXY="height";
	private static final String PAR_TIMEPAUSE="pause";
	
	
	private Position current_position=null;
	
	private final int my_pid;
	
	private final int speed_max;
	private final int speed_min;
	private final double maxx;
	private final double maxy;
	private final int pause;
	
	
	private int current_speed;
	
	private Position current_destination;

	
	private boolean moving=false;
	
	public PositionProtocolImpl(String prefix){
		String tmp[]=prefix.split("\\.");
		my_pid=Configuration.lookupPid(tmp[tmp.length-1]);
		this.speed_max=Configuration.getInt(prefix+"."+PAR_MAXSPEED);
		this.speed_min=Configuration.getInt(prefix+"."+PAR_MINSPEED);
		this.maxx=Configuration.getInt(prefix+"."+PAR_MAXX);
		this.maxy=Configuration.getInt(prefix+"."+PAR_MAXY);
		this.pause=Configuration.getInt(prefix+"."+PAR_TIMEPAUSE);
		System.err.println("Positionprotocol pid " + my_pid);
	}
	
	public Object clone(){
		PositionProtocolImpl res=null;
		try {
			res=(PositionProtocolImpl)super.clone();
		} catch (CloneNotSupportedException e) {}
		return res;
	}
	
	@Override
	public Position getCurrentPosition() {
		
		return this.current_position;
	}
	
	@Override
	public Position getCurrentDestination() {
		return current_destination;
	}

	@Override
	public int getCurrentSpeed() {
		return current_speed;
	}

	@Override
	public boolean isMoving() {
		return moving;
	}
	
	@Override
	public int getMinSpeed() {
		return speed_min;
	}
	
	@Override
	public double getMaxX(){
		return maxx;
	}
	
	@Override
	public double getMaxY(){
		return maxy;
	}
	
	@Override
	public int getMaxSpeed() {
		return this.speed_max;
	}

	@Override
	public int getTimePause() {
		return this.pause;
	}
	
	
	@Override
	public void initialiseCurrentPosition(Node host) {
		current_position= PositioningStrategiesFactory.getInitialPositionStrategy().getInitialPosition(host);
	}
	
	
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
	
	

	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		
		if(pid != my_pid){
			throw new RuntimeException("Receive Event for wrong protocol");
		}
		if(event instanceof String){
			String ev = (String) event;
			if(ev.equals(loop_event)){
				move(node);
				return;
			}
		}
		throw new RuntimeException("Receive unknown Event");

	}
}
