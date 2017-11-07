package data.model;

public class ClientServerState extends State {
    private int port;

    public ClientServerState(String ip, int port, boolean state) {
        super(ip, state);
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
