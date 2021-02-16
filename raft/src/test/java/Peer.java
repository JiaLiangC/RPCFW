import java.net.InetSocketAddress;

public class Peer {
    public Peer(String id, InetSocketAddress address) {
        this.id = id;
        this.address = address;
    }

    private String id;
        private InetSocketAddress address;

    public Peer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
}
