package org.eclipse.agents.chat.controller.workspace;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class WorkspaceController {

	String sessionId;
	WorkspaceDiffNode rootNode = new WorkspaceDiffNode(Differencer.CHANGE);
	Map<Path, WorkspaceDiffNode> nodes = new HashMap<Path, WorkspaceDiffNode>();
//	Map<Path, IFileState> states = new HashMap<Path, IFileState>();
	
	public WorkspaceController(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public String readFromEditor(ITextEditor editor, Integer line, Integer limit) {
	 	IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		int offset = 0;
		int length = doc.getLength();
		
		try {
			if (line != null) {
				offset = doc.getLineOffset(line);
				if (limit != null) {
					int endLine = line + limit - 1;
					String delim = doc.getLineDelimiter(endLine);
					length = doc.getLineOffset(endLine) 
							+ doc.getLineLength(endLine) 
							- offset 
							- (delim == null ? 0 : delim.length());
				} else {
					length -= offset;
				}
			}
			
			Tracer.trace().trace(Tracer.ACP, "read: " + offset +": "  + length);
			return doc.get(offset, length);

		} catch (BadLocationException e) {
			e.printStackTrace();
			Tracer.trace().trace(Tracer.ACP, "read", e);
			throw new JsonRpcException(e);
		}
	}
	
	public String readFromFile(Path absolutePath, Integer line, Integer limit) {

		IFile file = findFile(absolutePath);
		StringBuffer buffer = new StringBuffer();
		
		if (file != null) {
			
			int firstLine = line == null ? 0 : line;
			int lineLimit = limit == null ? -1 : limit;
			
			try {
				InputStreamReader reader = new InputStreamReader(((IFile)file).getContents());
				BufferedReader breader = new BufferedReader(reader);
				int i = 0;
				String lineRead = breader.readLine();
				
				while (lineRead != null) {
					if (i >= firstLine) {
						if (lineLimit == -1 || i < firstLine + lineLimit) {
							if (!buffer.isEmpty()) {
								buffer.append("\n");
							}
							buffer.append(lineRead);
						}
					}
					lineRead = breader.readLine();
					i++;
				}
				breader.close();
			} catch (CoreException e) {
				e.printStackTrace();
				throw new JsonRpcException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new JsonRpcException(e);
			}
		} else {
			
		}
		
		return buffer.toString();
	}


	public void writeToEditor(Path absolutePath, ITextEditor editor, String content) {
		
		if (!nodes.containsKey(absolutePath)) {
			IFile file = findFile(absolutePath);
			ITypedElement left = new LocalResourceTypedElement(file);
			ITypedElement right = new StringNode(file, readFromFile(absolutePath, null, null));
			
			IFileState state = getHistory(absolutePath);
			if (state != null) {
				right = new HistoryItem(left, state);
			}
			
			nodes.put(absolutePath, new WorkspaceDiffNode(Differencer.CHANGE, left, right));
			rootNode.add(nodes.get(absolutePath));
			

//			IFile file = findFile(absolutePath);
//			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
//			FileNode left = new FileNode(file);
//			StringNode right = new StringNode(file, document.get());
//			nodes.put(absolutePath, new WorkspaceDiffNode(Differencer.CHANGE, left, right));
//			rootNode.add(nodes.get(absolutePath));
		}
		
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		
		if (!nodes.containsKey(absolutePath)) {
			nodes.put(absolutePath, new WorkspaceDiffNode(Differencer.CHANGE, absolutePath, doc.get()));
		}
		
		doc.set(content);
	}

	public void writeToFile(Path absolutePath, String content) {
		IFile file = findFile(absolutePath);
		
		if (file != null) {
			
			if (!nodes.containsKey(absolutePath)) {
				ITypedElement left = new ResourceNode(file);
				ITypedElement right = new StringNode(file, readFromFile(absolutePath, null, null));
				
				IFileState state = getHistory(absolutePath);
				if (state != null) {
					right = new HistoryItem(left, state);
				}

				nodes.put(absolutePath, new WorkspaceDiffNode(Differencer.CHANGE, left, right));
				rootNode.add(nodes.get(absolutePath));
				
			}
		    try {
		        byte[] bytes = content.getBytes(file.getCharset());
		        ByteArrayInputStream newContentStream = new ByteArrayInputStream(bytes);
		        IProgressMonitor monitor = new NullProgressMonitor(); // Or a real progress monitor
		        file.setContents(newContentStream, IFile.NONE, monitor); // IFile.NONE for no update flags
		    } catch (CoreException e) {
		    	e.printStackTrace();
		    	throw new JsonRpcException(e);
		    } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new JsonRpcException(e);
			}
		}
	}
	
	public IFileState getHistory(Path path) {
		try {
			IFile file = findFile(path);
			if (file != null) {
				IFileState[] fs = file.getHistory(null);
				if (fs == null || fs.length == 0) {
					InputStream source = file.getContents(true); // Get current contents
			        file.setContents(source, IFile.FORCE, new NullProgressMonitor());
			        fs = file.getHistory(null);
				}
				if (fs != null && fs.length > 0) {
					return fs[fs.length - 1];
				}
			}
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static ITextEditor findFileEditor(Path absolutePath) {
		ITextEditor editor = null;
		for (IWorkbenchWindow ww : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : ww.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					IEditorPart part = reference.getEditor(true);
					if (part != null && part instanceof ITextEditor) {
						IEditorInput input = part.getEditorInput();
						if (input instanceof FileEditorInput) {
							IFile file = ((IFileEditorInput)input).getFile();
							if (file.getRawLocation().equals(absolutePath)) {
								editor = (ITextEditor)part;
							}
						}
					}
				}
			}
		}
		Tracer.trace().trace(Tracer.ACP, absolutePath.toOSString() + ": " + (editor != null));
		
		return editor;
	}
	
	public static IFile findFile(Path absolutePath) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(absolutePath);
		if (file != null) {
			if (!file.exists()) {
				try {
					file.refreshLocal(0, new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	public void clearVariants() {
		rootNode = new WorkspaceDiffNode(Differencer.NO_CHANGE);
	}

	public void displayDiffs() {
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
//		try {
//			Utils.openEditor(page, new LocalFileRevision((IFileState)states.values().toArray()[0]), new NullProgressMonitor());
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		TeamUI.showHistoryFor(page, rootNode, new WorkspaceHistorySource());
		
		
		//CompareFileRevisionEditorInput
		//LocalResourceTypedElement
		//SaveableCompareEditorInput
		//CompareAction
		
		while(true) {
			Activator.getDisplay().readAndDispatch();
		}
		
//		CompareConfiguration configuration = new CompareConfiguration();
//		configuration.setLeftLabel("Agent Changes");
//		configuration.setRightLabel("Original");
//		configuration.setLeftEditable(true);
//		configuration.setRightEditable(false);
//
//		WorkspaceCompareInput input = new WorkspaceCompareInput(configuration, rootNode);
//		
//		CompareUI.openCompareDialog(input);
//		CompareUI.openCompareEditor(input);
		
//		rootNode = new WorkspaceDiffNode(Differencer.CHANGE);
//		for (Path path: states.keySet()) {
//			IFile file = findFile(path);
//			if (file != null) {
//				ITypedElement base = new ResourceNode(file);
//				ITypedElement target= base;
//				ITextEditor editor = findFileEditor(path);
//				if (editor != null) {
//					IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
//					if (document != null) {
//						target= new DocumentNode(document, file);
//					}
//				}
//				
//				ITypedElement edition = new HistoryItem(base, states.get(path));
//				
//				//GenericHistoryView
//			
//				//LocalHistoryPage
//			}
//		}

		
	}
	
	
}
