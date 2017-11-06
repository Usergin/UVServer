package dagger.application;

import dagger.Module;
import dagger.Provides;
import data.remote.NetworkService;
import gui.control_panel.ControlPanelController;

import javax.inject.Singleton;

@Module(library = true,overrides = true, injects = {ControlPanelController.class}, complete = false)
public class NetworkModule {
    @Provides
    @Singleton
    public NetworkService provideNetworkService() {
        return new NetworkService();
    }

}
