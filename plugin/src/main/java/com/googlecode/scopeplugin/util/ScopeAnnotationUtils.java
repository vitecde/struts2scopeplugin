package com.googlecode.scopeplugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;
import com.opensymphony.xwork2.util.AnnotationUtils;

public abstract class ScopeAnnotationUtils {

	private static final long serialVersionUID = -4590322556118858869L;

	private static final Log LOG = LogFactory.getLog(ScopeAnnotationUtils.class);
	private static final Map<Class<?>, CachedMethods> cachedMethods = Collections
			.synchronizedMap(new HashMap<Class<?>, CachedMethods>());

	public static Method findAnnotatedMethod(Class<?> cls, String methodName) {
		Method methods[] = cls.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)
					&& methods[i].getParameterTypes().length == 0) {
				return methods[i];
			}
		}
		return null;
	}

	public static List<Field> findAnnotatedFields(Class<?> cls, boolean out) {
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

	public static List<Method> findAnnotatedMethods(Class<?> cls, boolean out) {
		CachedMethods cache = cachedMethods.get(cls);
		if (cache == null) {
			cache = new CachedMethods();
			cachedMethods.put(cls, cache);
		}
		Collection<Method> methods = null;
		if (out) {
			methods = cache.getOutMethods();
			if (methods == null) {				
				methods = new ArrayList<>(); 
				AnnotationUtils.addAllMethods(Out.class, cls, (List<Method>)methods);
				cache.setOutMethods(methods);
			}
		} else {
			methods = cache.getInMethods();
			if (methods == null) {
				methods = new ArrayList<>(); 
				AnnotationUtils.addAllMethods(In.class, cls, (List<Method>)methods);
				cache.setInMethods(methods);
			}
		}
		return new LinkedList<Method>(methods);
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
		public void setInMethods(Collection<Method> inMethods) {
			this.inMethods = new LinkedList<Method>(inMethods);
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
		public void setOutMethods(Collection<Method> outMethods) {
			this.outMethods = new LinkedList<Method>(outMethods);
		}
	}
}
