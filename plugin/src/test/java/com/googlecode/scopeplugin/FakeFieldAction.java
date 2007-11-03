package com.googlecode.scopeplugin;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;
import com.opensymphony.xwork2.Action;

public class FakeFieldAction implements Action {
	@In (scope=ScopeType.SESSION)
	@Out (scope=ScopeType.SESSION)
	private String testName;

	public String execute() {
		return SUCCESS;
	}
	
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}
}
