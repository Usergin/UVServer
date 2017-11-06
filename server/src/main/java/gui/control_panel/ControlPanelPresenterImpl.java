package gui.control_panel;

import business.control_panel.ControlPanelInteractor;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
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
    }

    @Override
    public void setOnDeviceServer(boolean val) {
        RxBus.instanceOf().getDeviceServerState().subscribe(deviceServerState -> {
            controlPanelView.showSnackBar(deviceServerState.getIp());
        }) ;
        if (val)
            controlPanelInteractor.startDeviceServer();

    }

    @Override
    public void setOnUserServer(boolean val) {

    }
}
