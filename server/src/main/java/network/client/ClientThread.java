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
    private int maxClientsCount;
    private String status;
    private InputStream sin;
    private OutputStream sout;
    private static final int TIMEOUT = 3000;

    // IP пользователя
    private String clientIp = "";
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;

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
        inputStream = new DataInputStream(sin);
        outputStream = new DataOutputStream(sout);
        start();
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;
        Runnable run = new Runnable() {
            public void run() {

                try {
                    byte[] message = new byte[10];
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                    DatagramSocket datagramSocket = new DatagramSocket(null);
                    datagramSocket.setReuseAddress(true);
                    datagramSocket.setBroadcast(true);
                    datagramSocket.bind(new InetSocketAddress(AppConstants.CLIENT_PORT));

                    while (!ClientObserverThreadServer.isStopped()) {
                        try {
//                            datagramSocket.setSoTimeout(TIMEOUT);
                            datagramSocket.receive(datagramPacket);
                            String text = new String(message, 0, datagramPacket.getLength());
                            String command = text.substring(0, 2);
                            System.out.println("command: " + command);
                      // проверка скорости на 0
                           if (text.substring(2, text.length()).equals(""))
                                NYBus.get().post(new CommandMessage(clientIp, 0, command), Channel.SEVEN);
                            else
                                NYBus.get().post(new CommandMessage(clientIp, Integer.parseInt(text.substring(2, text.length())), command), Channel.SEVEN);

                            if (command.equalsIgnoreCase(AppConstants.FORWARD)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_UP, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.FORWARD_RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_UPRIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.FORWARD_LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_UPLEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.BACKWARD)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_DOWN, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.BACKWARD_RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_DOWNRIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.BACKWARD_LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_DOWNLEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_RIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(CommandMessage.MESSAGE_LEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(AppConstants.STOP)) {
//                                handler.obtainMessage(CommandMessage.MESSAGE_STOP).sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    datagramSocket.close();
                    System.out.println("Kill Task");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
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

            dispatcher.sendMessageAllClient("Подключен пользователь: "
                    + clientIp);
            dispatcher.sendDeviceIpListToClient(this);

//            outputStream.writeUTF("received"); // отсылаем введенную строку текста
//            // серверу.
//            outputStream.flush();
            while (true) {
                status = inputStream.readUTF();
                if (status == null) {
                    // Невозможно прочитать данные, пользователь отключился от
                    // сервера
                    close();
                    // Останавливаем бесконечный цикл
                    break;
                } else if (!status.isEmpty()) {
                    // Нотификация: получено сообщение
                    System.out.println("status: " + String.valueOf(status));
                    switch (String.valueOf(status)) {
                        case "END":
                            System.out.println("Get end : " + status);
                            System.out.println("Client disconnected");
                            System.out.println("Closing connections & channels.");
                            inputStream.close();
                            outputStream.close();
                            close();
                            break;
                        case "IP":
                            System.out.println("Get ip : " + status);
                            System.out.println("Client choose device");
                            Dispatcher.addClientToHashMap(this, status);
                            break;
                        default:
                            System.out.println("this line ForDevice: " + status);
//                            NYBus.get().post( Channel.THREE);
                            dispatcher.onClientMessageReceivedForDevice(this, status);
                            break;
                    }
                }
                // outputStream.writeUTF("received"); // отсылаем введенную строку текста
                // // серверу.
                // outputStream.flush();

                // -----check isStopped and send quit---------
                if (ClientObserverThreadServer.isStopped()) {
                    System.out.println("quit");
                    outputStream.writeUTF("quit");
                    outputStream.flush();
                    close();
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].outputStream.writeUTF("*** A new client " + status);
                    }
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
            Dispatcher.removeClientFromHashMap(this);
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, false), Channel.THREE);

        }
    }

    public String getClientIp() {
        return clientIp;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private void close() {
        System.out.println("I'm close...");
        try {
            // Отправляем всем сообщение
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, false), Channel.THREE);
            // Удаляем пользователя со списка онлайн
            Dispatcher.removeClientFromHashMap(this);
            inputStream.close();
            outputStream.close();
            clientSocket.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}