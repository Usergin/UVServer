package network.client;

import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import dagger.Injector;
import dagger.application.NetworkModule;
import data.model.CommandMessage;
import data.model.ConnectionState;
import network.Dispatcher;
import utils.AppConstants;

import javax.inject.Inject;
import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This class is designed for create client thread and send data to device
 **/
public class ClientThread extends Thread {
    @Inject
    Dispatcher dispatcher;
    private Socket clientSocket = null;
    private final ClientThread[] threads;
    private final int maxClientsCount;
    private InputStream sin;
    private OutputStream sout;
    private static final int TIMEOUT = 3000;
    // IP пользователя
    private String clientIp = "";
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private DatagramSocket datagramSocket = null;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        Injector.inject(this, Arrays.asList(new NetworkModule()));
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
        // Берем входной и выходной потоки сокета, теперь можем
        // получать и
        // отсылать данные клиенту.
        try {
            sin = clientSocket.getInputStream();
            sout = clientSocket.getOutputStream();
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
        ClientThread[] threads = this.threads;


        Runnable run = () -> {
            try {
                byte[] message = new byte[10];
                DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                datagramSocket.bind(new InetSocketAddress(AppConstants.CLIENT_PORT));

                while (!ClientObserverThreadServer.isStopped()) {
                    try {
//                            datagramSocket.setSoTimeout(TIMEOUT);
                        datagramSocket.receive(datagramPacket);
                        dispatcher.onClientDatagramMessageReceivedForDevice(this,datagramPacket);
                        String text = new String(message, 0, datagramPacket.getLength());
                        String command = text.substring(0, 2);
                        System.out.println("command: " + command);
                        NYBus.get().post(new CommandMessage(clientIp, Integer.parseInt(text.substring(3, text.length())), command), Channel.SEVEN);

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
            clientIp = clientSocket.getInetAddress().getHostAddress();
            // Нотификация о подключении нового пользователя
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, true), Channel.THREE);
            // Помещаем пользователя в список пользователей
            Dispatcher.addClientToHashMap(this, null);
            System.out.println("dispatcher client: " + dispatcher);
            dispatcher.sendDeviceIpListToClient(this);

            int size = dataInputStream.readInt();
            byte[] buf = new byte[size];
            dataInputStream.readFully(buf);


            final String clientSelectedIp = new String(buf);
            System.out.println("buffer: " + clientSelectedIp);

            if (clientSelectedIp.startsWith(AppConstants.SELECTED_IP)) {
                System.out.println("selected ip by client: " + clientSelectedIp.substring(4, clientSelectedIp.length()));
                Dispatcher.addClientToHashMap(this, clientSelectedIp.substring(4, clientSelectedIp.length()));
            }

            while (!ClientObserverThreadServer.isStopped()) {

                size = dataInputStream.readInt();
                final byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
//                System.out.println("this line ForDevice: " + new String(buffer) + buffer.length);

                dispatcher.sendMessageFromClientToDevice(this, buffer);

                // -----check isStopped and send quit---------
                if (ClientObserverThreadServer.isStopped()) {
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
            dispatcher.onStopVideoReceivedForDevice(this);
//            Dispatcher.removeClientFromHashMap(this);
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, false), Channel.THREE);

        }
    }

    public String getClientIp() {
        return clientIp;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    private void close() {
        System.out.println("I'm close...");
        try {
            // Отправляем всем сообщение
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, false), Channel.THREE);
            // Удаляем пользователя со списка онлайн
            Dispatcher.removeClientFromHashMap(this);
            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}