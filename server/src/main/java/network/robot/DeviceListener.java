package network.robot;


public interface DeviceListener {
	/****** ������� *******/
	 
    // ������ ����������
    void serverForDeviceStarted(String ip, int port);
 
    // ������ ��������� ������
    void serverForDeviceStopped();
 
    // ����������� ����� ������������
    void onDeviceConnected(DeviceThread device);
 
    // ������������ ����������
    void onDeviceDisconnected(DeviceThread device);
 
    // �������� ��������� �� ������������
    void onDeviceMessageReceived(DeviceThread device, String message);
    
    // �������� ��������� �� ������������
    void onDeviceMessageReceivedForClient(DeviceThread device, String message);
    
}
