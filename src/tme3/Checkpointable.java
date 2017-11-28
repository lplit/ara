package tme3;

public interface Checkpointable {
	
	NodeState getCurrentState();
	void restoreState(NodeState restored_state);
	void suspend();
	void resume();
}
