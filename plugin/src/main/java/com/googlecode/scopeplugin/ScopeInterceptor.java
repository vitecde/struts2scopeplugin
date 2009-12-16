package com.googlecode.scopeplugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.scopeplugin.annotations.Begin;
import com.googlecode.scopeplugin.annotations.End;
import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;

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
 */
public class ScopeInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = -4590322556118858869L;

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

		String actionMethod = invocation.getProxy().getMethod();
		Class clazz = invocation.getAction().getClass();
		Method method = findAnnotatedMethod(clazz, actionMethod);
		if (method != null) {
			Begin begin = method.getAnnotation(Begin.class);
			if (begin != null) {
				if (!ctx.getSession().containsKey(
						ScopeType.CONVERSATION.toString())) {
					ctx.getSession().put(ScopeType.CONVERSATION.toString(),
							new HashMap<String, Object>());
				}
			}
		}

		List<Field> inFields = findAnnotatedFields(cls, false);
		for (Field f : inFields) {
			In in = f.getAnnotation(In.class);
			String propName = in.value();
			if (propName.length() == 0) {
				propName = f.getName();
			}
			Object obj = findObjectInScope(in.scope(), propName, ctx);

			if (in.required() && obj == null) {
				throw new RuntimeException("Scope object " + propName
						+ " cannot be found in scope " + in.scope());
			}
			Class fieldType = f.getType();
			if (obj != null && fieldType.isAssignableFrom(obj.getClass())) {
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
				throw new RuntimeException("Scope object " + propName
						+ " cannot be found in scope " + in.scope());
			}
			Class paramTypes[] = m.getParameterTypes();
			if (obj != null && paramTypes.length > 0
					&& paramTypes[0].isAssignableFrom(obj.getClass())) {
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

			putObjectInScope(out.scope(), propName, ctx, obj);
		}

		List<Method> outMethods = findAnnotatedMethods(cls, true);
		for (Method m : outMethods) {
			Out out = m.getAnnotation(Out.class);
			String propName = out.value();
			if (propName.length() == 0) {
				propName = determinePropertyName(m.getName(), true);
			}

			Object obj = m.invoke(action);

			putObjectInScope(out.scope(), propName, ctx, obj);
		}

		if (method != null) {
			End end = method.getAnnotation(End.class);
			if (end != null) {
				ctx.getSession().remove(ScopeType.CONVERSATION.toString());
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
				&& (scopeType == ScopeType.REQUEST || scopeType == ScopeType.UNSPECIFIED)) {
			obj = ActionContext.getContext().getParameters().get(propName);
		}
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
				&& (scopeType == ScopeType.CONVERSATION || scopeType == ScopeType.UNSPECIFIED)) {
			Map session = ctx.getSession();
			Map conversation = (Map) session.get(ScopeType.CONVERSATION
					.toString());
			if (conversation != null) {
				obj = conversation.get(propName);
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
		Map session = ctx.getSession();
		switch (scopeType) {
		case REQUEST:
			ActionContext.getContext().getParameters().put(propName, obj);
			break;
		case FLASH:
			Map<String, Object> flash = (Map<String, Object>) session
					.get(ScopeType.FLASH.toString());
			if (flash == null) {
				flash = new HashMap<String, Object>();
			}
			flash.put(propName, obj);
			session.put(ScopeType.FLASH.toString(), flash);
			break;
		case CONVERSATION:
			Map<String, Object> conversation = (Map<String, Object>) session
					.get(ScopeType.CONVERSATION.toString());
			if (conversation == null) {
				conversation = new HashMap<String, Object>();
			}
			conversation.put(propName, obj);
			session.put(ScopeType.CONVERSATION.toString(), conversation);
			break;
		case SESSION:
			ctx.getSession().put(propName, obj);
			break;
		case APPLICATION:
			ctx.getApplication().put(propName, obj);
			break;
		}
	}

	private Method findAnnotatedMethod(Class cls, String methodName) {
		Method methods[] = cls.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)
					&& methods[i].getParameterTypes().length == 0) {
				return methods[i];
			}
		}
		return null;
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
