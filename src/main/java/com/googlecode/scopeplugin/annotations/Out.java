package com.googlecode.scopeplugin.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import com.googlecode.scopeplugin.ScopeType;

@Retention(RUNTIME)
@Documented
public @interface Out {
    
    /**
     * Specifies that the outjected value must not be null, false by default.
     */
    boolean required() default false;
    
    /**
     * Species the scope, defaults to CONVERSATION.
     */
    ScopeType scope() default ScopeType.CONVERSATION;
    
    /**
     * The context variable name. Defaults to the name of the annotated field or getter method.
     */
    String value() default "";
}
