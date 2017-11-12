package gui.control_panel;

import business.control_panel.ControlPanelInteractor;
import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.annotation.Subscribe;
import com.mindorks.nybus.event.Channel;
import data.model.ClientServerState;
import data.model.ConnectionState;
import data.model.DeviceServerState;

public class ControlPanelPresenterImpl implements ControlPanelPresenter {
    private ControlPanelView controlPanelView;
    private ControlPanelInteractor controlPanelInteractor;

    public ControlPanelPresenterImpl(ControlPanelInteractor controlPanelInteractor) {
        this.controlPanelInteractor = controlPanelInteractor;
    }

    @Override
    public void setControlPanelView(ControlPanelView controlPanelView) {
        this.controlPanelView = controlPanelView;
        NYBus.get().register(this, Channel.ONE, Channel.TWO, Channel.THREE);
        NYBus.get().enableLogging();
    }

    @Subscribe(channelId = Channel.ONE)
    public void onChangeDeviceServerStatus(DeviceServerState deviceServerState) {
        if (deviceServerState.isState())
            controlPanelView.addServerStateToList(deviceServerState.getIp() + ":" + deviceServerState.getPort() + " device server run");
        else
            controlPanelView.addServerStateToList(" device server stop");
    }

    @Subscribe(channelId = Channel.TWO)
    public void onChangeClientServerStatus(ClientServerState clientServerState) {
        if (clientServerState.isState())
            controlPanelView.addServerStateToList(clientServerState.getIp() + ":" + clientServerState.getPort() + " client server run");
        else
            controlPanelView.addServerStateToList(" client server stop");
    }

    @Subscribe(channelId = Channel.THREE)
    public void onConnectUser(ConnectionState connectionStateConsumer) {
        String type;
        if (connectionStateConsumer.getType() == 0)
            type = "device";
        else
            type = "client";
        if (connectionStateConsumer.isState())
            controlPanelView.addServerStateToList(type + " " + connectionStateConsumer.getIp() + " connected success");
        else
            controlPanelView.addServerStateToList(type + " " + connectionStateConsumer.getIp() + " disconnected");
//        controlPanelView.showSnackBar(connectionStateConsumer.getIp() + connectionStateConsumer.isState());
    }

    @Override
    public void setOnDeviceServer(boolean val) {
        if (val)
            controlPanelInteractor.startDeviceServer();
        else
            controlPanelInteractor.stopDeviceServer();

    }

    @Override
    public void setOnClientServer(boolean val) {
        if (val)
            controlPanelInteractor.startClientServer();
        else
            controlPanelInteractor.stopClientServer();
    }
}
