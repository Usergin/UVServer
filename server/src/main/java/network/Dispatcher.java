package network;

import com.google.gson.Gson;
import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.annotation.Subscribe;
import com.mindorks.nybus.event.Channel;
import dagger.Injector;
import dagger.application.NetworkModule;
import data.model.ConnectionState;
import network.client.ClientThread;
import network.robot.DeviceThread;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class Dispatcher {
    @Inject
    Gson gson;
    // Обьект синхронизации доступа
    private static final Object lock = new Object();
    // Client
    // Хранит список пользователей
    private static LinkedHashMap<ClientThread, DeviceThread> clientList = new LinkedHashMap<ClientThread, DeviceThread>();
//    // Хранит слушателей сервера
//    private static LinkedList<ClientListener> listenerList = new LinkedList<ClientListener>();

    // Device
    // Хранит онлайн устройства
    private static LinkedHashMap<String, DeviceThread> deviceThreadList = new LinkedHashMap<String, DeviceThread>();
    //    // Хранит слушателей сервера
//    private static LinkedList<DeviceListener> listenerList = new LinkedList<DeviceListener>();
    private String TAG = Dispatcher.class.getCanonicalName();

    public Dispatcher() {
        System.out.println("Create Dispatcher");
        Injector.inject(this, Arrays.asList(new NetworkModule()));

        NYBus.get().register(this, Channel.THREE, Channel.FOUR, Channel.FIVE, Channel.SIX);
        NYBus.get().enableLogging();

    }

    public static void addDeviceToHashMap(DeviceThread deviceThread) {
        synchronized (lock) {
            System.out.println("addDeviceToHashMap" + deviceThread.getDeviceIp());
            deviceThreadList.put(deviceThread.getDeviceIp(), deviceThread);
        }
    }

    public static void removeDeviceFromHashMap(DeviceThread deviceThread) {
        synchronized (lock) {
            System.out.println("removeDeviceFromHashMap" + deviceThread.getDeviceIp());
            if (deviceThreadList.containsValue(deviceThread))
                deviceThreadList.remove(deviceThread);
        }
    }

    public static void addClientToHashMap(ClientThread clientThread, String ip) {
        synchronized (lock) {
            System.out.println("addClientToHashMap" + clientThread.getClientIp());
            clientList.put(clientThread, deviceThreadList.get(ip));
        }
    }

    public static void removeClientFromHashMap(ClientThread clientThread) {
        synchronized (lock) {
            System.out.println("removeClientFromHashMap" + clientThread.getClientIp());
            clientList.remove(clientThread);
        }
    }


//    @Subscribe(channelId = Channel.THREE)
//    public void onConnectUser(ConnectionState connectionStateConsumer) {
////        if (connectionStateConsumer.getType() == 1)
//
//    }

    // ***************** отправка сообщения всем пользователям ****************/
    @Subscribe(channelId = Channel.FOUR)
    public void sendMessageAllClient(String message) {
        System.out.println("sendMessageAllClient" + message);
        for (ClientThread user : clientList.keySet()) {
            try {
                user.getOutputStream().writeUTF(message);
                user.getOutputStream().flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ***************** отправка сообщения всем устройствам ****************/
    @Subscribe(channelId = Channel.FIVE)
    void sendMessageForAllDevice(String message) {
        System.out.println("sendMessageForAllDevice" + message);
        synchronized (lock) {
            for (DeviceThread device : deviceThreadList.values()) {
                try {
                    device.getOutputStream().writeUTF(message);
                    device.getOutputStream().flush();
                } catch (Exception ex) {
                    System.out.println("Exception!" + ex);

                }
            }
        }
    }

    // ***************** отправка сообщения новому пользователю списка устройств ****************/
    public void sendDeviceIpListToClient(ClientThread user) {
        System.out.println("sendDeviceIpListToClient " + deviceThreadList.entrySet());
        try {
            user.getOutputStream().writeUTF(gson.toJson(deviceThreadList.entrySet()));
            user.getOutputStream().flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ***************** отправка сообщения от устройства определенным пользователям ****************/
    @Subscribe(channelId = Channel.FIVE)
    public void onDeviceMessageReceivedForClient(DeviceThread deviceThread, String message) {
        System.out.println("onDeviceMessageReceivedForClient in clientobserver " + message);
        for (ClientThread clientThread : clientList.keySet()) {
            if (deviceThread.equals(clientList.get(clientThread))) {
                try {
                    System.out.println("onDeviceMessageReceivedForClient " + clientThread.getClientIp());
                    clientThread.getOutputStream().writeUTF(message);
                    clientThread.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // ***************** отправка сообщения от клиента устройству ****************/
    @Subscribe(channelId = Channel.SIX)
    public void onClientMessageReceivedForDevice(ClientThread user, String message) {
        DeviceThread deviceThread = clientList.get(user);
        if (deviceThread != null) {
            System.out.println("onClientMessageReceivedForDevice: " + deviceThread.getDeviceIp());
            try {
                deviceThread.getOutputStream().writeUTF(message);
                deviceThread.getOutputStream().flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//    @Subscribe(channelId = Channel.THREE)
//    public void onChangeStateOfConnection(ConnectionState user, String message) {
//        DeviceThread deviceThread = clientList.get(user);
//        if (deviceThread != null) {
//            System.out.println("onClientMessageReceivedForDevice: " + deviceThread.getDeviceIp());
//            try {
//                deviceThread.getOutputStream().writeUTF(message);
//                deviceThread.getOutputStream().flush();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//        for (ClientListener listener : listenerList) {
//            listener.onClientMessageReceivedForDevice(user, message);
//
//        }

/******************** добавление/удаление слушателей ********************/
//
//    // Добавляет слушателя событий сервера
//    public void addListener(ClientListener listener) {
//        synchronized (lock) {
//            listenerList.add(listener);
//        }
//    }
//
//    // Удаляет слушателя
//    public void removeListener(ClientListener listener) {
//        synchronized (lock) {
//            listenerList.remove(listener);
//        }
//    }


}
