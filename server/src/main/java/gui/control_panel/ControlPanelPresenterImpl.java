package gui.control_panel;

import business.control_panel.ControlPanelInteractor;
import utils.RxBus;

public class ControlPanelPresenterImpl implements ControlPanelPresenter {
    private ControlPanelView controlPanelView;
    private ControlPanelInteractor controlPanelInteractor;

    public ControlPanelPresenterImpl(ControlPanelInteractor controlPanelInteractor) {
        this.controlPanelInteractor = controlPanelInteractor;
    }

    @Override
    public void setControlPanelView(ControlPanelView controlPanelView) {
        this.controlPanelView = controlPanelView;
        RxBus.instanceOf().getDeviceServerState()
                .subscribe(deviceServerState -> {
                    controlPanelView.showSnackBar(deviceServerState.getIp());
                });
        RxBus.instanceOf().getClientServerState()
                .subscribe(clientServerState -> {
                    controlPanelView.showSnackBar(clientServerState.getIp());
                });
        RxBus.instanceOf().getSubjectConnectionState()
                .subscribe(connectionStateConsumer -> {
                    controlPanelView.showSnackBar(connectionStateConsumer.getIp() + connectionStateConsumer.isState());
                });
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
