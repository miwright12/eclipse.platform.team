/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.awtui.TestRunner;
import junit.framework.TestCase;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * Base-class to the low level-testcases for the Session.
 * Supplies convinience-methods and default attributes for the testcases.
 * Especally data for a default-connection to the server is stored.
 */
public abstract class JUnitTestCase extends TestCase {
	protected static final int RANDOM_CONTENT_SIZE = 10000;
	protected static final boolean NEWLINE_TEST = false;
	protected static final String PLATFORM_NEWLINE = System.getProperty("line.separator");
	protected static final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	
	public static final String[] EMPTY_ARGS = new String[0];

	/**
	 * Init the options and arguments to standard-values
	 */
	public JUnitTestCase(String name) {
		super(name);
	}

	/**
	 * Delete a project/resource form the standard cvs-server
	 */
	protected void magicDeleteRemote(String remoteName) throws CVSException {
		magicDeleteRemote(CVSTestSetup.repository, remoteName);
	}

	/**
	 * Delete a project/resource form the specified cvs-server
	 */
	protected static void magicDeleteRemote(ICVSRepositoryLocation location, String remoteName)
		throws CVSException {
		CVSTestSetup.executeRemoteCommand(location, "rm -rf " + 
			new Path(location.getRootDirectory()).append(remoteName).toString());
	}

	/**
	 * Sends the project to the standard cvs-server so that it contains the resources
	 * described in createResources.  The files have random content.
	 *
	 * @param projectName the name of the project to import
	 * @param createResources e.g. new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"}
	 */
	protected void magicSetUpRepo(String projectName, String[] createResources)
		throws  IOException, CoreException, CVSException {
		magicSetUpRepo(CVSTestSetup.repository, projectName, createResources);
	}
	
	/**
	 * Sends the project to the specified cvs-server so that it contains the resources
	 * described in createResources.  The files have random content.
	 * 
	 * @param location the CVS repository location
	 * @param projectName the name of the project to import
	 * @param createResources e.g. new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"}
	 */
	protected static void magicSetUpRepo(ICVSRepositoryLocation location, String projectName,
		String[] createResources) throws IOException, CoreException, CVSException {
		IProject projectRoot = workspaceRoot.getProject(projectName + "-setup-tmp");
		mkdirs(projectRoot);
		createRandomFile(projectRoot, createResources);
		magicDeleteRemote(location, projectName);
		
		String[] lOptions = new String[]{"-m","msg"};
		String[] args = new String[]{projectName,"a","b"};
	
		EclipseCVSClient.execute(location, CVSWorkspaceRoot.getCVSFolderFor(projectRoot),
			"import", EMPTY_ARGS, lOptions, args);
		projectRoot.delete(false /*force*/, null);
	}
	
	/**
	 *  Compare Arrays and find the first different element
	 */
	protected static void assertEqualsArrays(Object[] obArr1, Object[] obArr2) {
		
		assertEquals("Called assertEqualsArrays with null on one side", obArr1 == null,obArr2 == null);
		if (obArr1 == null) {
			return;
		}

		for (int i=0; i<Math.min(obArr1.length,obArr2.length); i++) {
			assertEquals("At Element " + i + " of the array",obArr1[i],obArr2[i]);
		}
		
		// If the Arrays are different in length, look for the first
		// not existing element and compare it to the existing in the
		// other array
		if (obArr1.length > obArr2.length) {
			assertEquals("Arrays of different length",obArr1[obArr2.length],null);
			return;
		}
	
		if (obArr1.length < obArr2.length) {
			assertEquals("Arrays of different length",obArr2[obArr1.length],null);
			return;
		}
			
	}
	
	/**
	 * Write text lines to file from an array of strings.
	 */
	protected static void writeToFile(IFile file, String[] contents)
		throws IOException, CoreException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream os = new PrintStream(bos);
		try {
			for (int i = 0; i < contents.length; i++) {
				os.println(contents[i]);
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			if (file.exists()) {
				file.setContents(bis, false /*force*/, true /*keepHistory*/, null);
			} else {
				mkdirs(file.getParent());
				file.create(bis, false /*force*/, null);
			}
		} finally {
			os.close();
		}
	}
	
