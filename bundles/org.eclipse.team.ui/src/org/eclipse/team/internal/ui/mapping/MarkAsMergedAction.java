/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.delta.IDelta;
import org.eclipse.team.core.delta.IThreeWayDelta;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.operations.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IContributorResourceAdapter;

/**
 * Action contributed by the {@link ModelSynchronizeParticipant} that 
 * will mark a file as merged.
 */
public class MarkAsMergedAction extends ModelProviderAction {

	public MarkAsMergedAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
		Utils.initAction(this, "action.markAsMerged."); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		final IMergeContext context = (IMergeContext)((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
		try {
			new ModelProviderOperation(getConfiguration()) {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					try {
						IDelta[] deltas = getSelectedDeltas(getStructuredSelection());
						for (int i = 0; i < deltas.length; i++) {
							IDelta delta = deltas[i];
							// TODO: mark as merged should support batching
							IStatus status = context.markAsMerged((IFile)context.getResource(delta), false, monitor);
							if (!status.isOK())
								throw new CoreException(status);
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			
			}.run();
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		return getSelectedDeltas(selection).length > 0;
	}

	protected IDelta[] getSelectedDeltas(IStructuredSelection selection) {
		// TODO: for now, just enable for files
		if (selection.size() == 1) {
			Object o = selection.getFirstElement();
			IResource resource = null;
			if (o instanceof IResource) {
				resource = (IResource) o;
			} else if (o instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) o;
				resource = (IResource)adaptable.getAdapter(IResource.class);
				if (resource == null) {
					IContributorResourceAdapter adapter = (IContributorResourceAdapter)adaptable.getAdapter(IContributorResourceAdapter.class);
					if (adapter != null)
						resource = adapter.getAdaptedResource(adaptable);
				}
			}
			if (resource != null && resource.getType() == IResource.FILE) {
				ISynchronizationContext context = getContext();
				IDelta delta = context.getDeltaTree().getDelta(resource.getFullPath());
				if (delta instanceof IThreeWayDelta) {
					IThreeWayDelta twd = (IThreeWayDelta) delta;
					if (twd.getDirection() == IThreeWayDelta.CONFLICTING) {
						return new IDelta[] { delta };
					}
				}
			}
		}
		return new IDelta[0];
	}
	
	private ISynchronizationContext getContext() {
		return ((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
	}

}