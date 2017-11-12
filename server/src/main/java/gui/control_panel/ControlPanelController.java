package gui.control_panel;

import com.jfoenix.controls.JFXListView;
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
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    private JFXListView listStateServer;
    @FXML
    private JFXListView listCommand;
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
            controlPanelPresenter.setOnClientServer(true);
        }
        else {
            controlPanelPresenter.setOnDeviceServer(false);
            controlPanelPresenter.setOnClientServer(false);
        }
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
    public void addServerStateToList(String str) {
        Platform.runLater(() -> {
            listStateServer.getItems().add(str);
            int lastElement = listStateServer.getItems().size()-1;
            listStateServer.getSelectionModel().select(lastElement);
            listStateServer.scrollTo(lastElement);
        });
    }

    @Override
    public void addCommandToList(String str) {
        Platform.runLater(() -> {
            listCommand.getItems().add(str);
            int lastElement = listCommand.getItems().size()-1;
            listCommand.getSelectionModel().select(lastElement);
            listCommand.scrollTo(lastElement);
        });
    }
}
