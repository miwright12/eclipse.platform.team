/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.team.examples.pessimistic;
 
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plugin for the <code>PessimisticFilesystemProvider</code>.
 */
public class PessimisticFilesystemProviderPlugin extends AbstractUIPlugin {
	/*
	 * Singleton instance.
	 */
	private static PessimisticFilesystemProviderPlugin instance;
	/*
	 * The resource change listener which notifies the provider of 
	 * added and deleted files.
	 */
	private ResourceChangeListener fListener;
	/*
	 * The provider listeners
	 */
	private List fListeners;

	/**
	 * The plugin identifier
	 */
	public static final String PLUGIN_ID = "org.eclipse.team.examples.pessimistic";
	/**
	 * The nature identifier.
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".pessimisticnature";

	/**
	 * Contstructor required by plugin lifecycle.
	 */
	public PessimisticFilesystemProviderPlugin(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		instance = this;
		fListeners= new ArrayList(1);
		//setDebugging(true);
	}

	/**
	 * Answers the singleton instance of this plugin.
	 */	
	public static PessimisticFilesystemProviderPlugin getInstance() {
		return instance;
	}

	/**
	 * Initializes the default preferences for this plugin.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(
			IPessimisticFilesystemConstants.PREF_CHECKED_IN_FILES_EDITED,
			IPessimisticFilesystemConstants.OPTION_PROMPT);
		store.setDefault(
			IPessimisticFilesystemConstants.PREF_CHECKED_IN_FILES_EDITED_NOPROMPT,
			IPessimisticFilesystemConstants.OPTION_AUTOMATIC);
		store.setDefault(
			IPessimisticFilesystemConstants.PREF_CHECKED_IN_FILES_SAVED,
			IPessimisticFilesystemConstants.OPTION_DO_NOTHING);
		store.setDefault(
			IPessimisticFilesystemConstants.PREF_ADD_TO_CONTROL,
			IPessimisticFilesystemConstants.OPTION_PROMPT);			
		store.setDefault(IPessimisticFilesystemConstants.PREF_FAIL_VALIDATE_EDIT, false);
		store.setDefault(IPessimisticFilesystemConstants.PREF_TOUCH_DURING_VALIDATE_EDIT, true);
	}
	
	/**
	 * Convenience method for logging errors.
	 */
	public void logError(Throwable exception, String message) {
		String pluginId= getDescriptor().getUniqueIdentifier();
		Status status= new Status(Status.ERROR, pluginId, Status.OK, message, exception);
		getLog().log(status);
		if (isDebugging()) {
			System.out.println(message);
			exception.printStackTrace();
		}			
	}

	/**
	 * Starts the resource listener.
	 * 
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		fListener= new ResourceChangeListener();
		fListener.startup();
		super.startup();
	}

	/**
	 * Stops the resource listener.
	 * 
	 * @see Plugin#startup()
	 */
	public void shutdown() throws CoreException {
		fListener.shutdown();
		fListener= null;
		super.shutdown();
	}
	
	/**
	 * Notifies the registered <code>IResourceStateListener</code> objects
	 * that the repository state for the resources has changed.
	 * 
	 * @param resources	Collection of resources that have changed.
	 */
	public void fireResourcesChanged(IResource[] resources) {
		if (resources == null || resources.length == 0 || fListeners.isEmpty())
			return;
		for (Iterator i= fListeners.iterator(); i.hasNext();) {
			IResourceStateListener listener= (IResourceStateListener) i.next();
			listener.stateChanged(resources);
		}
	}
	
	/**
	 * Adds the listener to the list of listeners that are notified when
	 * the repository state of resources change.
	 * 
	 * @param listener
	 */
	public void addProviderListener(IResourceStateListener listener) {
		if (fListeners.contains(listener))
			return;
		fListeners.add(listener);
	}
	
	
	/**
	 * Removes the listener from the list of listeners that are notified when
	 * the repository state of resources change.
	 * 
	 * @param listener
	 */
	public void removeProviderListener(IResourceStateListener listener) {
		fListeners.remove(listener);
	}
}