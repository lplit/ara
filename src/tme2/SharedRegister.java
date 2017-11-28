package tme2;

import peersim.core.Node;
import peersim.core.Protocol;

public interface SharedRegister extends Protocol {

	/**
	 * @return la  valeur lue par la dernière opération de lecture
	 */
	int getLastReadValue();

	/**
	 * @return renvoie vrai si une opération de lecture ou d'écriture est en cours, faux sinon
	 */
	boolean isInProgress();

	
	/**
	 * @return renvoie vrai si la dernière opération en cours attend que la 
	 * couche applicative prenne connaissance de sa terminaison, faux sinon
	 */
	boolean isFinished();

	/**
	 * @return renvoie le nom du registre
	 */
	String getName();

	
	/**
	 * permet à la couche applicative de clore l'opération courante
	 */
	void closeOperation();

	/**
	 * Permet d'initier une nouvelle opération d'écriture sur le registre
	 * @param host : le noeud peersim qui souhaite écrire sur le regitre
	 * @param value : la valeur à écrire sur le registre
	 * @throws NotClosedOperationException déclenchée lorsqu'
	 * il existe une opération que host à initié et qu'il n'a pas clos
	 */
	void write(Node host, int value) throws NotClosedOperationException;

	
	/**
	 * Permet d'initier une nouvelle opération de lecture sur le registre
	 * @param host : le noeud peersim qui souhaite lire le regitre
	 * @throws NotClosedOperationException déclenchée lorsqu'
	 * il existe une opération que host à initié et qu'il n'a pas clos
	 */
	void read(Node host) throws NotClosedOperationException;

}
