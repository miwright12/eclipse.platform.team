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

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;

/**
 * Send the contents of the CVS/Notify files to the server
 */
public class NOOPVisitor extends AbstractStructureVisitor {

	public NOOPVisitor(Session session) {
		// Only send non-empty folders
		super(session, false, false);
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor#visitFile(ICVSFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
		sendPendingNotification(file);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		if (folder.isCVSFolder()) {
			folder.acceptChildren(this);
		}
	}

}