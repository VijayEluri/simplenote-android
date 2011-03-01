package com.bryanjswift.swiftnote;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class SwiftNoteTest extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(com.bryanjswift.swiftnote.SwiftNoteTest.class).includeAllPackagesUnderHere().build();
	}

}
