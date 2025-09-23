package org.eclipse.mcp.acp.view;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleDocumentPartitionerExtension;

public class AcpDocumentPartitioner implements IConsoleDocumentPartitioner, IConsoleDocumentPartitionerExtension, IDocumentPartitionerExtension {

	IDocumentPartitioner dp;
	IConsoleDocumentPartitioner cdp;
	IConsoleDocumentPartitionerExtension cdpe;
	IDocumentPartitionerExtension dpe;
	
	public AcpDocumentPartitioner(IDocumentPartitioner dp) {
		this.dp = dp;
		this.cdp = (IConsoleDocumentPartitioner)dp;
		this.cdpe = (IConsoleDocumentPartitionerExtension)dp;
		this.dpe = (IDocumentPartitionerExtension)dp;
	}

	@Override
	public ITypedRegion[] computePartitioning(int arg0, int arg1) {
		return dp.computePartitioning(arg0, arg1);
	}

	@Override
	public void connect(IDocument arg0) {
		dp.connect(arg0);
	}

	@Override
	public void disconnect() {
		dp.disconnect();
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent arg0) {
		dp.documentAboutToBeChanged(arg0);
	}

	@Override
	public boolean documentChanged(DocumentEvent arg0) {
		return dp.documentChanged(arg0);
	}

	@Override
	public String getContentType(int arg0) {
		return dp.getContentType(arg0);
	}

	@Override
	public String[] getLegalContentTypes() {
		return dp.getLegalContentTypes();
	}

	@Override
	public ITypedRegion getPartition(int arg0) {
		return dp.getPartition(arg0);
	}

	@Override
	public IRegion documentChanged2(DocumentEvent arg0) {
		return dpe.documentChanged2(arg0);
	}

	@Override
	public ITypedRegion[] computeReadOnlyPartitions() {
		return cdpe.computeReadOnlyPartitions();
	}

	@Override
	public ITypedRegion[] computeReadOnlyPartitions(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return cdpe.computeReadOnlyPartitions(arg0, arg1);
	}

	@Override
	public ITypedRegion[] computeWritablePartitions() {
		// TODO Auto-generated method stub
		return cdpe.computeWritablePartitions();
	}

	@Override
	public ITypedRegion[] computeWritablePartitions(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return cdpe.computeWritablePartitions(arg0, arg1);
	}

	@Override
	public boolean containsReadOnly(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return cdpe.containsReadOnly(arg0, arg1);
	}

	@Override
	public int getNextOffsetByState(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return cdpe.getNextOffsetByState(arg0, arg1);
	}

	@Override
	public int getPreviousOffsetByState(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return cdpe.getPreviousOffsetByState(arg0, arg1);
	}

	@Override
	public boolean isReadOnly(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return cdpe.isReadOnly(arg0, arg1);
	}

	@Override
	public StyleRange[] getStyleRanges(int arg0, int arg1) {
		// TODO Auto-generated method stub
		StyleRange sr = new StyleRange();
		sr.start = arg0;
		sr.length = arg1;
        sr.borderStyle = SWT.BORDER_DOT;
        return new StyleRange[] {sr};
//		return cdp.getStyleRanges(arg0, arg1);
	}

	@Override
	public boolean isReadOnly(int arg0) {
		// TODO Auto-generated method stub
		return cdp.isReadOnly(arg0);
	}
	

}
