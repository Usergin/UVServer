package network.client;


import data.model.ClientServerState;
import network.robot.DeviceListener;
import network.robot.DeviceObserverThreadServer;
import network.robot.DeviceThread;
import utils.AppConstants;
import utils.RxBus;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ClientObserverThreadServer implements Runnable, ClientListener,
        DeviceListener {

    // The server socket.
    private static ServerSocket serverSocket = null;
    private static boolean isStopped = false;
    // Обьект синхронизации доступа
    private final Object lock = new Object();
    // Хранит список пользователей
    private static LinkedList<ClientThread> clientList = new LinkedList<ClientThread>();
    // Хранит слушателей сервера
    private static LinkedList<ClientListener> listenerList = new LinkedList<ClientListener>();
    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    private void connect() {
        /*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
        try {
//          callDeviceObserverServer();
            serverSocket = new ServerSocket(AppConstants.CLIENT_PORT);
            System.out.println("Waiting for a client...");
            String ip = InetAddress.getLocalHost()
                    .getHostAddress();
            // Нотификация события: сервер запущен
            serverForClientStarted(ip,  AppConstants.CLIENT_PORT);

        } catch (IOException e) {
            System.out.println(e);
            serverForClientStopped();
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
                        threads[i] = new ClientThread(clientSocket, threads);
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

    @Override
    public void run() {
        // TODO Auto-generated method stub
        connect();
    }

    static LinkedList<ClientThread> getClientList() {
        return clientList;
    }

    public synchronized static boolean isStopped() {
        return isStopped;
    }

    public synchronized void setFalseStopped() {

        isStopped = false;
    }

    public synchronized void stop() {
        isStopped = true;
        sendClientMessage(null, "END");
        serverForClientStopped();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    // ***************** отправка сообщения всем пользователям ****************/
    // sender - отправитель

    void sendClientMessage(ClientThread sender, String message) {
        synchronized (lock) {
            for (ClientThread user : clientList) {
                try {
                    user.getOut().writeUTF(message);
                    user.getOut().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /******************** добавление/удаление слушателей ********************/

    // Добавляет слушателя событий сервера
    public void addListener(ClientListener listener) {
        synchronized (lock) {

            listenerList.add(listener);
        }
    }

    // Удаляет слушателя
    public void removeListener(ClientListener listener) {
        synchronized (lock) {
            listenerList.remove(listener);
        }
    }

    /******************** методы интерфейса ServerListener *******************/

    public void serverForClientStarted(String ip, int port) {
        synchronized (lock) {
            RxBus.instanceOf().setClientServerState
                    (new ClientServerState(ip,port, true));
//            for (ClientListener listener : listenerList) {
//                listener.serverForClientStarted(ip, port);
//            }
        }
    }

    public void serverForClientStopped() {
        synchronized (lock) {
            RxBus.instanceOf().setClientServerState
                    (new ClientServerState(null,-1, true));
//            for (ClientListener listener : listenerList) {
//                listener.serverForClientStopped();
//            }
        }
    }

    public void onClientConnected(ClientThread user) {
        synchronized (lock) {
            for (ClientListener listener : listenerList) {
                listener.onClientConnected(user);
            }
        }
    }

    public void onClientDisconnected(ClientThread user) {
        synchronized (lock) {
            for (ClientListener listener : listenerList) {
                listener.onClientDisconnected(user);
            }
        }
    }

    public void onClientMessageReceived(ClientThread user, String message) {
        synchronized (lock) {
            for (ClientListener listener : listenerList) {
                listener.onClientMessageReceived(user, message);
            }
        }
    }

    public void callDeviceObserverServer() {
        DeviceObserverThreadServer multiDeviceServer = new DeviceObserverThreadServer();
        multiDeviceServer.addListener(this);

    }


    @Override
    public void onDeviceConnected(DeviceThread device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceDisconnected(DeviceThread device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceMessageReceived(DeviceThread device, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceMessageReceivedForClient(DeviceThread device,
                                                 String message) {
        // TODO Auto-generated method stub
        System.out.println("onDeviceMessageReceivedForClient in clientobserver " + message);
        sendClientMessage(null, message);

    }

    @Override
    public void onClientMessageReceivedForDevice(ClientThread user,
                                                 String message) {
        // TODO Auto-generated method stub
        for (ClientListener listener : listenerList) {
            listener.onClientMessageReceivedForDevice(user, message);

        }
    }
}