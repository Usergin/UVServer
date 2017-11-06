package network.robot;


public interface DeviceListener {
	/****** События *******/
	 
    // Сервер запустился
    void serverForDeviceStarted(String ip, int port);
 
    // Сервер прекратил работу
    void serverForDeviceStopped();
 
    // Подключился новый пользователь
    void onDeviceConnected(DeviceThread device);
 
    // Пользователь отключился
    void onDeviceDisconnected(DeviceThread device);
 
    // Получено сообщение от пользователя
    void onDeviceMessageReceived(DeviceThread device, String message);
    
    // Получено сообщение от пользователя
    void onDeviceMessageReceivedForClient(DeviceThread device, String message);
    
}
