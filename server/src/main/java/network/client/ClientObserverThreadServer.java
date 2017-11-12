package network.client;


import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import data.model.ClientServerState;
import data.model.ConnectionState;
import network.robot.DeviceListener;
import network.robot.DeviceObserverThreadServer;
import network.robot.DeviceThread;
import utils.AppConstants;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ClientObserverThreadServer implements Runnable{

    // The server socket.
    private static ServerSocket serverSocket = null;
    private static boolean isStopped = false;
    // Обьект синхронизации доступа
    private final Object lock = new Object();
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
            serverSocket = new ServerSocket(AppConstants.CLIENT_PORT);
            System.out.println("Waiting for a client...");
            String ip = InetAddress.getLocalHost()
                    .getHostAddress();
            // Нотификация события: сервер запущен
            serverForClientStarted(ip, AppConstants.CLIENT_PORT);

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
        //todo
        NYBus.get().post( "END", Channel.FOUR);
        serverForClientStopped();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


    /******************** методы интерфейса ServerListener *******************/

    public void serverForClientStarted(String ip, int port) {
        synchronized (lock) {
            NYBus.get().post(new ClientServerState(ip, port, true), Channel.TWO);
        }
    }

    public void serverForClientStopped() {
        synchronized (lock) {
            NYBus.get().post(new ClientServerState(null, -1, false), Channel.TWO);
        }
    }

//    public void onClientConnected(ClientThread user) {
//        synchronized (lock) {
//            for (ClientListener listener : listenerList) {
//                listener.onClientConnected(user);
//            }
//        }
//    }
//
//    public void onClientDisconnected(ClientThread user) {
//        synchronized (lock) {
//            for (ClientListener listener : listenerList) {
//                listener.onClientDisconnected(user);
//            }
//        }
//    }
//
//    public void onClientMessageReceived(ClientThread user, String message) {
//        synchronized (lock) {
//            for (ClientListener listener : listenerList) {
//                listener.onClientMessageReceived(user, message);
//            }
//        }
//    }



}