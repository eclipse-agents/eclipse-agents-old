package org.eclipse.agents.chat.controller.workspace;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.Tracer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
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
	public Map<Path, WorkspaceChange> nodes = new HashMap<Path, WorkspaceChange>();
	ListenerList<IWorkspaceChangeListener> listeners;

//	Map<Path, IFileState> states = new HashMap<Path, IFileState>();
	
	public WorkspaceController(String sessionId) {
		this.sessionId = sessionId;
		listeners = new ListenerList<IWorkspaceChangeListener>();
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void addListener(IWorkspaceChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IWorkspaceChangeListener listener) {
		listeners.remove(listener);
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
		
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		
		boolean changeExists = nodes.containsKey(absolutePath);
		
		try {
			WorkspaceChange workspaceChange = changeExists ?
					nodes.get(absolutePath) :
					new WorkspaceChange(this, Differencer.CHANGE, absolutePath, doc.get(0, doc.getLength()));
		
			new ReplaceEdit(0, doc.getLength(), content).apply(doc);
			
			if (changeExists) {
				modifyChange(workspaceChange);
			} else {
				addChange(workspaceChange);
			}
		
		} catch (MalformedTreeException e) {
			e.printStackTrace();
			throw new JsonRpcException(e);
		} catch (BadLocationException e) {
			e.printStackTrace();
			throw new JsonRpcException(e);
		}
	}

	public void writeToFile(Path absolutePath, String content) {
		IFile file = findFile(absolutePath);
		boolean changeExists = nodes.containsKey(absolutePath);
		WorkspaceChange workspaceChange = nodes.get(absolutePath);
		
		if (!changeExists) {
			IFileState state = getHistory(absolutePath);
			if (state != null) {
				workspaceChange = new WorkspaceChange(this, Differencer.CHANGE, absolutePath, state);
			} else if (file != null) {
				workspaceChange = new WorkspaceChange(this, Differencer.CHANGE, absolutePath, readFromFile(absolutePath, null, null));
			} else {
				workspaceChange = new WorkspaceChange(this, Differencer.CHANGE, absolutePath, "");
			}
		}
		
		if (file != null && file.exists()) {
		    try {
		    	TextFileChange change = new TextFileChange(content, file);
		    	change.setSaveMode(TextFileChange.FORCE_SAVE); // Ensure it saves if the editor is closed
		    	ReplaceEdit edit = new ReplaceEdit(0, file.getContents().available(), content);
		    	change.setEdit(edit);
		    	change.initializeValidationData(new NullProgressMonitor());
		    	PerformChangeOperation op = new PerformChangeOperation(change);
		    	ResourcesPlugin.getWorkspace().run(op, new NullProgressMonitor());
		        
		        if (changeExists) {
		        	modifyChange(workspaceChange);
		        } else {
		        	addChange(workspaceChange);
		        }
		    } catch (CoreException e) {
		    	e.printStackTrace();
		    	throw new JsonRpcException(e);
		    } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new JsonRpcException(e);
			} catch (IOException e) {
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
		nodes.clear();
	}

	public void addChange(WorkspaceChange change) {
		nodes.put(change.path, change);
		for (IWorkspaceChangeListener listener: listeners) {
			listener.changeAdded(sessionId, change);
		}
	}
	
	public void removeChange(WorkspaceChange change) {
		nodes.remove(change.path);
		for (IWorkspaceChangeListener listener: listeners) {
			listener.changeRemoved(sessionId, change);
		}
	}
	
	public void modifyChange(WorkspaceChange change) {
		for (IWorkspaceChangeListener listener: listeners) {
			listener.changeModified(sessionId, change);
		}
	}
	
}
