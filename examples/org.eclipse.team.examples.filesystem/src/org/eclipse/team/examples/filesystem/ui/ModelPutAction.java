/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelParticipantAction;

/**
 * A put action for use in the file system synchronize participant.
 */
public class ModelPutAction extends ModelParticipantAction {

	public ModelPutAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		// Only enable the put in outgoing or both modes
		int mode = getConfiguration().getMode();
		if (mode == ISynchronizePageConfiguration.OUTGOING_MODE || mode == ISynchronizePageConfiguration.BOTH_MODE) {
			return getResourceMappings(selection).length > 0;
		}
		return false;
	}

	private ResourceMapping[] getResourceMappings(IStructuredSelection selection) {
		List mappings = new ArrayList();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			ResourceMapping mapping = Utils.getResourceMapping(element);
			if (mapping != null)
				mappings.add(mapping);
		}
		return (ResourceMapping[]) mappings.toArray(new ResourceMapping[mappings.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ResourceMapping[] resourceMappings = getResourceMappings(getStructuredSelection());
		SubscriberScopeManager manager = FileSystemOperation.createScopeManager("Put", resourceMappings);
		try {
			new PutOperation(getConfiguration().getSite().getPart(), manager).run();
		} catch (InvocationTargetException e) {
			IStatus status = getStatus(e);
			ErrorDialog.openError(getConfiguration().getSite().getShell(), null, null, status);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	private IStatus getStatus(Throwable throwable) {
		if (throwable instanceof InvocationTargetException) {
			return getStatus(((InvocationTargetException) throwable).getCause());
		}
		return new Status(IStatus.ERROR, FileSystemPlugin.ID, 0, "An error occurred during the put.", throwable);
	}
}
