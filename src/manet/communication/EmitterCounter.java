package manet.communication;

import manet.Message;
import manet.positioning.PositionProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.List;


/** Décorateur sur Emitter qui compte le nombre de messages en transit.
 *  La classe concrète s'occupe d'envoyer les messages, celle-ci s'occupe de la réception.
 */

public abstract class EmitterCounter implements Emitter, EDProtocol {

    // Nombre de messages en transit.
    protected static int number_of_transits = 0;
    protected Emitter emitter_impl;


    protected int position_protocol;
    protected int this_pid;
    protected int verbose = 0;


    public EmitterCounter(String prefix, Emitter emitter) {
        String tmp[]=prefix.split("\\.");
        this_pid=Configuration.lookupPid(tmp[tmp.length-1]);

        emitter_impl = emitter;

        this.position_protocol= Configuration.getPid(prefix+"."+PAR_POSITIONPROTOCOL);
        this.verbose = Configuration.getInt(prefix+".verbose");


    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (pid == this_pid) {
            number_of_transits--;
            System.err.println(this_pid + " decrementing, left: " + number_of_transits);
        }
    }

    @Override
    /** Cette méthode doit incrémenter number_of_transits au moment de l'émission.
     */
    public abstract void emit(Node host, Message msg);


    @Override
    public int getLatency() {
        return emitter_impl.getLatency();
    }

    @Override
    public int getScope() {
        return emitter_impl.getScope();
    }

    public static int get_number_of_transits() {
        return number_of_transits;
    }


    @Override
    public Object clone(){
        EmitterCounter res=null;
        try {
            res=(EmitterCounter) super.clone();
        } catch (CloneNotSupportedException e) {}

        res.emitter_impl.clone();
        return res;
    }
//    public Node getParentNode();

}
