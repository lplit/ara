package manet.algorithm.gossip;

public class GossipData {
    public int id;
    public long id_initiator;

    @Override
    public String toString() {
        return id + " " + id_initiator;
    }
}
