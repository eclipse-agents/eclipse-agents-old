package org.eclipse.agents.chat.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageBookViewPage;

public class WorkspaceController {

	Map<IFile, IFileState> fileVariants = new HashMap<IFile, IFileState>();
	
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
