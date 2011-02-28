package com.bryanjswift.swiftnote;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class SimpleNoteTest extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(SimpleNoteTest.class).includeAllPackagesUnderHere().build();
	}

}
