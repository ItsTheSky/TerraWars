package net.itsthesky.terrawars.api.services.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    /**
     * The interface that this service implements.
     * If not specified, the first implemented interface will be used.
     */
    Class<?> value() default Void.class;

}