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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.*;
import org.eclipse.team.ui.synchronize.subscribers.SynchronizeViewerAdvisor;

public class CVSSynchronizeViewerAdvisor extends SynchronizeViewerAdvisor implements ISynchronizeModelChangeListener, IPropertyChangeListener {

	private boolean isGroupIncomingByComment = false;

	private static class CVSLabelDecorator extends LabelProvider implements ILabelDecorator  {
		public String decorateText(String input, Object element) {
			String text = input;
			if (element instanceof ISynchronizeModelElement) {
				IResource resource =  ((ISynchronizeModelElement)element).getResource();
				if(resource != null && resource.getType() != IResource.ROOT) {
					CVSLightweightDecorator.Decoration decoration = new CVSLightweightDecorator.Decoration();
					CVSLightweightDecorator.decorateTextLabel(resource, decoration, false, true);
					StringBuffer output = new StringBuffer(25);
					if(decoration.prefix != null) {
						output.append(decoration.prefix);
					}
					output.append(text);
					if(decoration.suffix != null) {
						output.append(decoration.suffix);
					}
					return output.toString();
				}
			}
			return text;
		}
		public Image decorateImage(Image base, Object element) {
			return base;
		}
	}
	
	public CVSSynchronizeViewerAdvisor(SubscriberPageConfiguration configuration, SyncInfoTree syncInfoTree) {
		super(configuration, syncInfoTree);
		
		// Sync changes are used to update the action state for the update/commit buttons.
		addInputChangedListener(this);
		
		// Listen for decorator changed to refresh the viewer's labels.
		CVSUIPlugin.addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#getLabelProvider()
	 */
	protected ILabelProvider getLabelProvider() {
		ILabelProvider oldProvider = super.getLabelProvider();
		return new DecoratingColorLabelProvider(oldProvider, new CVSLabelDecorator());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.TreeViewerAdvisor#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if(property.equals(CVSUIPlugin.P_DECORATORS_CHANGED) && getViewer() != null && getSyncInfoSet() != null) {
			getViewer().refresh(true /* update labels */);
		}
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		removeInputChangedListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeModelChangeListener#modelChanged(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
	 */
	public void modelChanged(ISynchronizeModelElement root) {
		ActionDelegateWrapper[] actions = getActionDelegates();
		for (int i = 0; i < actions.length; i++) {
			ActionDelegateWrapper wrapper = actions[i];
			wrapper.setSelection(root);
		}
	}

	/**
	 * Return the non-null list of action delegates whose selection must
	 * be updated when the model changes. 
	 * By default, an empty list is returned.
	 * @return the array of action delegates
	 */
	protected ActionDelegateWrapper[] getActionDelegates() {
		return new ActionDelegateWrapper[0];
	}
}
