package com.googlecode.scopeplugin;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;

public class FakeMethodAction {
	private String testName;

	@Out (required=false, scope=ScopeType.SESSION)
	public String getTestName() {
		return testName;
	}

	@In (required=false, scope=ScopeType.SESSION)
	public void setTestName(String testName) {
		this.testName = testName;
	}
}
