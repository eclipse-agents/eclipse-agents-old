package org.eclipse.mcp.acp.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;

public class AcpBrowser {

	private Browser browser;
	
	public AcpBrowser(Composite parent, int style) {
		browser = new Browser(parent, style);
		
		browser.setJavascriptEnabled(true);

		browser.setForeground(Activator.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		browser.setBackground(Activator.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setVisible(false);
		
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent pe) {

				int fontHeight = 13;
				
//				Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
				Font font = JFaceResources.getFont(JFaceResources.DIALOG_FONT);
				FontData[] data = font.getFontData();
				if (data != null && data.length > 0) {
					fontHeight = data[0].getHeight();
				}
				
//				Color bg = Activator.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
//			 	Color fg = Activator.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
				Color link = Activator.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND);
				Color fg = JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.INFORMATION_FOREGROUND");
				Color bg = JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.INFORMATION_BACKGROUND");
//				bg = JFaceResources.getColorRegistry().get("org.eclipse.ui.editors.backgroundColor");
//				fg = JFaceResources.getColorRegistry().get("org.eclipse.ui.editors.foregroundColor");
//				link = JFaceResources.getColorRegistry().get("org.eclipse.ui.editors.hyperlinkColor");
				
				String textFg = String.format("rgb(%d, %d, %d)", fg.getRed(), fg.getGreen(), fg.getBlue());
				String textBg = String.format("rgb(%d, %d, %d)", bg.getRed(), bg.getGreen(), bg.getBlue());
				String linkFg = String.format("rgb(%d, %d, %d)", link.getRed(), link.getGreen(), link.getBlue());
				
				String fxn = MessageFormat.format("setStyle(`{0}px`, `{1}`, `{2}`)", fontHeight, textFg, textBg);
				Activator.getDisplay().syncExec(()->browser.evaluate(fxn));
				
				browser.setVisible(true);
				
				browser.addLocationListener(LocationListener.changingAdapter(event -> {
					event.doit = false;
					
					WorkspaceResourceAdapter wra = new WorkspaceResourceAdapter(event.location);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					
					IResource resource = wra.getModel();
					if (resource instanceof IFile) {
						try {
						    IDE.openEditor(page, (IFile)resource); 
						} catch (PartInitException e) {
						    e.printStackTrace();
						}
					} else if (resource instanceof IFolder || resource instanceof IProject) {
						try {
							IViewPart view = page.showView("org.eclipse.ui.navigator.ProjectExplorer");
							if (view instanceof CommonNavigator) {
				                CommonNavigator projectExplorer = (CommonNavigator) view;
				                if (resource.exists() && resource.getProject().exists() && resource.getProject().isOpen()) {
					                projectExplorer.selectReveal(new StructuredSelection(resource));
				                }
				            }
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}));

			}
		});
		// Cancel opening of new windows
		browser.addOpenWindowListener(event -> {
			event.required= true;
		});

		// Replace browser's built-in context menu with none
		browser.setMenu(new Menu(browser.getShell(), SWT.NONE));
	}
	
	public void initialize() {

		try {
			File file = Activator.getDefault().getBundleFile("chat/session.html");
			browser.setUrl(file.toURI().toURL().toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void addPromptTurn() {
		if (!browser.isDisposed()) {
			String fxn = "addPromptTurn()";
			System.err.println(fxn);
			Activator.getDisplay().syncExec(()->browser.evaluate(fxn));
		}
	}
	
	public void addMessage(String _class, String content, boolean isMarkdown, boolean isChunk) {
		if (!browser.isDisposed()) {
			if (isMarkdown) {
				content = sanitize(content);
			}
			String fxn = String.format("addMessage(`%s`, `%s`, `%s`, `%s`)", _class, content, isMarkdown, isChunk);
			System.err.println(fxn);
			Activator.getDisplay().syncExec(()->browser.evaluate(fxn));
		}
	}

	public void addResourceLink(String text, String url, String _class) {
		if (!browser.isDisposed()) {
			String fxn = String.format("addResourceLink(`%s`, `%s`, `%s`)", text, url, _class);
			System.err.println(fxn);
			Activator.getDisplay().syncExec(()->browser.evaluate(fxn));
		}
	}
	
	public String sanitize(String s) {
		//TODO security
		return s.replaceAll("`", "\\\\`");
	}
	
	public boolean isDisposed() {
		return browser.isDisposed();
	}
}
