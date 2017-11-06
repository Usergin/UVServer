package gui.control_panel;

public interface ControlPanelPresenter {
    void setControlPanelView(ControlPanelView controlPanelView);
    void setOnDeviceServer(boolean val);
    void setOnUserServer(boolean val);

}
