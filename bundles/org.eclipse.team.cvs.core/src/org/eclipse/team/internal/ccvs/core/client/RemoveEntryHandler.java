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
package org.eclipse.team.internal.ccvs.core.client;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Handles a "Remove-entry" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Remove-entry ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * It removes the file from both the entries of the parent-folder. 
 * This happen, when the folder has allready been removed locally
 * what happens on a checkin that includes a removed file.
 */
class RemoveEntryHandler extends ResponseHandler {
	public String getResponseID() {
		return "Remove-entry"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();

		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);
		if (mFile.exists()) {
			CVSProviderPlugin.log(new CVSException(Policy.bind("RemoveEntryHandler.2", mFile.getRepositoryRelativePath()))); //$NON-NLS-1$
		} else {
			mFile.unmanage(null);
		}
	}
}
