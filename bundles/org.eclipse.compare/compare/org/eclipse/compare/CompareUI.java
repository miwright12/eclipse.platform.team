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
package org.eclipse.compare;

import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.structuremergeviewer.ICompareInput;


/**
 * The class <code>CompareUI</code> defines the entry point to initiate a configurable
 * compare operation on arbitrary resources. The result of the compare
 * is opened into a compare editor where the details can be browsed and
 * edited in dynamically selected structure and content viewers.
 * <p>
 * The Compare UI provides a registry for content and structure compare viewers,
 * which is initialized from extensions contributed to extension points
 * declared by this plug-in.
 */
public final class CompareUI {
	
	/**
	 * Compare Plug-in ID (value <code>"org.eclipse.compare"</code>).
	 * @since 2.0
	 */
	public static final String PLUGIN_ID= "org.eclipse.compare"; //$NON-NLS-1$

	/**
	 * Image descriptor for the disabled icon of the 'Next' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_DTOOL_NEXT= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.DTOOL_NEXT);
	/**
	 * Image descriptor for the normal icon of the 'Next' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_CTOOL_NEXT= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.CTOOL_NEXT);
	/**
	 * Image descriptor for the roll-over icon of the 'Next' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_ETOOL_NEXT= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.ETOOL_NEXT);
	
	/**
	 * Image descriptor for the disabled icon of the 'Previous' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_DTOOL_PREV= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.DTOOL_PREV);
	/**
	 * Image descriptor for the normal icon of the 'Previous' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_CTOOL_PREV= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.CTOOL_PREV);
	/**
	 * Image descriptor for the roll-over icon of the 'Previous' tool bar button.
	 * @since 2.0
	 */
	public static final ImageDescriptor DESC_ETOOL_PREV= CompareUIPlugin.getImageDescriptor(CompareUIPlugin.ETOOL_PREV);

	/**
	 * Name of the title property of a compare viewer.
 	 * If a property with this name is set
 	 * on the top level SWT control of a viewer, it is used as a title in the pane's
 	 * title bar.
 	 */
	public static final String COMPARE_VIEWER_TITLE= "org.eclipse.compare.CompareUI.CompareViewerTitle"; //$NON-NLS-1$
	
	private CompareUI() {
		// empty implementation
	}
	
	public static AbstractUIPlugin getPlugin() {
		return CompareUIPlugin.getDefault();
	}
	
	/**
	 * Returns this plug-in's resource bundle.
	 *
	 * @return the plugin's resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return CompareUIPlugin.getDefault().getResourceBundle();
	}
	
	/**
	 * Performs the comparison described by the given input and opens a
	 * compare editor on the result in the currently active workbench page.
	 *
	 * @param input the input on which to open the compare editor
	 */
	public static void openCompareEditor(CompareEditorInput input) {
		openCompareEditorOnPage(input, null);
	}
			
	/**
	 * Performs the comparison described by the given input and opens a
	 * compare editor on the result in the given workbench page.
	 *
	 * @param input the input on which to open the compare editor
	 * @param page the workbench page in which to open the compare editor
	 * @since 2.1
	 */
	public static void openCompareEditorOnPage(CompareEditorInput input, IWorkbenchPage page) {
		CompareUIPlugin plugin= CompareUIPlugin.getDefault();
		if (plugin != null)
			plugin.openCompareEditor(input, page, null);
	}
	
	/**
	 * Performs the comparison described by the given input and
	 * shows the result in the given editor.
	 *
	 * @param input the input on which to open the compare editor
	 * @param editor the compare editor to reuse or null to create a new one
	 * @since 3.0
	 */
	public static void reuseCompareEditor(CompareEditorInput input, IReusableEditor editor) {
		CompareUIPlugin plugin= CompareUIPlugin.getDefault();
		if (plugin != null)
			plugin.openCompareEditor(input, null, editor);
	}
			
	/**
	 * Performs the comparison described by the given input and opens a
	 * modal compare dialog on the result.
	 *
	 * @param input the input on which to open the compare dialog
	 */
	public static void openCompareDialog(CompareEditorInput input) {
		CompareUIPlugin plugin= CompareUIPlugin.getDefault();
		if (plugin != null)
			plugin.openCompareDialog(input);
	}
			
	/**
	 * Registers an image descriptor for the given type.
	 *
	 * @param type the type
	 * @param descriptor the image descriptor
	 */
	public static void registerImageDescriptor(String type, ImageDescriptor descriptor) {
		CompareUIPlugin.registerImageDescriptor(type, descriptor);
	}
	
	/**
	 * Returns a shared image for the given type, or a generic image if none
	 * has been registered for the given type.
	 * <p>
	 * Note: Images returned from this method will be automatically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param type the type
	 * @return the image
	 */
	public static Image getImage(String type) {
		return CompareUIPlugin.getImage(type);
	}
		
	/**
	 * Registers the given image for being disposed when this plug-in is shutdown.
	 *
	 * @param image the image to register for disposal
	 */
	public static void disposeOnShutdown(Image image) {
		CompareUIPlugin.disposeOnShutdown(image);
	}
	
