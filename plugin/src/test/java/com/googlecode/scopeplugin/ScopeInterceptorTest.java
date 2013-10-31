package com.googlecode.scopeplugin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.apache.struts2.ServletActionContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import com.opensymphony.xwork2.mock.MockActionProxy;

public class ScopeInterceptorTest extends TestCase {
  public void testWrongType() throws Exception
  {
    // test field annotation
    ScopeInterceptor interceptor = new ScopeInterceptor();
    MockActionInvocation invocation = new MockActionInvocation();
    HashMap session = new HashMap();
    session.put("testName", new Object());
    MockActionProxy proxy = new MockActionProxy();
    proxy.setMethod("execute");
    invocation.setProxy(proxy);
    invocation.setInvocationContext(new ActionContext(new HashMap()));
    invocation.getInvocationContext().setSession(session);
    FakeFieldAction fieldAction = new FakeFieldAction();
    invocation.setAction(fieldAction);
    // make sure this doesn't error out
    interceptor.intercept(invocation);

    // test field annotation
    interceptor = new ScopeInterceptor();
    invocation = new MockActionInvocation();
    session = new HashMap();
    session.put("testName", new Object());
    proxy = new MockActionProxy();
    proxy.setMethod("execute");
    invocation.setProxy(proxy);
    invocation.setInvocationContext(new ActionContext(new HashMap()));
    invocation.getInvocationContext().setSession(session);
    FakeMethodAction methodAction = new FakeMethodAction();
    invocation.setAction(methodAction);
    // make sure this doesn't error out
    interceptor.intercept(invocation);
  }

  public void testSessionScope() throws Exception
  {
    // test field annotation
    ScopeInterceptor interceptor = new ScopeInterceptor();
    MockActionInvocation invocation = new MockActionInvocation();
    HashMap session = new HashMap();
    MockActionProxy proxy = new MockActionProxy();
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

  public void testCookieScope() throws Exception
  {
    // Setup the request / response
    ActionContext.setContext(new ActionContext(new HashMap<String, Object>()));
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    ServletActionContext.setRequest(request);
    ServletActionContext.setResponse(response);

    // Configure the request
    final String inName = "cookieIn";
    final String inValue = "cookie in value";
    request.setCookies(new Cookie[]{new Cookie(inName, inValue)});

    // Configure the action
    final String outName = "cookieOut";
    final String outValue = "cookie out value";
    FakeCookieAction cookieAction = new FakeCookieAction();
    cookieAction.setCookieOut(outValue);

    // Configure the action proxy
    MockActionProxy proxy = new MockActionProxy();
    proxy.setMethod("execute");

    // Configure the invocation
    MockActionInvocation invocation = new MockActionInvocation();
    invocation.setProxy(proxy);
    invocation.setInvocationContext(new ActionContext(new HashMap()));
    invocation.setAction(cookieAction);

    // Call the action
    ScopeInterceptor interceptor = new ScopeInterceptor();
    interceptor.intercept(invocation);

    // Verify @In
    assertEquals(inValue, cookieAction.getCookieIn());

    // Verify @In(remove=true)
    for (Cookie c : response.getCookies())
    {
      if (c.getName() == inName)
      {
        assertEquals("", c.getValue());
        break;
      }
    }

    // Verify @Out
    for (Cookie c : response.getCookies())
    {
      if (c.getName() == outName)
      {
        assertEquals(outValue, c.getValue());
        break;
      }
    }
  }

  public void testBeginEnd() throws Exception
  {
    // test field annotation
    ScopeInterceptor interceptor = new ScopeInterceptor();
    HashMap session = new HashMap();

    MockActionInvocation invocation = new MockActionInvocation();
    MockActionProxy proxy = new MockActionProxy();
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
