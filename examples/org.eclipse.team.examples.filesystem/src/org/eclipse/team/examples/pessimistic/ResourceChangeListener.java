/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.team.examples.pessimistic;
 
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * The <code>ResourceChangeListener</code> listens for resource changes 
 * and (optionally) prompts the user to add the new resources to the 
 * control of the repository provider.
 */
public class ResourceChangeListener implements IResourceDeltaVisitor, IResourceChangeListener {
	/*
	 * Set of added resources
	 */
	private Set fAdded;
	/*
	 * Set of removed resources
	 */
	private Set fRemoved;
	
	public ResourceChangeListener() {
		fAdded= new HashSet(1);
		fRemoved= new HashSet(1);
	}

	/**
	 * Looks for the following changes:
	 * <ul>
	 *   <li>Resources that are controlled and are removed</li>
	 *   <li>Resources that are added under a managed project</li>
	 * </ul>
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource= delta.getResource();
		if (resource != null) {
			IProject project= resource.getProject();
			if (project != null) {
				PessimisticFilesystemProvider provider= (PessimisticFilesystemProvider)RepositoryProvider.getProvider(project, PessimisticFilesystemProviderPlugin.NATURE_ID);
				if (provider == null)
					return false;
				if (provider.isControlled(resource)) {
					switch (delta.getKind()) {
						case IResourceDelta.CHANGED:
						case IResourceDelta.ADDED:
							return true;
						case IResourceDelta.REMOVED:
							fRemoved.add(resource);
							return false;						
					}
				} else {
					switch (delta.getKind()) {
						case IResourceDelta.CHANGED:
						case IResourceDelta.REMOVED:
							return true;
						case IResourceDelta.ADDED:
							// don't prompt for ignored resources
							if (!provider.isIgnored(resource)) {
								fAdded.add(resource);
							}
							return true;						
					}				
				}
			} else {
				return true;
			}
		}					
		return false;
	}

	/*
	 * Convenience method to return a resource array from a collection
	 */
	private IResource[] toResourceArray(Collection collection) {
		if (collection.isEmpty()) {
			return new IResource[0];
		}
		IResource[] resources= new IResource[collection.size()];
		collection.toArray(resources);
		return resources;
	}

	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged (IResourceChangeEvent event) {
		try {
			event.getDelta().accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
			PessimisticFilesystemProviderPlugin.getInstance().logError(e, "Exceptions during resource callback");
		}

		if (!fRemoved.isEmpty() || !fAdded.isEmpty()) {
			final IWorkspaceRunnable workspaceRunnable= new IWorkspaceRunnable() {
				public void run(final IProgressMonitor monitor) throws CoreException {
					if (!fRemoved.isEmpty()) {
						remove(monitor);
					}
					
					if (!fAdded.isEmpty()) {
						add(monitor);
					}					
				}
			};
			// must fork since we are in resource callback.
			Runnable run= new Runnable() {
				public void run() {
					try {
						IWorkspace workspace= ResourcesPlugin.getWorkspace();
						if (workspace != null) {
							workspace.run(workspaceRunnable, null);
						}
					} catch (CoreException e) {
						PessimisticFilesystemProviderPlugin.getInstance().logError(e, "Problems encountered during attempt to add/remove control.");
					}
				}
			};
			new Thread(run).start();
		}
	}

	/*
	 * Convenience method to get the preference for what to do
	 * when new resource have been detected.
	 */
	private int getAddToControlPreference() {
		Preferences preferences= PessimisticFilesystemProviderPlugin.getInstance().getPluginPreferences();
		return preferences.getInt(IPessimisticFilesystemConstants.PREF_ADD_TO_CONTROL);
	}
	
