package org.eclipse.agents.chat.controller.workspace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

public class DocumentNode  implements ITypedElement, IEncodedStreamContentAccessor {
	private static final String UTF_16= "UTF-16"; //$NON-NLS-1$
	private final IDocument fDocument;
	private final IFile fFile;

	DocumentNode(IDocument document, IFile file) {
		fDocument= document;
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
		return new ByteArrayInputStream(Utilities.getBytes(fDocument.get(), UTF_16));
	}

	@Override
	public String getCharset() {
		return UTF_16;
	}
}
