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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.ui.synchronize.subscribers.DirectionFilterActionGroup;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberPageConfiguration;
import org.eclipse.ui.IActionBars;

public class WorkspaceSynchronizeAdvisor extends CVSSynchronizeViewerAdvisor {

	private DirectionFilterActionGroup modes;
	private ActionDelegateWrapper commitToolbar;
	private ActionDelegateWrapper updateToolbar;
	
	public WorkspaceSynchronizeAdvisor(SubscriberPageConfiguration configuration, SyncInfoTree syncInfoTree) {
		super(configuration, syncInfoTree);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor#initializeActions(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeActions(StructuredViewer treeViewer) {
		super.initializeActions(treeViewer);
		
		SubscriberPageConfiguration configuration = getConfiguration();
		configuration.setSupportedModes(SubscriberPageConfiguration.ALL_MODES);
		modes = new DirectionFilterActionGroup(configuration);

		commitToolbar = new ActionDelegateWrapper(new SubscriberCommitAction(), getSynchronizeView());
		WorkspaceUpdateAction action = new WorkspaceUpdateAction();
		action.setPromptBeforeUpdate(true);
		updateToolbar = new ActionDelegateWrapper(action, getSynchronizeView());

		Utils.initAction(commitToolbar, "action.SynchronizeViewCommit.", Policy.getBundle()); //$NON-NLS-1$
		Utils.initAction(updateToolbar, "action.SynchronizeViewUpdate.", Policy.getBundle()); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		IToolBarManager toolbar = actionBars.getToolBarManager();
		if (toolbar != null) {
			modes.fillToolBar(toolbar);
			toolbar.add(new Separator());
			toolbar.add(updateToolbar);
			toolbar.add(commitToolbar);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSynchronizeViewerAdvisor#getActionDelegates()
	 */
	protected ActionDelegateWrapper[] getActionDelegates() {
		// Returned so that the superclass will forward model changes
		return new ActionDelegateWrapper[]  { commitToolbar, updateToolbar };
	}
}