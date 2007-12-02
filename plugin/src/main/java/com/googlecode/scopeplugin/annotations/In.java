package com.googlecode.scopeplugin.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import com.googlecode.scopeplugin.ScopeType;

@Retention(RUNTIME)
@Documented
public @interface In {
    /**
     * Specifies that the injected value must not be null, false by default.
     */
    boolean required() default false;
    
    
    /**
     * Specifies that the injected value should be removed after action invocation, 
     * false by default.
     */
    boolean remove() default false;    
    
    /**
     * Explicitly specify the scope to search, instead of searching all scopes.
     */
    ScopeType scope() default ScopeType.UNSPECIFIED;
    
    /**
     * The context variable name. Defaults to the name of the annotated field or getter method.
     */
    String value() default "";
}
