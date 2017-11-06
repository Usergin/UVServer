package dagger.application;

import business.control_panel.ControlPanelInteractor;
import business.control_panel.ControlPanelInteractorImpl;
import dagger.AppScope;
import dagger.Module;
import dagger.Provides;
import data.remote.NetworkService;
import gui.control_panel.ControlPanelController;
import gui.control_panel.ControlPanelPresenter;
import gui.control_panel.ControlPanelPresenterImpl;
import utils.Parser;

@Module(library = true, overrides = true, includes = {AppModule.class, NetworkModule.class}, injects = {ControlPanelController.class}
        , complete = false)
public class ControlPanelModule {
    @Provides
    @AppScope
    ControlPanelInteractor provideControlPanelInteractor(NetworkService networkService, Parser parser) {
        return new ControlPanelInteractorImpl(networkService, parser);
    }

    @Provides
    @AppScope
    ControlPanelPresenter provideControlPanelPresenter(ControlPanelInteractor controlPanelInteractor) {
        return new ControlPanelPresenterImpl(controlPanelInteractor);
    }
}