	/**
	 * Returns a shared image for the given adaptable.
	 * This convenience method queries the given adaptable
	 * for its <code>IWorkbenchAdapter.getImageDescriptor</code>, which it
	 * uses to create an image if it does not already have one.
	 * <p>
	 * Note: Images returned from this method will be automatically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param adaptable the adaptable for which to find an image
	 * @return an image
	 */
	public static Image getImage(IAdaptable adaptable) {
		return CompareUIPlugin.getImage(adaptable);
	}
		
	
	/**
	 * Creates a stream merger for the given content type.
	 * If no stream merger is registered for the given content type <code>null</code> is returned.
	 *
	 * @param type the type for which to find a stream merger
	 * @return a stream merger for the given type, or <code>null</code> if no
	 *   stream merger has been registered
	 */
	public static IStreamMerger createStreamMerger(IContentType type) {
	    return CompareUIPlugin.getDefault().createStreamMerger(type);
	}

	/**
	 * Creates a stream merger for the given file extension.
	 * If no stream merger is registered for the file extension <code>null</code> is returned.
	 *
	 * @param type the type for which to find a stream merger
	 * @return a stream merger for the given type, or <code>null</code> if no
	 *   stream merger has been registered
	 */
	public static IStreamMerger createStreamMerger(String type) {
	    return CompareUIPlugin.getDefault().createStreamMerger(type);
	}

	/**
	 * Returns a structure compare viewer based on an old viewer and an input object.
	 * If the old viewer is suitable for showing the input, the old viewer
	 * is returned. Otherwise, the input's type is used to find a viewer descriptor in the registry
	 * which in turn is used to create a structure compare viewer under the given parent composite.
	 * If no viewer descriptor can be found <code>null</code> is returned.
	 *
	 * @param oldViewer a new viewer is only created if this old viewer cannot show the given input
	 * @param input the input object for which to find a structure viewer
	 * @param parent the SWT parent composite under which the new viewer is created
	 * @param configuration a configuration which is passed to a newly created viewer
	 * @return the compare viewer which is suitable for the given input object or <code>null</code>
	 */
	public static Viewer findStructureViewer(Viewer oldViewer, ICompareInput input, Composite parent,
				CompareConfiguration configuration) {

		return CompareUIPlugin.getDefault().findStructureViewer(oldViewer, input, parent, configuration);
	}
	
	/**
	 * Returns a content compare viewer based on an old viewer and an input object.
	 * If the old viewer is suitable for showing the input the old viewer
	 * is returned. Otherwise the input's type is used to find a viewer descriptor in the registry
	 * which in turn is used to create a content compare viewer under the given parent composite.
	 * If no viewer descriptor can be found <code>null</code> is returned.
	 *
	 * @param oldViewer a new viewer is only created if this old viewer cannot show the given input
	 * @param input the input object for which to find a content viewer
	 * @param parent the SWT parent composite under which the new viewer is created
	 * @param configuration a configuration which is passed to a newly created viewer
	 * @return the compare viewer which is suitable for the given input object or <code>null</code>
	 */
	public static Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent,
			CompareConfiguration configuration) {
		return CompareUIPlugin.getDefault().findContentViewer(oldViewer, input, parent, configuration);
	}
	
	/**
	 * Returns a content compare viewer based on an old viewer and an input
	 * object. If the old viewer is suitable for showing the input the old
	 * viewer is returned. Otherwise the input's type is used to find a viewer
	 * descriptor in the registry which in turn is used to create a content
	 * compare viewer under the given parent composite. In order to determine
	 * the input's type, the input must either implement IStreamContentAccessor
	 * and ITypedElement or ICompareInput. If no viewer descriptor can be found
	 * <code>null</code> is returned.
	 *
	 * @param oldViewer a new viewer is only created if this old viewer cannot show the given input
	 * @param input the input object for which to find a content viewer. Must
	 * implement either <code>IStreamContentAccessor</code> and<code>
	 * ITypedElement</code> or <code>ICompareInput</code>.
	 * @param parent the SWT parent composite under which the new viewer is created
	 * @param configuration a configuration which is passed to a newly created viewer
	 * @return the compare viewer which is suitable for the given input object or <code>null</code>
	 */
	public static Viewer findContentViewer(Viewer oldViewer, Object input, Composite parent,
			CompareConfiguration configuration) {
		
		return CompareUIPlugin.getDefault().findContentViewer(oldViewer, input, parent, configuration);
	}

	/**
	 * Adds an alias for the given type.
	 * Subsequent calls to <code>findStructureViewer</code>
	 * treat alias as a synonym for type and return the same viewer.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call this method. 
	 * @param type a type name for which a viewer has been registered
	 * @param alias a type name which should be treated as a synonym of type
	 * @since 2.0
	 */
	public static void addStructureViewerAlias(String type, String alias) {
		CompareUIPlugin.getDefault().addStructureViewerAlias(type, alias);
	}
	
	/**
	 * Remove all aliases for the given type. This method does not affect
	 * the initial binding between type and viewer. If no aliases exist for the
	 * given type this method does nothing.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call this method. 
	 * @param type the type name for which all synonymes are removed.
	 * @since 2.0
	 */
	public static void removeAllStructureViewerAliases(String type) {
		CompareUIPlugin.getDefault().removeAllStructureViewerAliases(type);
	}
}
