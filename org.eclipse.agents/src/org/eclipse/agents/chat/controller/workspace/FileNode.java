package org.eclipse.agents.chat.controller.workspace;

import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class FileNode  implements ITypedElement, IEncodedStreamContentAccessor {
	private static final String UTF_16= "UTF-16"; //$NON-NLS-1$
	private final IFile fFile;

	FileNode(IFile file) {
		fFile= file;
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
			return fFile.getContents(true);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCharset() {
		return UTF_16;
	}
}
