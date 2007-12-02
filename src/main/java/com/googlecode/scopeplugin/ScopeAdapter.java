package com.googlecode.scopeplugin;

public interface ScopeAdapter {
	public void putObject(Object obj);
	public Object findObject();
	public Object removeObject();
}