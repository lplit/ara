package tme2;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.util.ExtendedRandom;

public class Task extends Thread{

	protected final Node host;
	private ExtendedRandom random;
	
	public Task(Node host) {
		this.host=host;
		random = new ExtendedRandom(CommonState.r.nextInt());
	}
		
@Override
	public void run() {
		try {
			while(true){
				int val ;
				int op;
				
					val = random.nextInt(100);
					op = random.nextInt(1000)%3;

					
				String[] vars= SharedMemoryControleur.getNamesVar().toArray(new String[0]);
				String var= vars[random.nextInt(vars.length)];
				
				
				if(op != 0){
					SharedMemoryControleur.read(host, var);
					
				}else{
					SharedMemoryControleur.write(host, var, val);
				}
					
					
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NotClosedOperationException e) {
			e.printStackTrace();
		}

	}
	
			
}
