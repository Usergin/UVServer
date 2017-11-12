package data.model;

public class ConnectionState extends State {
    // 0 - device; 1 - client
    private int type;

    public ConnectionState(int type, String ip, boolean state) {
        super(ip, state);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