	/*
	 * Adds the resources to the control of the provider.
	 * If the add to control preference is:
	 *	do nothing - does not add
	 *  automatic - adds all resources
	 *  prompt - brings up a prompt which requests that the user
	 * 				select which resources to add
	 */
	private void add(final IProgressMonitor monitor) {
		switch (getAddToControlPreference()) {
			case IPessimisticFilesystemConstants.OPTION_DO_NOTHING:
				break;
			case IPessimisticFilesystemConstants.OPTION_AUTOMATIC:
				addToControl(fAdded, monitor);
				break;
			case IPessimisticFilesystemConstants.OPTION_PROMPT:
				final Shell shell= getShell();
				if (shell != null && !shell.isDisposed()) {
					final Set resources= new HashSet(fAdded);
					Runnable run= new Runnable() {
						public void run() {
							CheckedTreeSelectionDialog dialog= new CheckedTreeSelectionDialog(shell, new WorkbenchLabelProvider(), new ResourceSetContentProvider(resources));
							dialog.setMessage("Select the resources to be added to the control of the repository.");
							dialog.setTitle("Add resources to control");
							dialog.setContainerMode(true);
							dialog.setBlockOnOpen(true);
							dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
							Object[] resourceArray= resources.toArray();
							dialog.setExpandedElements(resourceArray);
							dialog.setInitialSelections(resourceArray);
							dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
							int status= dialog.open();
							
							if (status == Window.OK) {
								Object[] results= dialog.getResult();
								if (results != null) {
									Set resources= new HashSet(results.length);
									for (int i= 0; i < results.length; i++) {
										resources.add(results[i]);
									}
									addToControl(resources, monitor);
								}
							}
						}
					};
			
					Display display= shell.getDisplay();
					display.asyncExec(run);
				} else {
					PessimisticFilesystemProviderPlugin.getInstance().logError(null, "Could not aquire a shell");
				}
				break;
		}
		fAdded.clear();
	}

	/*
	 * Adds the resources to the control of the provider.
	 */
	private void addToControl(Collection resources, final IProgressMonitor monitor) {
		Map byProject= sortByProject(resources);
		for (Iterator i= byProject.keySet().iterator(); i.hasNext();) {
			IProject project= (IProject) i.next();
			PessimisticFilesystemProvider provider= (PessimisticFilesystemProvider)RepositoryProvider.getProvider(project, PessimisticFilesystemProviderPlugin.NATURE_ID);
			if (provider != null) {
				provider.addToControl(toResourceArray((Collection)byProject.get(project)), monitor);
			}
			
		}
	}
	
	/*
	 * Removes the resources from the control of the provider.
	 */
	private void remove(IProgressMonitor monitor) {
		Map byProject= sortByProject(fRemoved);
		for (Iterator i= byProject.keySet().iterator(); i.hasNext();) {
			IProject project= (IProject) i.next();
			PessimisticFilesystemProvider provider= (PessimisticFilesystemProvider)RepositoryProvider.getProvider(project, PessimisticFilesystemProviderPlugin.NATURE_ID);
			if (provider != null) {
				provider.removeFromControl(toResourceArray((Collection)byProject.get(project)), monitor);
			}
		}
		fRemoved.clear();
	}

	/*
	 * Convenience method to sort the resources by project
	 */
	private Map sortByProject(Collection resources) {
		Map byProject= new HashMap();
		for (Iterator i= resources.iterator(); i.hasNext();) {
			IResource resource= (IResource) i.next();
			IProject project= resource.getProject();
			Set set= (Set)byProject.get(project);
			if (set == null) {
				set= new HashSet(1);
				byProject.put(project, set);
			}
			set.add(resource);
		}
		return byProject;
	}

	/*
	 * Convenience method which answers a shell with which to prompt.
	 */
	private Shell getShell() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
			if (window == null) {
				IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
				if (windows != null && windows.length > 0) {
					window= windows[0];
				}
			}
			if (window != null) {
				Shell shell= window.getShell();
				if (shell == null)
					return null;
				if (shell.isDisposed())
					return null;
				return shell;
			}
		}
		return null;
	}

	/**
	 * Starts listening for changes.
	 */
	public void startup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		if (PessimisticFilesystemProviderPlugin.getInstance().isDebugging())
			System.out.println ("Resource callback registered");
	}
	
	/**
	 * Stops listening for changes.
	 */
	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (PessimisticFilesystemProviderPlugin.getInstance().isDebugging())
			System.out.println ("Resource callback unregistered");	
	}
}