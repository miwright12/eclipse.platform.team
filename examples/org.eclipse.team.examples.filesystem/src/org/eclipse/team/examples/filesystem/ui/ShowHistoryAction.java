/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

public class ShowHistoryAction extends ActionDelegate implements IObjectActionDelegate {

	IStructuredSelection fSelection;

	public void run(IAction action) {
		final Shell shell = Display.getDefault().getActiveShell();
		try {
			new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					final IResource resource = (IResource) fSelection.getFirstElement();
					Runnable r = new Runnable() {
						public void run() {
							TeamUI.showHistoryFor(TeamUIPlugin.getActivePage(), resource, null);
						}
					};

					FileSystemPlugin.getStandardDisplay().asyncExec(r);
				}
			});
		} catch (InvocationTargetException exception) {
			// ignore
		} catch (InterruptedException exception) {
			// ignore
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection) sel;
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// do nothing
	}
}
