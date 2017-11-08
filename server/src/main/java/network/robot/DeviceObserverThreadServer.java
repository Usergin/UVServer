package network.robot;

import data.model.ConnectionState;
import data.model.DeviceServerState;
import network.client.ClientListener;
import network.client.ClientObserverThreadServer;
import network.client.ClientThread;
import utils.AppConstants;
import utils.RxBus;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class DeviceObserverThreadServer implements Runnable, DeviceListener,
        ClientListener {

    // The server socket.
    private static ServerSocket serverSocket = null;
    private static boolean isStopped = false;
    // Обьект синхронизации доступа
    private final Object lock = new Object();
    private ClientObserverThreadServer multiClientThread;
    // Хранит онлайн устройства
    private static LinkedList<DeviceThread> deviceThreadList = new LinkedList<DeviceThread>();
    // Хранит слушателей сервера
    private static LinkedList<DeviceListener> listenerList = new LinkedList<DeviceListener>();
    // This chat server can accept up to maxClientsCount clients' connections.

    private static final int maxClientsCount = 10;
    private static final DeviceThread[] threads = new DeviceThread[maxClientsCount];

    private void connect() {
        /*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
        try {
//            callClientObserverServer();
            serverSocket = new ServerSocket(AppConstants.DEVICE_PORT);
            System.out.println("Waiting for a device...");
            String ip = InetAddress.getLocalHost()
                    .getHostAddress();
            // Нотификация события: сервер запущен
            serverForDeviceStarted(ip, AppConstants.DEVICE_PORT);
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
                Socket clientSocket = serverSocket.accept();
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

    public static LinkedList<DeviceThread> getDeviceThreadList() {
        return deviceThreadList;
    }

    public static void setDeviceThreadList(LinkedList<DeviceThread> deviceThreadList) {
        DeviceObserverThreadServer.deviceThreadList = deviceThreadList;
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

    void sendMessageDevice(DeviceThread sender, String message) {
        synchronized (lock) {
            for (DeviceThread device : deviceThreadList) {
                try {
                    device.getOutputStream().writeUTF(message);
                    device.getOutputStream().flush();
                } catch (Exception ex) {
                    System.out.println("Exception!" + ex);

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

    private void serverForDeviceStarted(String ip, int port) {
        synchronized (lock) {
            RxBus.instanceOf().setDeviceServerState
                    (new DeviceServerState(ip, port, true));
        }
    }

    private void serverForDeviceStopped() {
        synchronized (lock) {
            RxBus.instanceOf().setDeviceServerState
                    (new DeviceServerState(null, -1, false));
//            for (DeviceListener listener : listenerList) {
//                listener.serverForDeviceStopped();
//            }
        }
    }

    public void onDeviceConnected(DeviceThread device) {
        synchronized (lock) {
            RxBus.instanceOf().setSubjectConnectionState(
                    new ConnectionState(AppConstants.DEVICE_TYPE, device.getDeviceIp(), true));
//            for (DeviceListener listener : listenerList) {
//                listener.onDeviceConnected(device);
//            }
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