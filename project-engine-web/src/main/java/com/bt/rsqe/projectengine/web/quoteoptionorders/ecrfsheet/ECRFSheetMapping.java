package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ECRFSheetMapping {
    String name();

    boolean allowNull() default true;
}
