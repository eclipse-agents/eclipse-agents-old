/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/package org.eclipse.agents.contexts;

import org.eclipse.ui.ISharedImages;

public class Images {

	public static final String IMG_SERVER = "icons/server.gif";
	public static final String IMG_RESOURCEMANAGER = "icons/file_obj.png";
	public static final String IMG_PROMPTMANAGER = "icons/template_obj.png";
	public static final String IMG_TOOL = "icons/insert_template.png";
	public static final String IMG_PLAY = "icons/play.gif";
	public static final String IMG_2WAYCOMPARE = "icons/twowaycompare_co.svg";
	public static final String IMG_UNDO_All = "icons/restart_all.svg";
	
	// shared UI images
	public static final String IMG_REMOVE_ALL = ISharedImages.IMG_ELCL_REMOVEALL;
	public static final String IMG_REMOVE = ISharedImages.IMG_ELCL_REMOVE;
	public static final String IMG_UNDO = ISharedImages.IMG_TOOL_UNDO;
	
	
	
	public static String[] imagelist = { 
			IMG_SERVER, 
			IMG_RESOURCEMANAGER, 
			IMG_PROMPTMANAGER, 
			IMG_TOOL,
			IMG_PLAY,
			IMG_2WAYCOMPARE,
			IMG_UNDO_All
	};
	
	public static String[] sharedList = {
		ISharedImages.IMG_ELCL_REMOVE,
		ISharedImages.IMG_ELCL_REMOVEALL,
		ISharedImages.IMG_TOOL_UNDO
	};
}










