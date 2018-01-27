package manet;

import manet.detection.NeighborProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class DensityController implements Control {


    private static final String PAR_NEIGHBOR = "neighbours";
    private static final String PAR_VERBOSE = "verbose";
    private static final String PAR_STEP = "step";

    private final int this_pid;

    private int verbose = 0; // Est-ce que l'on print les résultats sur stdout
    private int step; // step du controlleur

    private double
            dit = 0.0,  // la moyenne du nombre de voisins par noeud à l'instant t (densite)
            eit = 0.0,  // l'écart-type de dit (donc a l'instant t)
            dt  = 0.0,  // densité moyenne sur le temps (avg of d_dt)
            et  = 0.0,  // disparité moyenne de densité sur le temps (avg of d_et)
            edt = 0.0;  // variation de la densité au cours du temps (ecart type des valeurs d_dt,
    // donc de toute la sim jusqu'a mtn)

    // Arrays containing data for dt, et and edt calculations
    private ArrayList<Double>
            d_dt  = new ArrayList<Double>(), // Updated by dit()
            d_et  = new ArrayList<Double>(), // Updated by eit()
            d_edt = new ArrayList<Double>(); // Updates by edt()

    public DensityController(String prefix) {
        this.this_pid = Configuration.getPid(prefix+"."+PAR_NEIGHBOR);
        this.verbose = Configuration.getInt(prefix + "." + PAR_VERBOSE);
        this.step = Configuration.getInt(prefix + "." + PAR_STEP);
    }


    @Override
    // @return true if the simulation has to be stopped, false otherwise.
    public boolean execute() {
        // Over-time averages
        dt = dt();
        et = et();
        edt = edt();

        // 'Live' values
        dit = dit();
        eit = eit();

//        if (this.verbose != 0)
            if (CommonState.getTime() >= CommonState.getEndTime()-step)
                printCols();

        return false;
    }

    /** A l'instant T **/

    /**
     * Calculates the average number of neighbours in the
     * network when called (works on 'live' data)
     * D_i(t) : Moyenne du nombre de voisins par noeud a l'instant t
     *
     * Updates dit and d_dt[]
     *
     * @return double average neighbors per node
     */
    private double dit() {
        double
                sum = 0.0,
                avg = 0.0;

        for (int i = 0 ; i < Network.size() ; i++) {
            double n_neigs = ((NeighborProtocol) Network.get(i).getProtocol(this_pid)).getNeighbors().size();
            sum += n_neigs;
        }

        avg = sum / Network.size();
        d_dt.add(avg);  // Add to history
        return avg;
    }

    /**
     * Calculates the standard deviation
     * E_i(t) : L'ecart type de D_i(t) (dit())
     * Works on 'live' data
     * Updates eit and d_et[]
     *
     * @return l'écart-type de dit
     */
    public double eit() {
        double stdDev = 0.0;
        for (Double d : d_dt) {
            if (this.verbose != 0)
                System.out.format("d: %.2f\n", d);
            stdDev += Math.pow(d - dit, 2);
        }
        if (this.verbose != 0)
            System.out.format("Stddev: %.2f ", stdDev);
        double avg = stdDev/d_dt.size();
        stdDev = Math.sqrt(avg);
        if (this.verbose != 0)
            System.out.format("avg: %.2f stddev: %.2f\n", avg, stdDev);
        d_et.add(stdDev);   // Add to history
        return stdDev;
    }



    /** Stats for all until current **/

    /**
     * La moyenne de l'ensemble des valeurs D_i(t') pour tout t' < t
     * donc densite moyenne sur le temps
     *
     * Updates dt, works with history array
     *
     * @return average density so far
     */
    public double dt() {
        double sum = 0.0, ret = 0.;
        if (!d_dt.isEmpty()) {
            for (Double d : d_dt)
                sum += d;
            ret = sum / d_dt.size();
        }
        return ret;
    }

    /**
     * La moyenne de l'ensemble des valeurs E_i(t') pour tout t' < t
     * donc disparite moyenne de densite sur le temps
     *
     * Updates et, works with history array
     *
     * @return average density so far
     */
    public double et() {
        double sum = 0.0, ret = 0.;
        if (!d_et.isEmpty()) {
            for (Double d : d_et)
                sum += d;
            ret = sum / d_et.size();
        }
        return ret;
    }

    /**
     * L'ecart type des valeurs D_i(t'), pour tout t' <= t, ce qui
     * permet de juger de la variation de la densite au cours du temps.
     * Plus le @return de cette fonction est elevee par rapport au resultat
     * de et(), plus le reseau a change de densite moyenne au cours
     * du temps.
     *
     * Updates recalculates etd, works with history array
     *
     * @return
     */
    public double edt() {
        double stdDev = 0.0;
        if (!d_dt.isEmpty()) {
            for (Double d : d_dt)
                stdDev += Math.pow(dt - d, 2);
            stdDev = stdDev / d_dt.size();
        }
        d_edt.add(stdDev);
        return stdDev;
    }


    /* Getters */
    public double getEdt() { return edt; }
    public double getEt() { return et; }
    public double getDt() { return dt; }
    public double getEit() { return eit; }
    public double getDit() { return dit; }

    /** We're lazy so functions for q10
     * Col1 = D(t=end)
     * Col2 = E(t=end) / D(t=end)
     * Col3 = ED(t=end) / D(t=end)
     * **/
    public double col1() { return getDt(); }
    public double col2() { return (getEt() / getDt()); }
    public double col3() { return (getEdt() / getDt()); }

    public void printCols() {
        String s = String.format("%.2f;%.2f;%.2f", col1(), col2(), col3());
        System.out.println(s);
        System.out.flush();

        if (verbose != 0)
            System.err.println("Should have flushed");
    }

    public void printState() {
        String ddt = "[";
        String det = "[";
        String dedt = "[";

        for (Double d : d_dt)
            ddt += String.format(" %.2f\t", d);

        for (Double d : d_et)
            det += String.format(" %.2f\t", d);

        for (Double d : d_edt)
            dedt += String.format(" %.2f\t", d);

        ddt += "]";
        det += "]";
        dedt += "]";

        String s = String.format("dit: %.2f\teit: %.2f\tdt: %.2f\tet: %.2f\tedt: %.2f\n" +
                        "d_dt:\t%s\n" +
                        "d_et:\t%s\n" +
                        "d_edt:\t%s\n",
                dit, eit, dt, et, edt, ddt, det, dedt);


        System.out.println(s);
    }
}