package com.simplenote.android;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SimpleNoteTest extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(SimpleNoteTest.class).includeAllPackagesUnderHere().build();
	}

}
