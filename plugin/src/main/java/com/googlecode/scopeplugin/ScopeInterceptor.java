package com.googlecode.scopeplugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.StrutsException;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.AnnotationUtils;

/**
 * <!-- START SNIPPET: description -->
 * 
 * This interceptor creates the HttpSession. <p/> This is particular usefull
 * when using the &lt;@s.token&gt; tag in freemarker templates. The tag <b>do</b>
 * require that a HttpSession is already created since freemarker commits the
 * response to the client immediately.
 * 
 * <!-- END SNIPPET: description -->
 * 
 * <p/> <u>Interceptor parameters:</u>
 * 
 * 
 * <!-- START SNIPPET: extending -->
 * 
 * <ul>
 * <li>none</li>
 * </ul>
 * 
 * <!-- END SNIPPET: extending -->
 * 
 * 
 * <!-- START SNIPPET: parameters -->
 * 
 * <ul>
 * 
 * <li>None</li>
 * 
 * </ul>
 * 
 * <!-- END SNIPPET: parameters -->
 * 
 * <b>Example:</b>
 * 
 * <pre>
 * &lt;!-- START SNIPPET: example --&gt;
 * &lt;action name=&quot;someAction&quot; class=&quot;com.examples.SomeAction&quot;&gt;
 *     &lt;interceptor-ref name=&quot;create-session&quot;/&gt;
 *     &lt;interceptor-ref name=&quot;defaultStack&quot;/&gt;
 *     &lt;result name=&quot;input&quot;&gt;input_with_token_tag.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * &lt;!-- END SNIPPET: example --&gt;
 * </pre>
 * 
 * @version $Date: 2006-11-06 07:01:43 -0800 (Mon, 06 Nov 2006) $ $Id:
 *          CreateSessionInterceptor.java 471756 2006-11-06 15:01:43Z husted $
 */
