/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.core.Policy;

/**
 * This collector maintains a {@link SyncInfoSet} for a particular team subscriber keeping
 * it up-to-date with both incoming changes and outgoing changes as they occur for 
 * resources in the workspace. The collector can be configured to consider all the subscriber's
 * roots or only a subset.
 * <p>
 * The advantage of this collector is that it processes both resource and team
 * subscriber deltas in a background thread.
 * </p>
 * @since 3.0
 */
public final class WorkingSetFilteredSyncInfoCollector {

	private WorkingSetSyncSetInput workingSetInput;
	private SyncSetInputFromSyncSet filteredInput;
	private SubscriberEventHandler eventHandler;
	private IResource[] roots;
	
	/**
	 * Create a collector that collects out-of-sync resources that are children of
	 * the given roots. If the roots are <code>null</code>, then all out-of-sync resources
	 * from the subscriber are collected. An empty array of roots will cause no resources
	 * to be collected. The <code>start()</code> method must be called after creation
	 * to rpime the collector's sync sets.
	 * @param subscriber the Subscriber
	 * @param roots the roots of the out-of-sync resources to be collected
	 */
	public WorkingSetFilteredSyncInfoCollector(SubscriberSyncInfoCollector collector, IResource[] roots) {
		this.roots = roots;
		this.eventHandler = collector.getEventHandler();	
		// TODO: optimize and don't use working set if no roots are passed in
		workingSetInput = new WorkingSetSyncSetInput((SubscriberSyncInfoSet)collector.getSyncInfoSet(), getEventHandler());
		filteredInput = new SyncSetInputFromSyncSet(workingSetInput.getSyncSet(), getEventHandler());
		filteredInput.setFilter(new SyncInfoFilter() {
			public boolean select(SyncInfo info, IProgressMonitor monitor) {
				return true;
			}
		});
	}
	
	/**
	 * Return the set that provides access to the out-of-sync resources for the collector's
	 * subscriber that are descendants of the roots for the collector,
	 * are in the collector's working set and match the collectors filter. 
	 * @see getSubscriberSyncInfoSet()
	 * @see getWorkingSetSyncInfoSet()
	 * @return a SyncInfoSet containing out-of-sync resources
	 */
	public SyncInfoTree getSyncInfoTree() {
		return filteredInput.getSyncSet();
	}

	/**
	 * This causes the calling thread to wait any background collection of out-of-sync resources
	 * to stop before returning.
	 * @param monitor a progress monitor
	 */
	public void waitForCollector(IProgressMonitor monitor) {
		monitor.worked(1);
		// wait for the event handler to process changes.
		while(eventHandler.getEventHandlerJob().getState() != Job.NONE) {
			monitor.worked(1);
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
			Policy.checkCanceled(monitor);
		}
		monitor.worked(1);
	}
	
	/**
	 * Return the roots that are being considered by this collector.
	 * By default, the collector is interested in the roots of its
	 * subscriber. However, the set can be reduced using {@link setRoots(IResource)).
	 * @return
	 */
	public IResource[] getRoots() {
		return roots;
	}
	
	/**
	 * Clears this collector's sync info sets and causes them to be recreated from the
	 * associated <code>Subscriber</code>. The reset will occur in the background. If the
	 * caller wishes to wait for the reset to complete, they should call 
	 * {@link waitForCollector(IProgressMonitor)}.
	 */
	public void reset() {	
		workingSetInput.reset();
	}

	/**
	 * Disposes of the background job associated with this collector and deregisters
	 * all it's listeners. This method must be called when the collector is no longer
	 * referenced and could be garbage collected.
	 */
	public void dispose() {
		workingSetInput.disconnect();
		if(filteredInput != null) {
			filteredInput.disconnect();
		}
	}
	
	/**
	 * Return the event handler that performs the background processing for this collector.
	 * The event handler also serves the purpose of serializing the modifications and adjustments
	 * to the collector's sync sets in order to ensure that the state of the sets is kept
	 * consistent.
	 * @return Returns the eventHandler.
	 */
	protected SubscriberEventHandler getEventHandler() {
		return eventHandler;
	}
	
	/**
	 * Set the working set resources used to filter the output <code>SyncInfoSet</code>.
	 * @see getWorkingSetSyncInfoSet()
	 * @param resources the working set resources
	 */
	public void setWorkingSet(IResource[] resources) {
		workingSetInput.setWorkingSet(resources);
		workingSetInput.reset();
	}
	
	/**
	 * Get th working set resources used to filter the output sync info set.
	 * @return the working set resources
	 */
	public IResource[] getWorkingSet() {
		return workingSetInput.getWorkingSet();
	}
	
	/**
	 * Set the filter for this collector. Only elements that match the filter will
	 * be in the out sync info set.
	 * @see getSyncInfoSet()
	 * @param filter the sync info filter
	 */
	public void setFilter(SyncInfoFilter filter) {
		filteredInput.setFilter(filter);
		filteredInput.reset();
	}
	
	/**
	 * Return the filter that is filtering the output of this collector.
	 * @return a sync info filter
	 */
	public SyncInfoFilter getFilter() {
		if(filteredInput != null) {
			return filteredInput.getFilter();
		}
		return null;
	}
	
	/**
	 * Return a <code>SyncInfoSet</code> that contains the out-of-sync elements
	 * from the subsciber sync info set filtered
	 * by the working set resources but not the collector's <code>SyncInfoFilter</code>.
	 * @see getSubscriberSyncInfoSet()
	 * @return a <code>SyncInfoSet</code>
	 */
	public SyncInfoSet getWorkingSetSyncInfoSet() {
		return workingSetInput.getSyncSet();
	}

	/**
	 * Run the given runnable in the event handler of the collector
	 * @param runnable a runnable
	 */
	public void run(IWorkspaceRunnable runnable) {
		eventHandler.run(runnable, true /* front of queue */);
	}
}