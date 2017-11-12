package network.robot;


public interface DeviceListener {
	/****** События *******/
	 
//    // Сервер запущен
//    void serverForDeviceStarted(String ip, int port);
//
//    // Сервер остановлен
//    void serverForDeviceStopped();
 
    // Подключилось новое устройство
    void onDeviceConnected(DeviceThread device);
 
    // Устройство отключилось
    void onDeviceDisconnected(DeviceThread device);
 
    // Получено сообщение от устройства
    void onDeviceMessageReceived(DeviceThread device, String message);
    
    // Получено сообщение для оператора
    void onDeviceMessageReceivedForClient(DeviceThread device, String message);
    
}
