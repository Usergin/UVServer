package data.model;

/**
 * Created by oldman on 13.11.17.
 */
public class CommandMessage {
    private int speed;
    private String command;
    private String ip;

    public CommandMessage(String ip, int speed, String command) {
        this.ip = ip;
        this.speed = speed;
        this.command = command;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
