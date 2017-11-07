package business.control_panel;

import data.remote.NetworkService;
import network.client.ClientListener;
import network.client.ClientObserverThreadServer;
import network.client.ClientThread;
import network.robot.DeviceListener;
import network.robot.DeviceObserverThreadServer;
import network.robot.DeviceThread;
import utils.Parser;

public class ControlPanelInteractorImpl implements ControlPanelInteractor, DeviceListener, ClientListener {
    private NetworkService networkService;
    private Parser parser;
    private DeviceObserverThreadServer deviceObserverThreadServer = null;
    private ClientObserverThreadServer clientObserverThreadServer = null;

    public ControlPanelInteractorImpl(NetworkService networkService, Parser parser) {
    }

    @Override
    public void startDeviceServer() {
        deviceObserverThreadServer = new DeviceObserverThreadServer();
        deviceObserverThreadServer.addListener(this);
        deviceObserverThreadServer.setFalseStopped();
        new Thread(deviceObserverThreadServer).start();
    }

    @Override
    public void startClientServer() {
        clientObserverThreadServer = new ClientObserverThreadServer();
        clientObserverThreadServer.addListener(this);
        clientObserverThreadServer.setFalseStopped();
        new Thread(clientObserverThreadServer).start();
    }

    @Override
    public void stopDeviceServer() {
        if (deviceObserverThreadServer != null)
            deviceObserverThreadServer.stop();
    }

    @Override
    public void stopClientServer() {
        if (clientObserverThreadServer != null)
            clientObserverThreadServer.stop();
    }

    @Override
    public void serverForDeviceStarted(String ip, int port) {

    }

    @Override
    public void serverForDeviceStopped() {

    }

    @Override
    public void onDeviceConnected(DeviceThread device) {

    }

    @Override
    public void onDeviceDisconnected(DeviceThread device) {

    }

    @Override
    public void onDeviceMessageReceived(DeviceThread device, String message) {

    }

    @Override
    public void onDeviceMessageReceivedForClient(DeviceThread device, String message) {

    }

    @Override
    public void serverForClientStarted(String ip, int port) {

    }

    @Override
    public void serverForClientStopped() {

    }

    @Override
    public void onClientConnected(ClientThread user) {

    }

    @Override
    public void onClientDisconnected(ClientThread user) {

    }

    @Override
    public void onClientMessageReceived(ClientThread user, String message) {

    }

    @Override
    public void onClientMessageReceivedForDevice(ClientThread user, String message) {

    }
}
