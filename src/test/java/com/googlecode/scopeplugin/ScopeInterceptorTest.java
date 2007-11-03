package com.googlecode.scopeplugin;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import com.opensymphony.xwork2.mock.MockActionProxy;

public class ScopeInterceptorTest extends TestCase {
	public void testSessionScope() throws Exception {
		// test field annotation
		ScopeInterceptor interceptor = new ScopeInterceptor();
		MockActionInvocation invocation = new MockActionInvocation();
		HashMap session = new HashMap();
		ActionProxy proxy = new MockActionProxy();
		proxy.setMethod("execute");
		invocation.setProxy(proxy);
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
		proxy = new MockActionProxy();
		proxy.setMethod("execute");
		invocation.setProxy(proxy);
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

	public void testBeginEnd() throws Exception {
		// test field annotation
		ScopeInterceptor interceptor = new ScopeInterceptor();
		HashMap session = new HashMap();

		MockActionInvocation invocation = new MockActionInvocation();
		ActionProxy proxy = new MockActionProxy();
		proxy.setMethod("firstPage");
		invocation.setProxy(proxy);
		invocation.setInvocationContext(new ActionContext(new HashMap()));
		invocation.getInvocationContext().setSession(session);
		final FakeConversationAction convAction1 = new FakeConversationAction();
		invocation.setAction(convAction1);
		interceptor.intercept(invocation);
		Map convMap = (Map) session.get(ScopeType.CONVERSATION.toString());
		assertNotNull(convMap);
		
		proxy.setMethod("thirdPage");
		interceptor.intercept(invocation);
		convMap = (Map) session.get(ScopeType.CONVERSATION.toString());
		assertNull(convMap);
	}
}
