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
     *     A l'instant T
      */

    /**
     * Calculates the standart deviation
     * E_i(t) : L'ecart type de D_i(t) (avgNeighborsT())
     * @return
     */
    public double standardDeviationT() {
        double
            avg = avgNeighborsT(),
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
     * D_i(t) : Moyenne du nombre de voisins par noeud a l'instant t
     * @return double average neighbors per node
     */
    public double avgNeighborsT() {
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

    /**
     * Pour tout t' < t
     */

    /**
     * La moyenne de l'ensemble des valeurs D_i(t') pour tout t' < t
     * donc densite moyenne sur le temps
     * @return average density so far
     */
    public double avgNeighbors() {
        double avg = 0.0;

        return avg;
    }

    /**
     * La moyenne de l'ensemble des valeurs E_i(t') pour tout t' < t
     * donc disparite moyenne de densite sur le temps
     * @return average density so far
     */
    public double stdDeviation() {
        double stdDev = 0.0;

        return stdDev;
    }

    /**
     * L'ecart type des valeurs D_i(t'), pour tout t' <= t, ce qui
     * permet de juger de la variation de la densite au cours du temps.
     * Plus le @return de cette fonction est elevee par rapport au resultat
     * de stdDeviation(), plus le reseau a change de densite moyenne au cours
     * du temps.
     * @return
     */
    public double stdDevEvolution() {
        double stdDev = 0.0;

        return stdDev;
    }

}
