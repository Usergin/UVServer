package network.robot;


import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import dagger.Injector;
import dagger.application.NetworkModule;
import data.model.ConnectionState;
import network.Dispatcher;
import network.client.ClientObserverThreadServer;
import utils.AppConstants;

import javax.inject.Inject;
import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This class is designed for create arduino device thread and send information
 * data to client
 **/
public class DeviceThread extends Thread {
    @Inject
    Dispatcher dispatcher;
    private Socket deviceSocket = null;
    private final DeviceThread[] threads;
    private int maxClientsCount;
    private InputStream sin;
    private OutputStream sout;
    // IP пользователя
    private String deviceIp = "";
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private DatagramSocket datagramSocket = null;

    DeviceThread(Socket deviceSocket, DeviceThread[] threads) {
        Injector.inject(this, Arrays.asList(new NetworkModule()));
        this.deviceSocket = deviceSocket;
        this.threads = threads;
        maxClientsCount = threads.length;

        try {
            sin = deviceSocket.getInputStream();
            sout = deviceSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Конвертируем потоки в другой тип, чтоб легче обрабатывать
        // текстовые сообщения.
        dataInputStream = new DataInputStream(sin);
        dataOutputStream = new DataOutputStream(sout);
        start();
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        DeviceThread[] threads = this.threads;
        Runnable run = () -> {
            try {
                byte[] message = new byte[10];
                DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                datagramSocket.bind(new InetSocketAddress(AppConstants.DEVICE_PORT));

                while (!ClientObserverThreadServer.isStopped()) {
                    try {
//                            datagramSocket.setSoTimeout(TIMEOUT);
                        datagramSocket.receive(datagramPacket);
                        String text = new String(message, 0, datagramPacket.getLength());
                        System.out.println("command: " + text);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                datagramSocket.close();
                System.out.println("Kill Task");
            } catch (SocketException e) {
                e.printStackTrace();
            }
        };
        new Thread(run).start();


        try {
            // IP пользователя
            deviceIp = deviceSocket.getInetAddress().getHostAddress();
            // Нотификация о подключении нового пользователя
            NYBus.get().post(new ConnectionState(AppConstants.DEVICE_TYPE, deviceIp, true), Channel.THREE);
            // Помещаем пользователя в список пользователей
            Dispatcher.addDeviceToHashMap(this);

            while (!DeviceObserverThreadServer.isStopped()) {

                int size = dataInputStream.readInt();

                final byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);

                dispatcher.sendMessageDeviceToClient(
                        this, buffer);
                System.out
                        .println("Sending this line to the server..." +String.valueOf(buffer)+ buffer.length);

                // -----check isStopped and send quit---------
                if (DeviceObserverThreadServer.isStopped()) {
                    System.out.println("quit");
                    dataOutputStream.writeUTF("quit");
                    dataOutputStream.flush();
                    close();
                }

				/*
                 * Clean up. Set the current thread variable to null so that a
				 * new client could be accepted by the server.
				 */

                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            NYBus.get().post(new ConnectionState(AppConstants.DEVICE_TYPE, getDeviceIp(), false), Channel.THREE);
            Dispatcher.removeDeviceFromHashMap(this);
        }
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    private void close() {
        System.out.println("I'm close...");

        try {
            // Нотификация: пользователь отключился
            NYBus.get().post(new ConnectionState(AppConstants.DEVICE_TYPE, getDeviceIp(), false), Channel.THREE);
            // Отправляем всем сообщение
            Dispatcher.removeDeviceFromHashMap(this);

            dataInputStream.close();
            dataOutputStream.close();
            deviceSocket.close();
            if (deviceSocket != null)
                deviceSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}