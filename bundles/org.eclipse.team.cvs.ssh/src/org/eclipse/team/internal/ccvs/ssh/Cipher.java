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
package org.eclipse.team.internal.ccvs.ssh;

public abstract class Cipher {
public abstract void decipher(byte[] src, int srcPos, byte[] dst, int dstPos, int len);
public abstract void encipher(byte[] src, int srcPos, byte[] dst, int dstPos, int len);
public static Cipher getInstance(String algorithm) {
	try {
		Class c = Class.forName("org.eclipse.team.internal.ccvs.ssh." + algorithm); //$NON-NLS-1$
		return (Cipher) c.newInstance();
	} catch (Exception e) {
		return null;
	}
}
public abstract void setKey(byte[] key);
}