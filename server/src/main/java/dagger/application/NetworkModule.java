package dagger.application;

import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import data.remote.NetworkService;
import gui.control_panel.ControlPanelController;
import network.Dispatcher;
import network.client.ClientThread;
import network.robot.DeviceThread;
import utils.Parser;

import javax.inject.Singleton;

@Module(library = true,overrides = true, injects = {ControlPanelController.class, Dispatcher.class, ClientThread.class, DeviceThread.class}, complete = false)
public class NetworkModule {
    @Provides
    @Singleton
    public NetworkService provideNetworkService() {
        return new NetworkService();
    }
    @Provides
    @Singleton
    Dispatcher provideDispatcher() {
        return new Dispatcher();
    }
    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }
}
