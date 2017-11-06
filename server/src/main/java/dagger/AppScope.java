package dagger;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by OldMan on 18.06.2017.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface AppScope {
}