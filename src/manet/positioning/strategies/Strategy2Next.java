package manet.positioning.strategies;

import manet.positioning.NextDestinationStrategy;
import manet.positioning.Position;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Node;

public class Strategy2Next implements NextDestinationStrategy {

	private static final String PAR_POSITIONPID = "positionprotocol";
	private final int position_pid;
	
	public Strategy2Next(String prefix) {
		position_pid=Configuration.getPid(prefix+"."+PAR_POSITIONPID);
	}
	
	@Override
	public Position getNextDestination(Node host, int speed) {
		
		return ((PositionProtocol)host.getProtocol(position_pid)).getCurrentPosition();
/*		return new Position(
				((PositionProtocol) host.getProtocol(position_pid)).getCurrentPosition().getX()+5.0,
				((PositionProtocol) host.getProtocol(position_pid)).getCurrentPosition().getY()+5.0);
				*/
//		return new Position(0.0, 0.0);
	}

}
