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
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

public interface IRepositoryListener {
	public void repositoryAdded(ICVSRepositoryLocation root);
	public void repositoryRemoved(ICVSRepositoryLocation root);
	public void repositoriesChanged(ICVSRepositoryLocation[] roots);
}
