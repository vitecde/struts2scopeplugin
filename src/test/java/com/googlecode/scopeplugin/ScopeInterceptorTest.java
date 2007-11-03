package com.googlecode.scopeplugin;

import java.util.HashMap;

import junit.framework.TestCase;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.mock.MockActionInvocation;

public class ScopeInterceptorTest extends TestCase {
	public void testSessionScope() throws Exception {
		// test field annotation
		ScopeInterceptor interceptor = new ScopeInterceptor();
		MockActionInvocation invocation = new MockActionInvocation();
		HashMap session = new HashMap();
		invocation.setInvocationContext(new ActionContext(new HashMap()));
		invocation.getInvocationContext().setSession(session);
		FakeAction action = new FakeAction();
		action.setTestName("Tom");
		invocation.setAction(action);
		interceptor.intercept(invocation);
		assertEquals("Tom", action.getTestName());
		
		session.clear();
		action.setTestName(null);
		session.put("testName", "Bob");
		interceptor.intercept(invocation);
		assertEquals("Bob", action.getTestName());
		
		// test method annotation
	}
}
