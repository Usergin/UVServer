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
	private DeviceObserverThreadServer multiDeviceServer;
	private InputStream sin;
	private OutputStream sout;
	// IP пользователя
	private String deviceIp = "";
	private DataInputStream inputStream = null;
	private DataOutputStream outputStream = null;
	private ClientObserverThreadServer multiClientThread;

	DeviceThread(Socket clientSocket, DeviceThread[] threads) {
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
			deviceIp = clientSocket.getInetAddress().getHostAddress();
			// Нотификация о подключении нового пользователя
			multiDeviceServer.onDeviceConnected(this);
			// Помещаем пользователя в список пользователей
			DeviceObserverThreadServer.getDeviceThreadList().add(this);
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
					outputStream.writeUTF("quit");
					outputStream.flush();
					close();
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].outputStream.writeUTF("*** A new user " + status
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
			e.printStackTrace();
		}
	}

	DataOutputStream getOutputStream() {
		return outputStream;
	}

	void setOutputStream(DataOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	private void close() {
		System.out.println("I'm close...");

		try {
			// Нотификация: пользователь отключился
			multiDeviceServer.onDeviceDisconnected(this);
			// Отправляем всем сообщение
			multiDeviceServer.sendMessageDevice(null,
					"Отключено исполнительное устройство: " + deviceIp);
			// Удаляем пользователя со списка онлайн
			DeviceObserverThreadServer.getDeviceThreadList().remove(this);
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