package com.googlecode.scopeplugin;

import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;

public class FakeFieldAction {
	@In (scope=ScopeType.SESSION)
	@Out (scope=ScopeType.SESSION)
	private String testName;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}
}
