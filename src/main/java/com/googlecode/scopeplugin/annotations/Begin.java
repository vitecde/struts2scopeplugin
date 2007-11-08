package com.googlecode.scopeplugin.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as beginning a long-running conversation, 
 * if none exists, and if the method returns a non-null value 
 * without throwing an exception.
 *  
 * A null outcome never begins a conversation.
 * If the method is of type void, a conversation always
 * begins.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Begin 
{
   /**
    * An OGNL expression for the conversation id. If a 
    * conversation with the same id aready exists, the scope interceptor 
    * will redirect to that conversation.
    */
   String id() default "";
   /**
    * The name of the (natural) conversation to join 
    */
   String conversation() default "";
}