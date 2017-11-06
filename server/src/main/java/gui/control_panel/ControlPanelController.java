package gui.control_panel;

import business.control_panel.ControlPanelInteractorImpl;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.JFXTreeTableView;
import dagger.Injector;
import dagger.application.ControlPanelModule;
import io.datafx.controller.ViewController;
import io.datafx.controller.ViewNode;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.util.Arrays;

@ViewController(value = "/fxml/control_panel.fxml")
@Singleton
public class ControlPanelController implements ControlPanelView {
    @FXMLViewFlowContext
    @Inject
    ViewFlowContext flowContext;
    @Inject
    ControlPanelPresenter controlPanelPresenter;
    @ViewNode
    private StackPane rootPane;

    //    @ViewNode
//    private JFXTextField txtUsername;
//    @ViewNode
//    private JFXPasswordField txtPassword;
    @FXML
    private JFXTreeTableView tableViewStateServer;
    @FXML
    private JFXToggleButton btnStateServer;
    private static final String ERROR = "error";
    private static final String EM1 = "1em";

    private static final Logger LOG = Logger.getLogger(ControlPanelController.class);
    private StringProperty username = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    BooleanBinding booleanBind = Bindings.or(username.isEmpty(),
            password.isEmpty());

    @PostConstruct
    public void init() throws Exception {
        Injector.inject(this, Arrays.asList(new ControlPanelModule()));
        System.out.println("init " + controlPanelPresenter);
        controlPanelPresenter.setControlPanelView(this);

//        txtUsername.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if (!newVal) {
//                txtUsername.validate();
//            }
//        });
//        txtPassword.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if (!newVal) {
//                txtPassword.validate();
//            }
//        });
//        imgProgress.setVisible(false);
//        btnLogin.disableProperty().bind(booleanBind);
//        username.bind(txtUsername.textProperty());
//        password.bind(txtPassword.textProperty());
    }


    @Override
    public void showSnackBar(String message) {
        System.out.println("message " + message);
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                JFXSnackbar jfxSnackbar = new JFXSnackbar(rootPane);
                jfxSnackbar.show(message, 3000);

            }
    });
    }

    @FXML
    void changeServerState(ActionEvent event) {
        System.out.println("event " + btnStateServer.isSelected());
        if(btnStateServer.isSelected()) {
            controlPanelPresenter.setOnDeviceServer(true);
            controlPanelPresenter.setOnUserServer(true);
        }
//        controlPanelPresenter.onAuthentication(new Authentication(username.get(), getHashedValue(password.get())));
    }

    @Override
    public void showProgress(boolean val) {

//        imgProgress.setVisible(val);
    }

    @Override
    public void openUserConnection(boolean val) {
    }

    @Override
    public void openDeviceConnection(boolean val) {

    }

    @Override
    public void addServerState(String str) {

    }

    @Override
    public void addCommand(String str) {

    }
}
