package network.robot;

import data.DeviceServerState;
import network.client.ClientListener;
import network.client.ClientObserverThreadServer;
import network.client.ClientThread;
import utils.AppConstants;
import utils.RxBus;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class DeviceObserverThreadServer implements Runnable, DeviceListener,
        ClientListener {

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;
    private static boolean isStopped = false;
    // Обьект синхронизации доступа
    private final Object lock = new Object();
    private ClientObserverThreadServer multiClientThread;
    // Хранит онлайн пользователей чата
    static LinkedList<DeviceThread> clientList = new LinkedList<DeviceThread>();

    // Хранит слушателей сервера
    private static LinkedList<DeviceListener> listenerList = new LinkedList<DeviceListener>();
    // This chat server can accept up to maxClientsCount clients' connections.

    private static final int maxClientsCount = 10;
    private static final DeviceThread[] threads = new DeviceThread[maxClientsCount];

    public void connect() {
        /*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
        try {
            callClientObserverServer();
            serverSocket = new ServerSocket(AppConstants.TYPE_DEVICE_PORT);

            System.out.println("Waiting for a client...");
            String ip = serverSocket.getInetAddress().getLocalHost()
                    .getHostAddress();
            // Нотификация события: сервер запущен
            RxBus.instanceOf().setDeviceServerState(new DeviceServerState(ip, AppConstants.TYPE_DEVICE_PORT, true));
//			serverForDeviceStarted(ip, AppConstants.TYPE_DEVICE_PORT);

        } catch (IOException e) {
            System.out.println(e);
            serverForDeviceStopped();

        }

		/*
		 * Create a client socket for each connection and pass it to a new
		 * client thread.
		 */
        while (!isStopped) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Got a client :)");

                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        threads[i] = new DeviceThread(clientSocket, threads);
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(
                            clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private void callClientObserverServer() {
        multiClientThread = new ClientObserverThreadServer();
        multiClientThread.addListener(this);

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        connect();
    }

    public synchronized static boolean isStopped() {
        return isStopped;
    }

    public synchronized void setFalseStopped() {
        isStopped = false;
    }

    public synchronized void stop() {
        isStopped = true;
        sendMessageDevice(null, "END");
        serverForDeviceStopped();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    // ***************** отправка сообщения всем пользователям ****************/
    // sender - отправитель

    public void sendMessageDevice(DeviceThread sender, String message) {
        synchronized (lock) {
            for (DeviceThread device : clientList) {
                try {
                    device.out.writeUTF(message);
                    device.out.flush();
                } catch (Exception ex) {
                }
            }
        }
    }

    /******************** добавление/удаление слушателей ********************/

    // Добавляет слушателя событий сервера
    public void addListener(DeviceListener listener) {
        synchronized (lock) {
            System.out.println("listener." + listener);
            listenerList.add(listener);
        }
    }

    // Удаляет слушателя
    public void removeListener(DeviceListener listener) {
        synchronized (lock) {
            listenerList.remove(listener);
        }
    }

    /******************** методы интерфейса DeviceListener *******************/

    public void serverForDeviceStarted(String ip, int port) {
        synchronized (lock) {
            for (DeviceListener listener : listenerList) {
                listener.serverForDeviceStarted(ip, port);
            }
        }
    }

    public void serverForDeviceStopped() {
        synchronized (lock) {
            for (DeviceListener listener : listenerList) {
                listener.serverForDeviceStopped();
            }
        }
    }

    public void onDeviceConnected(DeviceThread device) {
        synchronized (lock) {
            for (DeviceListener listener : listenerList) {
                listener.onDeviceConnected(device);
            }
        }
    }

    public void onDeviceDisconnected(DeviceThread device) {
        synchronized (lock) {
            for (DeviceListener listener : listenerList) {
                listener.onDeviceDisconnected(device);
            }
        }
    }

    public void onDeviceMessageReceived(DeviceThread device, String message) {
        synchronized (lock) {
            for (DeviceListener listener : listenerList) {
                listener.onDeviceMessageReceived(device, message);
            }
        }
    }

    @Override
    public void serverForClientStarted(String ip, int port) {
        // TODO Auto-generated method stub

    }

    @Override
    public void serverForClientStopped() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClientConnected(ClientThread user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClientDisconnected(ClientThread user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClientMessageReceived(ClientThread user, String message) {
        // TODO Auto-generated method stub
        // onDeviceMessageReceivedForClient(null, message);
    }

    @Override
    public void onClientMessageReceivedForDevice(ClientThread user,
                                                 String message) {
        // TODO Auto-generated method stub
        System.out.println("onClientMessageReceivedForDevice in Device."
                + message);

        sendMessageDevice(null, message);
    }

    @Override
    public void onDeviceMessageReceivedForClient(DeviceThread device,
                                                 String message) {
        // TODO Auto-generated method stub
        for (DeviceListener listener : listenerList) {
            listener.onDeviceMessageReceivedForClient(device, message);
        }

    }

}