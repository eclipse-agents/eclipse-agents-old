package org.eclipse.mcp.acp.view;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;


public class AcpView extends ViewPart  {

	public static final String ID  = "com.ibm.systemz.wcaz4e.explanation.CodeExplanationView"; //$NON-NLS-1$

	CodeExplanationInput explanationInput;
	CodeExplanationJob explanationJob = null;

	StyledText sourceLink;
	Text inputText;
	boolean disposed = false;

	@Override
	public void createPartControl(Composite parent) {
		Composite middle = new Composite(parent, SWT.NONE);
		middle.setLayout(new GridLayout(1, true));
		middle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		sourceLink = new StyledText (middle, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		sourceLink.addListener(SWT.MouseDown, event -> {
			int offset = sourceLink.getOffsetAtPoint(new Point (event.x, event.y));
			if (offset != -1) {
				StyleRange style = null;
				try {
					style = sourceLink.getStyleRangeAtOffset(offset);
				} catch (IllegalArgumentException e) {
						// no character under event.x, event.y
				}
				if (style != null && style.underline && style.underlineStyle == SWT.UNDERLINE_LINK) {
					explanationInput.jumpToSourceLocation();
				}
			}
		});
		sourceLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		

		messageText = new Text(middle, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		messageText.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public void setFocus() {

	}

	public void setInput(CodeExplanationInput input) {
		if (explanationJob != null) {
			explanationJob.cancel();
		}
		
		this.explanationInput = input;

		String explanationType;
		switch (input.getCommand()) {
		case SIMPLE:
			explanationType = Messages.CodeExplanationView_explanationType_simple;
			break;
		case DETAILED:
			explanationType = Messages.CodeExplanationView_explanationType_detailed;
			break;
		case GUIDED:
			explanationType = Messages.CodeExplanationView_explanationType_guided;
			break;
		default:
			explanationType = Messages.CodeExplanationView_explanationType_unknown;
			break;
		}
		
		String lines = input.getSourceStartLine() == input.getSourceEndLine() ? "" + (input.getSourceStartLine() + 1) //$NON-NLS-1$
				: (input.getSourceStartLine() + 1) + " - " + (input.getSourceEndLine() + 1); //$NON-NLS-1$
		String referenceWord = input.getReferenceWord();
		String referenceWordType = input.getReferenceWordType();
		String fileName = input.getFileName();

		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				StyleRange style = new StyleRange();
				style.underline = true;
				style.underlineStyle = SWT.UNDERLINE_LINK;

				
				if (referenceWord != null && referenceWordType != null) {
					String localized = MessageFormat.format(Messages.CodeExplanationView_source_link_w_reference,
							explanationType, input.getFileName(), referenceWordType, referenceWord, lines);
					sourceLink.setText(localized);
					
					int[] ranges = {
							localized.indexOf(fileName), 
							fileName.length(), 
							localized.indexOf(referenceWord), 
							referenceWord.length(),
							localized.indexOf(lines),
							lines.length()};
					
					StyleRange[] styles = {style, style, style};
					sourceLink.setStyleRanges(ranges, styles);
				} else {
					
					String localized = MessageFormat.format(Messages.CodeExplanationView_source_link, explanationType, input.getFileName(), lines);
					sourceLink.setText(localized);
					
					int[] ranges = {
							localized.indexOf(fileName), 
							fileName.length(), 
							localized.indexOf(lines),
							lines.length()};
					
					StyleRange[] styles = {style, style};
					sourceLink.setStyleRanges(ranges, styles);
				}
				sourceLink.requestLayout();
				messageText.setText(""); //$NON-NLS-1$
				
				updateEnablements();
			}	
		});

		explanationJob = new CodeExplanationJob(this, input);
		explanationJob.schedule();
	}

	public void runCommand(Command command, boolean toggleSelected) {
		switch(command) {
		case COMMENT:
			toggleComment(command, toggleSelected);
			break;
		case COPY:
			doCopy();
			break;
		case DOWNLOAD:
			doDownload();
			break;
		case INSERT:
			doInsert();
			break;
		case REGENERATE:
		case REGENERATE_DETAILED:
		case REGENERATE_GUIDED:
		case REGENERATE_SIMPLE:
			doRegenerate(command);
			break;	
		default:
			break;
		}

	}

	private void doCopy() {
		TextTransfer textTransfer = TextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(Activator.getDisplay());
		String selectionText = messageText.getSelectionText();
		if (selectionText != null && !selectionText.isEmpty()) {
			clipboard.setContents(new Object[] { selectionText }, new Transfer[] { textTransfer });
		} else if (messageText.getText() != null && !messageText.getText().isEmpty() ) {
			clipboard.setContents(new Object[] { messageText.getText() }, new Transfer[] { textTransfer });
		}
	}

