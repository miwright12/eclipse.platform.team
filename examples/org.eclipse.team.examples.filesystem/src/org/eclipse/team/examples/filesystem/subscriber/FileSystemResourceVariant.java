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
package org.eclipse.team.examples.filesystem.subscriber;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;

/**
 * A file system resource variant. Although not strictly necessary, this
 * class extends <code>CachedResourceVariant</code> which will
 * cache the contents of the resource variant.
 */
public class FileSystemResourceVariant extends CachedResourceVariant {
	
	private java.io.File ioFile;
	private byte[] bytes;
	
	/**
	 * Create a resource variant for the given file. The bytes will
	 * be calculated when they are accessed.
	 * @param file the file
	 */
	public FileSystemResourceVariant(java.io.File file) {
		this.ioFile = file;
	}
	
	/**
	 * Create a resource variant for the given file and sync bytes.
	 * @param file the file
	 * @param bytes the timestamp bytes
	 */
	public FileSystemResourceVariant(java.io.File file, byte[] bytes) {
		this.ioFile = file;
		this.bytes = bytes;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#fetchContents(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fetchContents(IProgressMonitor monitor) throws TeamException {
		try {
			setContents(new BufferedInputStream(new FileInputStream(ioFile)), monitor);
		} catch (FileNotFoundException e) {
			throw new TeamException("Failed to fetch contents for " + getFilePath(), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCachePath()
	 */
	protected String getCachePath() {
		// append the timestamp to the file path to give each variant a unique path
		return getFilePath() + " " + ioFile.lastModified();
	}
	
	private String getFilePath() {
		try {
			return ioFile.getCanonicalPath();
		} catch (IOException e) {
			// Failed for some reason. Try the absolute path.
			FileSystemPlugin.log(new Status(IStatus.ERROR, FileSystemPlugin.ID, 0, 
					"Failed to obtain canonical path for " + ioFile.getAbsolutePath(), e));
			return ioFile.getAbsolutePath();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCacheId()
	 */
	protected String getCacheId() {
		return FileSystemPlugin.ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#getName()
	 */
	public String getName() {
		return ioFile.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#isContainer()
	 */
	public boolean isContainer() {
		return ioFile.isDirectory();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#getContentIdentifier()
	 */
	public String getContentIdentifier() {
		// Use the modification timestamp as the content identifier
		return new Date(ioFile.lastModified()).toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
	 */
	public byte[] asBytes() {
		if (bytes == null) {
			// For simplicity, convert the timestamp to it's string representation.
			// A more optimal storage format would be the 8 bytes that make up the long.
			bytes = Long.toString(ioFile.lastModified()).getBytes();
		} 
		return bytes;
	}

	/**
	 * Return the files contained by the file of this resource variant.
	 * @return the files contained by the file of this resource variant.
	 */
	public File[] members() {
		return ioFile.listFiles();
	}
}
