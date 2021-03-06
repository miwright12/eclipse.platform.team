/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;
import org.eclipse.team.internal.ccvs.ui.tags.*;
import org.eclipse.ui.IWorkbenchPart;

public class UpdateWizard extends ResizableWizard {

	private ResourceMapping[] mappers;
	private final IWorkbenchPart part;
	private TagSelectionWizardPage tagSelectionPage;
	
	public UpdateWizard(IWorkbenchPart part, ResourceMapping[] mappers) {
        super("UpdateWizard", CVSUIPlugin.getPlugin().getDialogSettings()); //$NON-NLS-1$
		this.part = part;
		this.mappers = mappers;
		setWindowTitle(CVSUIMessages.UpdateWizard_title); 
	}
	
    public static void run(IWorkbenchPart part, ResourceMapping[] mappers) {
        final UpdateWizard wizard = new UpdateWizard(part, mappers);
        open(part.getSite().getShell(), wizard);
    }
    
	public void addPages() {
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_CHECKOUT);
        tagSelectionPage = new TagSelectionWizardPage("tagPage", CVSUIMessages.UpdateWizard_0, substImage, CVSUIMessages.UpdateWizard_1, TagSource.create(mappers), TagSourceWorkbenchAdapter.INCLUDE_ALL_TAGS); //$NON-NLS-1$  
		tagSelectionPage.setAllowNoTag(true);
		tagSelectionPage.setHelpContxtId(IHelpContextIds.UPDATE_TAG_SELETION_PAGE);
		CVSTag tag = getInitialSelection();
		if (tag != null) {
			tagSelectionPage.setSelection(tag);
		}
		addPage(tagSelectionPage);
	}
	
	/**
     * @return
     */
    private CVSTag getInitialSelection() {
        try {
            for (int i = 0; i < mappers.length; i++) {
                ResourceMapping mapper = mappers[i];
                IProject[] projects = mapper.getProjects();
                for (int k = 0; k < projects.length; k++) {
                    IProject project = projects[k];
                    ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
                    FolderSyncInfo info = folder.getFolderSyncInfo();
                    if (info != null) {
                        return info.getTag();
                    }
                }
            }
        } catch (CoreException e) {
            CVSUIPlugin.log(e);
        }
        return null;
    }

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			new UpdateOperation(part, mappers, Command.NO_LOCAL_OPTIONS, tagSelectionPage.getSelectedTag()).run();
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getShell(), null, null, e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		return super.performFinish();
	}
}
