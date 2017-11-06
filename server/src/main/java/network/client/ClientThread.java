package network.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class is designed for create client thread and send data to device
 **/
public class ClientThread extends Thread {

	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	String status;
	ClientObserverThreadServer multiServer;
	InputStream sin;
	OutputStream sout;
	// IP пользователя
	public String userIp = "";
	public DataInputStream in = null;
	public DataOutputStream out = null;

	public ClientThread(Socket clientSocket, ClientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		multiServer = new ClientObserverThreadServer();
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
		ClientThread[] threads = this.threads;

		try {
			// IP пользователя
			userIp = clientSocket.getInetAddress().getHostAddress();
			// Нотификация о подключении нового пользователя
			multiServer.onClientConnected(this);
			// Помещаем пользователя в список пользователей
			ClientObserverThreadServer.clientlist.add(this);
			// Отправляем всем сообщение
			multiServer.sendClientMessage(null, "Подключен пользователь: "
					+ userIp);

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

						System.out.println("this line ForDevice: " + status);
						multiServer.onClientMessageReceivedForDevice(this,
								status);


					}

				}

				// out.writeUTF("received"); // отсылаем введенную строку текста
				// // серверу.
				// out.flush();

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
								+ " entered the chat room !!! ***");
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
			multiServer.onClientDisconnected(this);
			// Отправляем всем сообщение
			multiServer.sendClientMessage(null, "Отключен пользователь: "
					+ userIp);
			// Удаляем пользователя со списка онлайн
			ClientObserverThreadServer.clientlist.remove(this);
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