package com.googlecode.scopeplugin;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;

public class FakeAction {
	@In (required=false, scope=ScopeType.SESSION)
	@Out (required=false, scope=ScopeType.SESSION)
	private String testName;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}
}
