package business.control_panel;

import data.remote.NetworkService;
import network.client.ClientObserverThreadServer;
import network.robot.DeviceListener;
import network.robot.DeviceObserverThreadServer;
import network.robot.DeviceThread;
import utils.Parser;

public class ControlPanelInteractorImpl implements ControlPanelInteractor, DeviceListener {
    private NetworkService networkService;
    private Parser parser;
    private DeviceObserverThreadServer multiDeviceServer = null;
    private ClientObserverThreadServer multiThread = null;

    public ControlPanelInteractorImpl(NetworkService networkService, Parser parser) {
    }

    @Override
    public void startDeviceServer() {
        multiDeviceServer = new DeviceObserverThreadServer();
        multiDeviceServer.addListener(this);
        multiDeviceServer.setFalseStopped();
        new Thread(multiDeviceServer).start();
    }

    @Override
    public void startUserServer() {

    }

    @Override
    public void stopDeviceServer() {

    }

    @Override
    public void stopUserServer() {

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
}
