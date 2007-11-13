package com.googlecode.scopeplugin;

import java.util.HashMap;

import junit.framework.TestCase;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.mock.MockActionInvocation;
import com.opensymphony.xwork.mock.MockActionProxy;

public class ScopeInterceptorTest extends TestCase {
	public void testMethodFromSuperClass() throws Exception {
		// test field annotation
		ScopeInterceptor interceptor = new ScopeInterceptor();
		MockActionInvocation invocation = new MockActionInvocation();
		ActionProxy proxy = new MockActionProxy();
		proxy.setMethod("execute");
		invocation.setProxy(proxy);
		invocation.setInvocationContext(new ActionContext(new HashMap()));
		FakeAction fakeAction = new FakeAction();
		invocation.setAction(fakeAction);
		// make sure this doesn't error out
		interceptor.intercept(invocation);
	}
}
