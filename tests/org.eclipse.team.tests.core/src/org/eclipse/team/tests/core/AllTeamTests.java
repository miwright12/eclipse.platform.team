/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.tests.resources.ResourceTest;

public class AllTeamTests extends ResourceTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTeamTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTeamTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(RepositoryProviderTests.suite());
		suite.addTest(StreamTests.suite());
		suite.addTest(StorageMergerTests.suite());
		suite.addTest(org.eclipse.team.tests.core.regression.AllTests.suite());
		return suite;
	}
}

