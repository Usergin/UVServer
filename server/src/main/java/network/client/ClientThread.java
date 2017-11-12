package network.client;

import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import dagger.Injector;
import dagger.application.NetworkModule;
import data.model.ConnectionState;
import network.Dispatcher;
import utils.AppConstants;

import javax.inject.Inject;
import java.io.*;
import java.net.Socket;
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
        try {
            // IP пользователя
            clientIp = clientSocket.getInetAddress().getHostAddress();
            // Нотификация о подключении нового пользователя
            NYBus.get().post(new ConnectionState(AppConstants.CLIENT_TYPE, clientIp, true), Channel.THREE);
            // Помещаем пользователя в список пользователей
            Dispatcher.addClientToHashMap(this, null);
            dispatcher.sendMessageAllClient("Подключен пользователь: "
                    + clientIp);
            dispatcher.sendIpDeviceListDeviceToClient(this);
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