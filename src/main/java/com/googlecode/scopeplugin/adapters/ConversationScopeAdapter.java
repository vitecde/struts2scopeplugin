package com.googlecode.scopeplugin.adapters;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.scopeplugin.ScopeType;
import com.opensymphony.xwork2.ActionContext;

public class ConversationScopeAdapter extends AbstractScopeAdapter {
	final Map<String, Object> session;

	public ConversationScopeAdapter(ActionContext context, String name) {
		super(context, ScopeType.CONVERSATION, name);
		session = context.getSession();
	}

	public Object findObject() {
		Object obj = null;
		Map<String, Object> conversation = (Map<String, Object>) session
				.get(ScopeType.CONVERSATION.toString());
		if (conversation != null) {
			obj = conversation.get(getName());
		}
		return obj;
	}

	public void putObject(Object obj) {
		Map<String, Object> conversation = (Map<String, Object>) session
				.get(ScopeType.CONVERSATION.toString());
		if (conversation == null) {
			conversation = new HashMap<String, Object>();
		}
		conversation.put(getName(), obj);
		session.put(ScopeType.CONVERSATION.toString(), conversation);
	}

	public Object removeObject() {
		Object obj = null;
		Map<String, Object> conversation = (Map<String, Object>) session
				.get(ScopeType.CONVERSATION.toString());
		if (conversation != null) {
			obj = conversation.remove(getName());
		}
		return obj;
	}
}
