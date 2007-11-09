package com.googlecode.scopeplugin;

import com.opensymphony.xwork.interceptor.Interceptor;

/**
 * Provides default implementations of optional lifecycle methods
 */
public abstract class AbstractInterceptor implements Interceptor {

    /**
     * Does nothing
     */
    public void init() {
    }
    
    /**
     * Does nothing
     */
    public void destroy() {
    }

}