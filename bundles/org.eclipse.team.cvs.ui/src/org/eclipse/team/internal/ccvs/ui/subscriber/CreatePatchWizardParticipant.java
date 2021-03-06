/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

public class CreatePatchWizardParticipant extends WorkspaceSynchronizeParticipant {
	 
    final GenerateDiffFileWizard fWizard;
    
    public CreatePatchWizardParticipant(ISynchronizeScope scope, GenerateDiffFileWizard wizard) {
        super(scope);
        fWizard= wizard;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    protected void initializeConfiguration( ISynchronizePageConfiguration configuration) {
        super.initializeConfiguration(configuration);
        configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ISynchronizePageConfiguration.LAYOUT_GROUP});
        configuration.setProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU, ISynchronizePageConfiguration.DEFAULT_CONTEXT_MENU);
        ((SynchronizePageConfiguration)configuration).setViewerStyle(SynchronizePageConfiguration.CHECKBOX);
        configuration.setSupportedModes(ISynchronizePageConfiguration.OUTGOING_MODE);
        configuration.setMode(ISynchronizePageConfiguration.OUTGOING_MODE);
    }
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#doesSupportSynchronize()
     */
    public boolean doesSupportSynchronize() {
        return false;
    }
}
