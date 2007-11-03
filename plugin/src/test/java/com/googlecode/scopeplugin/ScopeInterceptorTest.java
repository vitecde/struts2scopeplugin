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
		FakeFieldAction fieldAction = new FakeFieldAction();
		fieldAction.setTestName("Tom");
		invocation.setAction(fieldAction);
		interceptor.intercept(invocation);
		assertEquals("Tom", fieldAction.getTestName());
		
		session.clear();
		fieldAction.setTestName(null);
		session.put("testName", "Bob");
		interceptor.intercept(invocation);
		assertEquals("Bob", fieldAction.getTestName());
		
		// test method annotation
		interceptor = new ScopeInterceptor();
		invocation = new MockActionInvocation();
		session = new HashMap();
		invocation.setInvocationContext(new ActionContext(new HashMap()));
		invocation.getInvocationContext().setSession(session);
		FakeMethodAction methodAction = new FakeMethodAction();
		methodAction.setTestName("Bill");
		invocation.setAction(methodAction);
		interceptor.intercept(invocation);
		assertEquals("Bill", methodAction.getTestName());
		
		session.clear();
		methodAction.setTestName(null);
		session.put("testName", "George");
		interceptor.intercept(invocation);
		assertEquals("George", methodAction.getTestName());
	}
}
