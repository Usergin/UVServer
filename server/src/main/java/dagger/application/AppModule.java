package dagger.application;

import dagger.Module;
import dagger.Provides;
import io.datafx.controller.flow.context.ViewFlowContext;
import main.Main;
import network.Dispatcher;
import utils.Parser;

/**
 * Created by OldMan on 18.06.2017.
 */

@Module(injects = Main.class )
public class AppModule {
    //    @Provides
//    FXMLLoader provideFxmlLoader(){
//        return new FXMLLoader();
//    }
    @Provides
    ViewFlowContext provideViewFlowContext() {
        return new ViewFlowContext();
    }

    @Provides
    Parser provideParser() {
        return new Parser();
    }

}