public class ScopeInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = -4590322556118858869L;

	private static final Log LOG = LogFactory.getLog(ScopeInterceptor.class);
	private static final Map<Class, CachedMethods> cachedMethods = Collections
			.synchronizedMap(new HashMap<Class, CachedMethods>());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.interceptor.Interceptor#intercept(com.opensymphony.xwork2.ActionInvocation)
	 */
	public String intercept(ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();

		ActionContext ctx = invocation.getInvocationContext();
		Class cls = action.getClass();
		boolean flashScopeUsed = false;

		List<Field> inFields = findAnnotatedFields(cls, false);
		for (Field f : inFields) {
			In in = f.getAnnotation(In.class);
			String propName = in.value();
			if (propName.length() == 0) {
				propName = f.getName();
			}
			Object obj = findObjectInScope(in.scope(), propName, ctx);

			if (in.required() && obj == null) {
				throw new StrutsException("Scope object " + propName
						+ " cannot be found in scope " + in.scope());
			}
			if (obj != null) {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				f.set(action, obj);
				f.setAccessible(accessible);
			}
		}

		List<Method> inMethods = findAnnotatedMethods(cls, false);
		for (Method m : inMethods) {
			In in = m.getAnnotation(In.class);
			String propName = in.value();
			if (propName.length() == 0) {
				propName = determinePropertyName(m.getName(), false);
			}

			flashScopeUsed |= in.scope() == ScopeType.FLASH;

			Object obj = findObjectInScope(in.scope(), propName, ctx);

			if (in.required() && obj == null) {
				throw new StrutsException("Scope object " + propName
						+ " cannot be found in scope " + in.scope());
			}
			if (obj != null) {
				m.invoke(action, obj);
			}
		}

		if (flashScopeUsed) {
			ctx.getSession().remove(ScopeType.FLASH.toString());
		}

		String ret = invocation.invoke();

		List<Field> outFields = findAnnotatedFields(cls, true);
		for (Field f : outFields) {
			Out out = f.getAnnotation(Out.class);
			String propName = out.value();
			if (propName.length() == 0) {
				propName = f.getName();
			}

			boolean accessible = f.isAccessible();
			f.setAccessible(true);
			Object obj = f.get(action);
			f.setAccessible(accessible);

			if (obj != null) {
				putObjectInScope(out.scope(), propName, ctx, obj);
			}
		}

		List<Method> outMethods = findAnnotatedMethods(cls, true);
		for (Method m : outMethods) {
			Out out = m.getAnnotation(Out.class);
			String propName = out.value();
			if (propName.length() == 0) {
				propName = determinePropertyName(m.getName(), true);
			}

			Object obj = m.invoke(action);

			if (obj != null) {
				putObjectInScope(out.scope(), propName, ctx, obj);
			}
		}
		return ret;
	}

	/**
	 * @param in
	 * @param propName
	 * @param ctx
	 */
	private Object findObjectInScope(ScopeType scopeType, String propName,
			ActionContext ctx) {
		Object obj = null;
		if (obj == null
				&& (scopeType == ScopeType.FLASH || scopeType == ScopeType.UNSPECIFIED)) {
			Map session = ctx.getSession();
			if (session != null) {
				Map flash = (Map) session.get(ScopeType.FLASH.toString());
				if (flash != null) {
					obj = flash.get(propName);
				}
			}
		}
		if (obj == null
				&& (scopeType == ScopeType.SESSION || scopeType == ScopeType.UNSPECIFIED)) {
			obj = ctx.getSession().get(propName);
		}
		if (obj == null
				&& (scopeType == ScopeType.APPLICATION || scopeType == ScopeType.UNSPECIFIED)) {
			obj = ctx.getApplication().get(propName);
		}
		return obj;
	}

	private void putObjectInScope(ScopeType scopeType, String propName,
			ActionContext ctx, Object obj) {
		switch (scopeType) {
		case FLASH:
			Map session = ctx.getSession();
			Map<String, Object> flash = (Map<String, Object>) session
					.get(ScopeType.FLASH.toString());
			if (flash == null) {
				flash = new HashMap<String, Object>();
			}
			flash.put(propName, obj);
			session.put(ScopeType.FLASH.toString(), flash);
			break;
		case SESSION:
			ctx.getSession().put(propName, obj);
			break;
		case APPLICATION:
			ctx.getApplication().put(propName, obj);
			break;
		}
	}

	private List<Field> findAnnotatedFields(Class cls, boolean out) {
		List<Field> annotatedFields = new ArrayList<Field>();
		Field fields[] = cls.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (out) {
				if (fields[i].getAnnotation(Out.class) != null) {
					annotatedFields.add(fields[i]);
				}
			} else {
				if (fields[i].getAnnotation(In.class) != null) {
					annotatedFields.add(fields[i]);
				}
			}
		}
		return annotatedFields;
	}

	private List<Method> findAnnotatedMethods(Class cls, boolean out) {
		CachedMethods cache = cachedMethods.get(cls);
		if (cache == null) {
			cache = new CachedMethods();
			cachedMethods.put(cls, cache);
		}
		List<Method> methods = null;
		if (out) {
			methods = cache.getOutMethods();
			if (methods == null) {
				methods = AnnotationUtils.findAnnotatedMethods(cls, Out.class);
				cache.setOutMethods(methods);
			}
		} else {
			methods = cache.getInMethods();
			if (methods == null) {
				methods = AnnotationUtils.findAnnotatedMethods(cls, In.class);
				cache.setInMethods(methods);
			}
		}
		return methods;
	}

	private String determinePropertyName(String methodName, boolean out) {
		String name = methodName;
		if (!out && methodName.startsWith("set")) {
			name = name.substring("set".length());
		} else if (out) {
			if (methodName.startsWith("get") || methodName.startsWith("has")) {
				name = name.substring("get".length());
			} else if (methodName.startsWith("is")) {
				name = name.substring("is".length());
			}
		}
		if (Character.isUpperCase(name.charAt(0))) {
			if (name.length() == 1 || !Character.isUpperCase(name.charAt(1))) {
				name = Character.toLowerCase(name.charAt(0))
						+ name.substring(1);
				return name;
			}
		}
		return name;
	}

	private static class CachedMethods {
		private List<Method> inMethods;
		private List<Method> outMethods;

		/**
		 * @return the inMethods
		 */
		public List<Method> getInMethods() {
			return inMethods;
		}

		/**
		 * @param inMethods
		 *            the inMethods to set
		 */
		public void setInMethods(List<Method> inMethods) {
			this.inMethods = inMethods;
		}

		/**
		 * @return the outMethods
		 */
		public List<Method> getOutMethods() {
			return outMethods;
		}

		/**
		 * @param outMethods
		 *            the outMethods to set
		 */
		public void setOutMethods(List<Method> outMethods) {
			this.outMethods = outMethods;
		}
	}

}
