package tme1;


public  class ReliableBroadcastMessage extends Message {
	
	private  final long idSender;
	private  final long numseq;
			
	public long getIdSender() {
		return idSender;
	}
	public long getNumseq() {
		return numseq;
	}
	public ReliableBroadcastMessage(long idsrc, long iddest, long idSender, long numseq, String tag, Object content, int pid){
		super(idsrc,iddest,tag,content, pid);
		this.idSender=idSender;
		this.numseq=numseq;
	}
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		if(! (o instanceof ReliableBroadcastMessage)){
			return false;
		}
		ReliableBroadcastMessage rbm=(ReliableBroadcastMessage)o;
		return rbm.idSender == this.idSender && rbm.numseq==this.numseq; 
		
	}
	
	@Override
	public int hashCode(){
		
		return (int)numseq* 1000 + (int)idSender ;
	}

}