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
package org.eclipse.team.examples.pessimistic.ui;
 
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProviderPlugin;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard which adds the <code>PessimisticFilesystemProvider</code> nature
 * to a given project.
 */
public class ConfigurationWizard extends Wizard implements IConfigurationWizard {
	/*
	 * The project in question.
	 */
	private IProject project;
	
	/*
	 * @see Wizard#addPages()
	 */
	public void addPages() {
		// workaround the wizard problem
		addPage(new BlankPage());
	}

	/*
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			RepositoryProvider.map(project, PessimisticFilesystemProviderPlugin.NATURE_ID);
		} catch (TeamException e) {
			PessimisticFilesystemProviderPlugin.getInstance().logError(e, "Could not set sharing on " + project);
			return false;
		}
		return true;
	}

	/*
	 * @see IConfigurationWizard#init(IWorkbench, IProject)
	 */	
	public void init(IWorkbench workbench, IProject project) {
		this.project = project;
	}
}
