package network.robot;

import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import data.model.DeviceServerState;
import utils.AppConstants;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceObserverThreadServer implements Runnable {

    // The server socket.
    private static ServerSocket serverSocket = null;
    private static boolean isStopped = false;
    // Обьект синхронизации доступа
    private final Object lock = new Object();
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
                System.out.println("Got a device :)");

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
        NYBus.get().post("END", Channel.FOUR);
        serverForDeviceStopped();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /******************** методы интерфейса DeviceListener *******************/

    private void serverForDeviceStarted(String ip, int port) {
        synchronized (lock) {
            NYBus.get().post(new DeviceServerState(ip, port, true), Channel.ONE);
        }
    }

    private void serverForDeviceStopped() {
        synchronized (lock) {
            NYBus.get().post(new DeviceServerState(null, -1, false), Channel.ONE);
        }
    }
}