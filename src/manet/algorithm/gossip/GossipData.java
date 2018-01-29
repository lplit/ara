package manet.algorithm.gossip;

public class GossipData {
    public int id;
    public long id_initiator;

    public GossipData() {}

    public GossipData(int new_id, long new_id_initiator) {
        id = new_id;
        id_initiator = new_id_initiator;
    }
    @Override
    public String toString() {
        return id + " " + id_initiator;
    }

}
