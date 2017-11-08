package gui.control_panel;

public interface ControlPanelView {
    void showSnackBar(String message);
    void showProgress(boolean val);
    void openUserConnection(boolean val);
    void openDeviceConnection(boolean val);
    void addServerStateToList(String str);
    void addCommandToList(String str);
}
