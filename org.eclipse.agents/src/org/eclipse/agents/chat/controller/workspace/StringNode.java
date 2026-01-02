/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.agents.chat.controller.workspace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;

public class StringNode  implements ITypedElement, IEncodedStreamContentAccessor {
	private static final String UTF_16= "UTF-16"; //$NON-NLS-1$
	private final IFile fFile;
	private final String content;

	StringNode(IFile file, String content) {
		fFile= file;
		this.content = content;
	}

	@Override
	public String getName() {
		return fFile.getName();
	}

	@Override
	public String getType() {
		return fFile.getFileExtension();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public InputStream getContents() {
		try {
			return new ByteArrayInputStream(content.getBytes(UTF_16));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCharset() {
		return UTF_16;
	}
}
