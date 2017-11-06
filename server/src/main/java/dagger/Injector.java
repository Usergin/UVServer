package dagger;

import com.gluonhq.ignite.dagger.DaggerContext;

import java.util.List;

/**
 * Created by OldMan on 19.06.2017.
 */
public class Injector {
    private static  DaggerContext context;

    public static void setContext(DaggerContext localContext) {
        context = localContext;
    }

    public static void inject(Object object,  List<Object> modules) {
        new DaggerContext(object, () -> modules).init();
    }
}
