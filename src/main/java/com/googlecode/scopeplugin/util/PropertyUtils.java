package com.googlecode.scopeplugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PropertyUtils {

	private static final long serialVersionUID = -4590322556118858869L;

	private static final Log LOG = LogFactory.getLog(PropertyUtils.class);

	public static String determinePropertyName(String methodName, boolean out) {
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

	public static Object getProperty(Object action, Method m)
			throws IllegalAccessException, InvocationTargetException {
		Object obj = m.invoke(action);
		return obj;
	}

	public static Object getProperty(Object action, Field f)
			throws IllegalAccessException {
		boolean accessible = f.isAccessible();
		f.setAccessible(true);
		Object obj = f.get(action);
		f.setAccessible(accessible);
		return obj;
	}

	public static void setProperty(Object action, Method m, Object obj)
			throws IllegalAccessException, InvocationTargetException {
		Class<?> paramTypes[] = m.getParameterTypes();
		if (obj != null && paramTypes.length > 0
				&& paramTypes[0].isAssignableFrom(obj.getClass())) {
			m.invoke(action, obj);
		}
	}

	public static void setProperty(Object action, Field f, Object obj)
			throws IllegalAccessException {
		Class<?> fieldType = f.getType();
		if (obj != null && fieldType.isAssignableFrom(obj.getClass())) {
			boolean accessible = f.isAccessible();
			f.setAccessible(true);
			f.set(action, obj);
			f.setAccessible(accessible);
		}
	}
}
