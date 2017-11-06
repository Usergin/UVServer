package data;

public class ServerState {
    private String ip;
    private int port;
    private boolean state;

    public ServerState(String ip, int port, boolean state) {
        this.ip = ip;
        this.port = port;
        this.state = state;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
