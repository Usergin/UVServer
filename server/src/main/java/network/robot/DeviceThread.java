package network.robot;


import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.event.Channel;
import data.model.ConnectionState;
import network.Dispatcher;
import utils.AppConstants;

import javax.inject.Inject;
import java.io.*;
import java.net.Socket;

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
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;

    DeviceThread(Socket deviceSocket, DeviceThread[] threads) {
        this.deviceSocket = deviceSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
        // Берем входной и выходной потоки сокета, теперь можем
        // получать и
        // отсылать данные клиенту.
        try {
            sin = deviceSocket.getInputStream();
            sout = deviceSocket.getOutputStream();
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
        DeviceThread[] threads = this.threads;
        try {
            // IP пользователя
            deviceIp = deviceSocket.getInetAddress().getHostAddress();
            // Нотификация о подключении нового пользователя
            NYBus.get().post(new ConnectionState(AppConstants.DEVICE_TYPE, deviceIp, true), Channel.THREE);
//			multiDeviceServer.onDeviceConnected(this);
            // Помещаем пользователя в список пользователей
            Dispatcher.addDeviceToHashMap(this);
            dispatcher.sendMessageAllClient("Подключено устройство: "
                    + deviceIp);
            // Отправляем всем сообщение
//			multiDeviceServer.sendMessageDevice(null,
//					"Подключено исполнительное устройство: " + deviceIp);
            while (true) {
                String status = inputStream.readUTF();
                if (status == null) {
                    // Невозможно прочитать данные, пользователь отключился от
                    // сервера
                    close();
                    // Останавливаем бесконечный цикл
                    break;
                } else if (!status.isEmpty()) {
                    // Нотификация: получено сообщение
                    if (String.valueOf(status).equals("END")) {
                        System.out.println("Get end : " + status);
                        System.out.println("Device disconnected");
                        System.out.println("Closing connections & channels.");
                        inputStream.close();
                        outputStream.close();
                        close();
                    } else {
                        dispatcher.onDeviceMessageReceivedForClient(
                                this, status);
                        System.out
                                .println("Sending this line to the server...");
                    }
                }

                // -----check isStopped and send quit---------
                if (DeviceObserverThreadServer.isStopped()) {
                    System.out.println("quit");
                    outputStream.writeUTF("quit");
                    outputStream.flush();
                    close();
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].outputStream.writeUTF("A new device");
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

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(DataOutputStream outputStream) {
        this.outputStream = outputStream;
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

            inputStream.close();
            outputStream.close();
            deviceSocket.close();
            if (deviceSocket != null)
                deviceSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}