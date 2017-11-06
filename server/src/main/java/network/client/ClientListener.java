package network.client;

public interface ClientListener {

	/****** События *******/

	// Сервер запустился
    void serverForClientStarted(String ip, int port);

	// Сервер прекратил работу
    void serverForClientStopped();

	// Подключился новый пользователь
    void onClientConnected(ClientThread user);

	// Пользователь отключился
    void onClientDisconnected(ClientThread user);

	// Получено сообщение от пользователя
    void onClientMessageReceived(ClientThread user, String message);

	// Получено сообщение от пользователя
    void onClientMessageReceivedForDevice(ClientThread user,
                                          String message);

}