	private void doInsert() {
		if (messageText.getText() == null || messageText.getText().length() == 0) {
			return;
		}
		
		String explanation;
		if (messageText.getSelectionCount() > 0) {
			explanation = messageText.getSelectionText();
		} else {
			explanation = messageText.getText();
		}
		
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		if (editor != null && editor instanceof ITextEditor) {
			ITextEditor textEditor =(ITextEditor)editor;
			if (textEditor.isEditable()) {
				final ISelection selection = textEditor.getSelectionProvider().getSelection();
				if (selection instanceof ITextSelection) {
					Activator.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								explanationInput.insertExplanationAsComment((ITextEditor)editor, explanation, Math.max(0, ((ITextSelection)selection).getEndLine()));
							} catch (Exception e) {
								Tracer.trace().trace(Tracer.API, "Error updating document of CommonSourceEditor", e); //$NON-NLS-1$
							}
						}
					});
				}
			}
		}
	}

	private void doDownload() {
		if (messageText.getText() != null && messageText.getText().length() > 0) {
			FileDialog fileDialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
			fileDialog.setFileName("codeExplanation.txt"); //$NON-NLS-1$
			fileDialog.setFilterExtensions(new String[] { "*.txt" }); //$NON-NLS-1$
			fileDialog.setOverwrite(true);
			fileDialog.setText(Messages.CodeExplanationView_download_title);
			String absolutePath = fileDialog.open();
			if (absolutePath != null) {
				try {
					FileWriter writer = new FileWriter(new File(absolutePath));
					writer.write(messageText.getText());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void doRegenerate(Command command) {
		if (explanationInput != null) {
			switch(command) {
			case REGENERATE:
				// do not change explanation type
				break;
			case REGENERATE_SIMPLE:
				explanationInput.setCommand(Command.SIMPLE);
				break;
			case REGENERATE_DETAILED:
				explanationInput.setCommand(Command.DETAILED);
				break;
			case REGENERATE_GUIDED:
				explanationInput.setCommand(Command.GUIDED);
				break;	
			default:
				break;
			}
			setInput(explanationInput);
		}
		
	}

	private void toggleComment(Command command, boolean toggleSelected) {
//		if (toggleSelected) {
//			IDocument document = ((ITextEditor)input.getTextEditor()).getDocumentProvider().getDocument(input.editorInput);
//			String delimiter = TextUtilities.getDefaultLineDelimiter(document);
//			String formattedContent = getFormattedContent(delimiter);
//			messageText.setText(formattedContent);
//		} else {
//			messageText.setText(response.generated_text);
//		}
	}

	public void setExplanation(String explanationText) {
		IExplanationLanguage explanationLanguage = explanationInput.getExplanationLanguage();
		if (explanationLanguage != null) {
			String formattedExplanation = explanationLanguage.getFormattedExplanation(explanationText, explanationInput.getCommand().toString());
			if (formattedExplanation != null) {
				explanationText = formattedExplanation;
			}
		}
		final String fExplanationText = explanationText;
		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!messageText.isDisposed()) {
					messageText.setText(fExplanationText);
				}
			}
		});
		explanationJob = null;
	}

	public boolean isRegenerateCommandEnabled(Command command) {
		if (explanationInput != null) {
			IExplanationLanguage language = explanationInput.getExplanationLanguage();
			if (language != null) {
				if (language instanceof CobolLanguage) {
					return command == Command.SIMPLE || 
							command == Command.DETAILED ||
							command == Command.GUIDED;
				} else if (language instanceof JclLanguage) {
					return command == Command.SIMPLE ||
							command == Command.DETAILED;
				}
			}
		}
		return false;
	}
	
	public Command getActiveCommand() {
		if (explanationInput != null) {
			return explanationInput.command;
		}
		return null;
	}
	

	private void updateEnablements() {

		if (!disposed) {
			ICommandService service = (ICommandService) getSite().getService(ICommandService.class);
			boolean explanationExists = messageText.getText() != null && !messageText.getText().isEmpty();
			
			setEnabled(Command.DOWNLOAD, explanationExists);
			setEnabled(Command.INSERT, explanationExists);
			setEnabled(Command.COPY, explanationExists);
			
		}
	}
	
	private void setEnabled(Command command, boolean enabled) {
		ICommandService service = (ICommandService) getSite().getService(ICommandService.class);
		org.eclipse.core.commands.Command scopedCommand = service.getCommand(command.getCommandId());
		if (scopedCommand != null) {
			IHandler handler = scopedCommand.getHandler();
			if (handler instanceof IHandler2) {
				((IHandler2)handler).setEnabled("" + enabled);
			}
		}
		IContributionItem[] items = getViewSite().getActionBars().getToolBarManager().getItems();
		for (IContributionItem item: items) {
			
		}
		items.toString();
	}

	@Override
	public void dispose() {
		super.dispose();
		this.disposed = true;
	}
}
