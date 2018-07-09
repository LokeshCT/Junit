package com.bt.rsqe.projectengine.web.userImport;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProductModelTemplate {
    String name();

    boolean allowNull() default true;

    int priority() default 999;

    boolean locked() default false;
}