	/**
	 * Read text lines from file into an array of strings.
	 */
	protected static String[] readFromFile(IFile file)
		throws IOException, CoreException {
		if (! file.exists()) return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		List fileContentStore = new ArrayList();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				fileContentStore.add(line);
			}			
		} finally {
			reader.close();
		}
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
	}

	/**
	 * Append text files to file from an array of strings, create new file if it
	 * does not exist yet.
	 */
	protected static void appendToFile(IFile file, String[] contents)
		throws IOException, CoreException {
		String[] oldContents = readFromFile(file);
		String[] newContents;
		if (oldContents == null) {
			newContents = contents;
		} else {
			newContents = new String[oldContents.length + contents.length];
			System.arraycopy(oldContents, 0, newContents, 0, oldContents.length);
			System.arraycopy(contents, 0, newContents, oldContents.length, contents.length);
		}
		writeToFile(file, newContents);
	}
	
	/**
	 * Pre-Append text files to file from an array of strings, create new file if it
	 * does not exist yet.
	 */
	protected static void prefixToFile(IFile file, String[] contents)
		throws IOException, CoreException {
		String[] oldContents = readFromFile(file);
		String[] newContents;
		if (oldContents == null) {
			newContents = contents;
		} else {
			newContents = new String[oldContents.length + contents.length];
			System.arraycopy(contents, 0, newContents, 0, contents.length);
			System.arraycopy(oldContents, 0, newContents, contents.length, oldContents.length);
		}
		writeToFile(file, newContents);
	}
	
	/**
	 * genertates Random content meand to be written in a File
	 */
	protected static String createRandomContent() {
		
		StringBuffer content = new StringBuffer();
		int contentSize;
		
		content.append("Random file generated for test" + PLATFORM_NEWLINE);
		
		contentSize = (int) Math.round(RANDOM_CONTENT_SIZE * 2 * Math.random());
		for (int i=0; i<contentSize; i++) {
			
			if (Math.random()>0.99) {
				content.append(PLATFORM_NEWLINE);
			}

			if (Math.random()>0.99) {
				content.append("\n");
			}
			
			if (NEWLINE_TEST) {
				if (Math.random()>0.99) {
					content.append("\n\r");
				}
				if (Math.random()>0.99) {
					content.append('\r');
				}
				if (Math.random()>0.99) {
					content.append("\r\n");
				}
				if (Math.random()>0.99) {
					content.append("\n");
				}
				if (Math.random()>0.99) {
					content.append("\n\n");
				}
				if (Math.random()>0.99) {
					content.append("\r\r");
				}
			}
			
			content.append((char)('\u0021' + Math.round(60 * Math.random())));
		}
		
		return content.toString();
	}
	
	/**
	 * Creates a folder (and its parents if needed).
	 */
	protected static void mkdirs(IContainer container) throws CoreException {
		if (container.getType() == IResource.PROJECT) {
			IProject project = (IProject) container;
			if (! project.exists()) {
				project.create(null);
			}
			project.open(null);
		} else if (container.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) container;
			if (! folder.exists()) {
				mkdirs(folder.getParent());
				folder.create(false /*force*/, true /*local*/, null);
			}
		}
	}

	/**
	 * Creates the file with random content, and all the folders on the
	 * way to there.
	 */
	private static void createRandomFile(IFile file)
		throws IOException, CoreException {
		mkdirs(file.getParent());
		writeToFile(file, new String[] { createRandomContent() });
	}
		
	/**
	 * Build the given fileStructure, all files are going to have
	 * sample content, all folders on the way are created.
	 */
	protected static void createRandomFile(IContainer parent, String[] fileNameArray) 
		throws IOException, CoreException {
		for (int i = 0; i < fileNameArray.length; i++) {
			IFile file = parent.getFile(new Path(fileNameArray[i]));
			createRandomFile(file);
		}
	}

	/**
	 * wait milliseconds to continou the execution
	 */
	public static void waitMsec(int msec) {	
		try {
			int wait = CVSTestSetup.WAIT_FACTOR * msec;
			long start = System.currentTimeMillis();
			Thread.sleep(wait);
			long end = System.currentTimeMillis();
			// Allow a 100 ms error in waiting
			assertTrue("Error in thread class. Did not wait long enough", (end - start) > (wait - 100));
		} catch(InterruptedException e) {
			fail("wait-problem");
		}
	}
	
	/**
	 * Call this method from the main-method of your test-case.
	 * It initialises some required parameter and runs the testcase.
	 */
	protected static void run(Class test) {
		// XXX is this property used anywhere?
		System.setProperty("eclipse.cvs.standalone", "true");
		TestRunner.run(test);
	}
}
