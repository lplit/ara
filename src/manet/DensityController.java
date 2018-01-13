package manet;

import manet.detection.NeighborProtocolImpl;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DensityController implements Control {


    private static final String PAR_NEIGHBOR = "latency";

    private final int this_pid;

    public DensityController(String prefix) {
        this.this_pid = Configuration.getPid(prefix+"."+PAR_NEIGHBOR);
    }


    @Override
    public boolean execute() {

        return false;
    }


    /**
     * Calculates the standart deviation
     * @return
     */
    public double standardDeviation() {
        double
            avg = avgNeighbors(),
            stdDev = 0.0;
        int n_size = Network.size();

        for (int i = 0 ; i < n_size ; i++ ) {
            double n_neigs = ((NeighborProtocolImpl) Network.get(i).getProtocol(this_pid)).getNeighbors().size();
            stdDev += Math.pow(n_neigs - avg, 2);
        }
        return Math.sqrt(stdDev/n_size);
    }


    /**
     * Calculates the average number of neighbors in the
     * network when called
     * @return double average neighbors per node
     */
    public double avgNeighbors() {
        double
            sum = 0.0,
            avg = 0.0;

        for (int i = 0 ; i < Network.size() ; i++) {
            Node n = Network.get(i);
            double n_neigs = ((NeighborProtocolImpl) n.getProtocol(this_pid)).getNeighbors().size();
            sum += n_neigs;
        }

        avg = sum / Network.size();
        return avg;
    }






}
