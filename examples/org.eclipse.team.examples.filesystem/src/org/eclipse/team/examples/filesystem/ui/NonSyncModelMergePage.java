/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.internal.ui.mapping.SynchronizationResourceMappingContext;
import org.eclipse.team.ui.mapping.ISynchronizationCompareAdapter;
import org.eclipse.team.ui.synchronize.ModelMergeOperation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;

/**
 * This class provides the page for the {@link NonSyncMergePart}.
 */
public class NonSyncModelMergePage extends Page {
	
	IMergeContext context;
	TreeViewer viewer;
	List mappings;
	
	/*
	 * Content provider that returns the list of conflicting mappings
	 */
	class PageContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IMergeContext) {
				if (mappings == null)
					// TODO: should be using a real progress monitor
					computeMappings(new NullProgressMonitor());
				return mappings.toArray();
			}
			return new Object[0];
		}
		public Object getParent(Object element) {
			if (element instanceof ResourceMapping) {
				return context;
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			if (element instanceof IMergeContext) {
				return true;
			}
			return false;
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public void dispose() {
			// Nothing to do
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}
	}
	
	/*
	 * Label provider that provides a label and image for conflicting resource mappings
	 */
	class PageLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof ResourceMapping) {
				ResourceMapping mapping = (ResourceMapping) element;
				ISynchronizationCompareAdapter adapter = NonSyncMergePart.getCompareAdapter(mapping);
				if (adapter != null)
					return adapter.getPathString(mapping) + "(" + mapping.getModelProvider().getDescriptor().getLabel() + ")";
			}
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				ci.getName();
			}
			return super.getText(element);
		}
		public Image getImage(Object element) {
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				ci.getImage();
			}
			if (element instanceof ResourceMapping) {
				ResourceMapping mapping = (ResourceMapping) element;
				ISynchronizationCompareAdapter adapter = NonSyncMergePart.getCompareAdapter(mapping);
				ICompareInput input = adapter.asCompareInput(context, mapping.getModelObject());
				if (input != null)
					return input.getImage();
			}
			return super.getImage(element);
		}
	}

	/*
	 * Sorter that sorts mappings by model and then name
	 */
	class PageSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof ResourceMapping && e2 instanceof ResourceMapping) {
				ResourceMapping m1 = (ResourceMapping) e1;
				ResourceMapping m2 = (ResourceMapping) e2;
				if (m1.getModelProvider() == m2.getModelProvider()) {
					return getLabel(m1).compareTo(getLabel(m2));
				}
				return compare(m1, m2);
			}
			return super.compare(viewer, e1, e2);
		}
		private int compare(ResourceMapping m1, ResourceMapping m2) {
			ModelProvider[] sorted = ModelMergeOperation.sortByExtension(new ModelProvider[] { m1.getModelProvider(), m2.getModelProvider() });
			return sorted[0] == m1.getModelProvider() ? -1 : 1;
		}
		private String getLabel(ResourceMapping mapping) {
			ISynchronizationCompareAdapter adapter = NonSyncMergePart.getCompareAdapter(mapping);
			if (adapter != null)
				return adapter.getPathString(mapping);
			return "";
		}
	}
	
	public NonSyncModelMergePage(IMergeContext context) {
		super();
		this.context = context;
	}
	
	/**
	 * Create the list of all mappings that overlap with the out-of-sync files.
	 */
	public void computeMappings(IProgressMonitor monitor) {
	    IModelProviderDescriptor[] descriptors = ModelProvider.getModelProviderDescriptors();
	    mappings = new ArrayList();
	    for (int i = 0; i < descriptors.length; i++) {
	        IModelProviderDescriptor descriptor = descriptors[i];
	        // Get the subset of files that this model provider cares about
	        try {
				IResource[] resources = descriptor.getMatchingResources(getOutOfSyncFiles());
				if (resources.length > 0) {
				    ModelProvider provider = descriptor.getModelProvider();
				    // Get the mappings for those resources
				    ResourceMapping[] mappings = provider.getMappings(resources, new SynchronizationResourceMappingContext(context), monitor);
				    this.mappings.addAll(Arrays.asList(mappings ));
				}
			} catch (CoreException e) {
				FileSystemPlugin.log(e);
			}
	    }
	}

	private IResource[] getOutOfSyncFiles() {
		IDiff[] diffs = getContext().getDiffTree().getDiffs(ResourcesPlugin.getWorkspace().getRoot(), IResource.DEPTH_INFINITE);
		List result = new ArrayList();
		for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = ResourceDiffTree.getResourceFor(diff);
			if (resource.getType() == IResource.FILE)
				result.add(resource);
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	/**
	 * Return the merge context.
	 * @return the merge context
	 */
	public IMergeContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new PageContentProvider());
		viewer.setLabelProvider(new PageLabelProvider());
		viewer.setSorter(new PageSorter());
		hookContextMenu(viewer);
		viewer.setInput(context);
	}

	/*
	 * Hook the context menu to display the Overwrite and Mark-as-merged actions
	 */
	private void hookContextMenu(final TreeViewer viewer) {
		final MenuManager menuMgr = new MenuManager(); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	/**
	 * Fill the context menu.
	 * @param manager the context menu manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		/*
		 * Add a mark as merged action. Because we are not using the 
		 * Synchronization framework and, more specifically, the
		 * Common Navigator content provider for the model providers,
		 * we do not have access to the merge handlers of the model.
		 * Therefore, we are writing are action to detect whether there
		 * are files that overlap between the selected model elements and
		 * unselected model elements.
		 */
		Action markAsMerged = new Action("Mark as Merged") {
			public void run() {
				try {
					final IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException {
							IDiff[] diffs = getSelectedDiffs(selection, monitor);
							if (!checkForModelOverlap(diffs, monitor)) {
								return;
							}
							try {
								context.markAsMerged(diffs, false, monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException e) {
					FileSystemPlugin.log(new Status(IStatus.ERROR, FileSystemPlugin.ID, 0, e.getTargetException().getMessage(), e.getTargetException()));
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		};
		manager.add(markAsMerged);
	}

	protected IDiff[] getSelectedDiffs(IStructuredSelection selection, IProgressMonitor monitor) {
		Object[] elements = selection.toArray();
		return getDiffs(elements, monitor);
	}

	private IDiff[] getDiffs(Object[] elements, IProgressMonitor monitor) {
		Set result = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			try {
				if (element instanceof ResourceMapping) {
					ResourceMapping mapping = (ResourceMapping) element;
					ResourceTraversal[] traversals = mapping.getTraversals(new SynchronizationResourceMappingContext(context), monitor);
					result.addAll(Arrays.asList(context.getDiffTree().getDiffs(traversals)));
				}
			} catch (CoreException e) {
				FileSystemPlugin.log(e);
			}
		}
		return (IDiff[]) result.toArray(new IDiff[result.size()]);
	}

	/**
	 * Check whether any of the diffs overlap with mappings that are not selected
	 * @param diffs
	 * @return
	 */
	protected boolean checkForModelOverlap(IDiff[] diffs, IProgressMonitor monitor) {
		// TODO: This check should see if the diffs are also part of mappings 
		// that are not included in the selection. 
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	public Control getControl() {
		if (viewer != null)
			return viewer.getControl();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	public void setFocus() {
		if (viewer != null)
			viewer.getControl().setFocus();
	}

	public ISelectionProvider getViewer() {
		return viewer;
	}

}
