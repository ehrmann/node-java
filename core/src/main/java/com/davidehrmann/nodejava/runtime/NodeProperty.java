package com.davidehrmann.nodejava.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface NodeProperty {
    String value() default "";
}
