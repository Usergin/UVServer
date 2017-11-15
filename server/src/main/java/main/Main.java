package main;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyphLoader;
import dagger.Injector;
import dagger.application.AppModule;
import gui.control_panel.ControlPanelController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import utils.Parser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class Main extends Application {
    private static final Logger LOG = Logger.getLogger(Main.class);
    @FXMLViewFlowContext
    @Inject
    ViewFlowContext viewFlowContext;
    @Inject
    Parser parser;

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOG.info("Application started");
        Injector.inject(this, Arrays.asList(new AppModule()));
        new Thread(() -> {
            try {
                //he just loaded some svg from a font file
                SVGGlyphLoader.loadGlyphsFont(Main.class.getResourceAsStream("/fonts/icomoon.svg"), "icomoon.svg");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
        viewFlowContext.register("stage", primaryStage);

        Flow flow = new Flow(ControlPanelController.class);
        DefaultFlowContainer container = new DefaultFlowContainer();
        flow.createHandler(viewFlowContext).start(container);

        Image applicationIcon = new Image(getClass().getResourceAsStream("/drawables/icon_mo_rf_80x80.png"));
        primaryStage.getIcons().add(applicationIcon);
        primaryStage.setResizable(false);

        JFXDecorator decorator = new JFXDecorator(primaryStage, container.getView(), false, false, false);
        decorator.setMaximized(false);
        Scene scene = new Scene(decorator);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(ControlPanelController.class.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                ControlPanelController.class.getResource("/css/jfoenix-design.css").toExternalForm(),
                ControlPanelController.class.getResource("/css/control_panel.css").toExternalForm());

        primaryStage.setScene(scene);
//        primaryStage.setOnCloseRequest(e -> controller.shutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Application stop");

    }
}
