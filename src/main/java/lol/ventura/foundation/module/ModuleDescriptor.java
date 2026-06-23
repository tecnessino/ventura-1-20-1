package lol.ventura.foundation.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDescriptor {
    String name();
    Category category();
    String brief();
    int key() default 0;
    boolean enableByDefault() default false;
}