package org.eclipse.agents.chat.controller.workspace;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.SessionAdapter;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.texteditor.ITextEditor;

public class WorkspaceController {

	String sessionId;
	Map<IFile, IFileState> fileVariants = new HashMap<IFile, IFileState>();
	
	public WorkspaceController(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public String readTextFile(Path absolutePath, Integer line, Integer limit) {
		ITextEditor editor = findFileEditor(absolutePath);
		if (editor != null) {
			return readFromEditor(editor, line, limit);
		} else {
			return readFromFile(absolutePath, line, limit);
		}
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
					length = doc.getLineOffset(endLine) + doc.getLineLength(endLine);
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


	public void writeToEditor(ITextEditor editor, String content) {
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.set(content);
	}

	public void writeToFile(Path absolutePath, String content) {
		IFile file = findFile(absolutePath);
		
		if (file != null) {
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
		fileVariants.clear();
	}

	public void addFileVariant(IFile file) {
		if (!fileVariants.containsKey(file)) {
			try {
				IFileState[] history = file.getHistory(new NullProgressMonitor());
				 if (history.length > 0) {
					 addFileVariant(file,  history[history.length - 1]);
				} else {
					System.err.println("handle this case");
				}
			 } catch (CoreException e) {
				e.printStackTrace();
			 }
		}
	}
	
	public void addFileVariant(IFile file, IFileState state) {
		fileVariants.put(file,  state);
	}
	
	public void displayDiffs() {
		if (!fileVariants.isEmpty()) {
			SyncInfoTree syncSet = new SyncInfoTree();
		    IResourceVariantComparator comparator = new FileVariantComparator();
		    
		    for (IFile file: fileVariants.keySet()) {
		        // The local file is our "local" resource
		        // The current state in history (from IFileState) is our "remote" variant
		        // The 'base' can be null, or another common ancestor
		        
		        FileStateVariant remoteVariant = new FileStateVariant(file, fileVariants.get(file));
		        
		        // SyncInfo calculates the difference kind automatically based on comparator logic
		        SyncInfo info = new SyncInfo(file, null, remoteVariant, comparator);
		   
		        // Manually set the kind to force it to show as an outgoing change (modified) if needed
		        // info.setKind(SyncInfo.OUTGOING | SyncInfo.CHANGE); 
		        
		        syncSet.add(info);
		    }
		    
//		    SyncInfoCompareInput input = new SyncInfoCompareInput("abcd", syncSet); 
//		    
//		    ISynchronizationScopeManager manager = new 
//		    SynchronizationContext context = new SynchronizationContext();
//		    ModelSynchronizeParticipant msp = new ModelSynchronizeParticipant(syncSet);
//		    ISynchronizeManager manager = TeamUI.getSynchronizeManager();
//		    
//		    // Create and register the participant
//		    manager.addSynchronizeParticipants(new ISynchronizeParticipant[]{msp});
//		    
//		    // Show the synchronize view and activate your new participant
//		    ISynchronizeView view = manager.showSynchronizeViewInActivePage();
//		    view.display(msp);

		    // Optional: pin the view so it doesn't get replaced by default SCM operations
		    
		}
	}
	
	
	class FileStateVariant implements IResourceVariant {
		
		IFileState state;
		IFile file;
		
		public FileStateVariant(IFile file, IFileState state) {
			this.file = file;
			this.state = state;
		}

		@Override
		public byte[] asBytes() {
			try {
				InputStream is = state.getContents();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
				    buffer.write(data, 0, nRead);
				}
				buffer.flush();
				return buffer.toByteArray();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new byte[0];
		}

		@Override
		public String getContentIdentifier() {
			return DateFormat.getDateTimeInstance().format(new Date(state.getModificationTime()));
		}

		@Override
		public String getName() {
			return state.getName();
		}

		@Override
		public IStorage getStorage(IProgressMonitor arg0) throws TeamException {
			return state;
		}

		@Override
		public boolean isContainer() {
			return false;
		}
	}
	
	class FileVariantComparator implements IResourceVariantComparator {

		@Override
		public boolean compare(IResource arg0, IResourceVariant arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean compare(IResourceVariant arg0, IResourceVariant arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isThreeWay() {
			// TODO Auto-generated method stub
			return false;
		}
		
		
	}
	
	class FileVariantParticipant extends AbstractSynchronizeParticipant {

		@Override
		public IPageBookViewPage createPage(ISynchronizePageConfiguration arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void run(IWorkbenchPart arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void initializeConfiguration(ISynchronizePageConfiguration arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
