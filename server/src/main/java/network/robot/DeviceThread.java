package network.robot;


import network.client.ClientObserverThreadServer;

import java.io.*;
import java.net.Socket;

/**
 * This class is designed for create arduino device thread and send information
 * data to client
 **/
public class DeviceThread extends Thread {
	private Socket clientSocket = null;
	private final DeviceThread[] threads;
	private int maxClientsCount;
	private String status;
	private DeviceObserverThreadServer multiDeviceServer;
	private InputStream sin;
	private OutputStream sout;
	// IP пользователя
	public String userIp = "";
	public DataInputStream in = null;
	public DataOutputStream out = null;
	private ClientObserverThreadServer multiClientThread;

	public DeviceThread(Socket clientSocket, DeviceThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		multiDeviceServer = new DeviceObserverThreadServer();
		// Берем входной и выходной потоки сокета, теперь можем
		// получать и
		// отсылать данные клиенту.
		try {
			sin = clientSocket.getInputStream();
			sout = clientSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Конвертируем потоки в другой тип, чтоб легче обрабатывать
		// текстовые сообщения.
		in = new DataInputStream(sin);
		out = new DataOutputStream(sout);
		start();
	}

	public void run() {
		int maxClientsCount = this.maxClientsCount;
		DeviceThread[] threads = this.threads;
		try {
			// IP пользователя
			userIp = clientSocket.getInetAddress().getHostAddress();
			// Нотификация о подключении нового пользователя
			multiDeviceServer.onDeviceConnected(this);
			// Помещаем пользователя в список пользователей
			DeviceObserverThreadServer.clientList.add(this);
			// Отправляем всем сообщение
			multiDeviceServer.sendMessageDevice(null,
					"Подключено исполнительное устройство: " + userIp);
			while (true) {

				status = in.readUTF();
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
						close();
					} else {
						multiDeviceServer.onDeviceMessageReceivedForClient(
								this, status);
						multiDeviceServer.sendMessageDevice(null, status);
						System.out.println("this line ForClient: " + status);
						System.out
								.println("Sending this line to the server...");
					}

				}

				// -----check isStopped and send quit---------
				if (ClientObserverThreadServer.isStopped()) {
					System.out.println("quit");

					out.writeUTF("quit");
					out.flush();
					close();
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].out.writeUTF("*** A new user " + status
								+ " entered the command post !!! ***");
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
		}
	}

	public void close() {
		System.out.println("I'm close...");

		try {
			// Нотификация: пользователь отключился
			multiDeviceServer.onDeviceDisconnected(this);
			// Отправляем всем сообщение
			multiDeviceServer.sendMessageDevice(null,
					"Отключено исполнительное устройство: " + userIp);
			// Удаляем пользователя со списка онлайн
			DeviceObserverThreadServer.clientList.remove(this);
			in.close();
			out.close();
			clientSocket.close();
			if (clientSocket != null)
				clientSocket.close();
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		}
	}

}