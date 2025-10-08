package org.eclipse.mcp.acp.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionUpdate;
import org.eclipse.mcp.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AcpBrowser {

	private ObjectMapper mapper;
	private Browser browser;
	
	public AcpBrowser(Composite parent, int style) {
		mapper = new ObjectMapper();
		
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
//				Color link = Activator.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND);
				
				Color link = JFaceResources.getColorRegistry().get(JFacePreferences.HYPERLINK_COLOR);
				Color linkActive = JFaceResources.getColorRegistry().get(JFacePreferences.ACTIVE_HYPERLINK_COLOR); 
				Color info_fg = JFaceResources.getColorRegistry().get(JFacePreferences.INFORMATION_FOREGROUND_COLOR);
				Color info_bg = JFaceResources.getColorRegistry().get(JFacePreferences.INFORMATION_BACKGROUND_COLOR);
				Color bg = JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
				Color fg = JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
//				link = JFaceResources.getColorRegistry().get("org.eclipse.ui.editors.hyperlinkColor");
				
				String textFg = String.format("rgb(%d, %d, %d)", fg.getRed(), fg.getGreen(), fg.getBlue());
				String textBg = String.format("rgb(%d, %d, %d)", bg.getRed(), bg.getGreen(), bg.getBlue());
				String linkFg = String.format("rgb(%d, %d, %d)", link.getRed(), link.getGreen(), link.getBlue());
				String infoFg = String.format("rgb(%d, %d, %d)", info_fg.getRed(), info_fg.getGreen(), info_fg.getBlue());
				String infoBg = String.format("rgb(%d, %d, %d)", info_bg.getRed(), info_bg.getGreen(), info_bg.getBlue());
				
				
				String fxn = String.format("setStyle(`%spx`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`)", 
						fontHeight, textFg, textBg, linkFg, linkActive, infoFg, infoBg);
				System.err.println(fxn);
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
		
		browser.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				ObjectNode o = mapper.createObjectNode();
				o.put("character", e.character);
				o.put("detail", e.detail);
				o.put("doit", e.doit);
				o.put("keycode", e.keyCode);
				o.put("keyLocation", e.keyLocation);
				o.put("stateMask", e.stateMask);
				o.put("time", e.time);
				
				Activator.getDisplay().syncExec(()-> {
					Object doit = browser.evaluate(String.format("keyTraversed(`%s`)", sanitize(o.toString())));
					e.doit = Boolean.valueOf(doit.toString());
				});
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
	
	public String sanitize(String s) {
		//TODO security
		return s.replaceAll("`", "\\\\`");
	}
	
	public boolean isDisposed() {
		return browser.isDisposed();
	}

	public void updateSession(SessionUpdate update) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(update);
				String fxn = String.format("updateSession(`%s`)", sanitize(json));
				System.err.println(fxn);
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void acceptPromptRequest(PromptRequest request) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(request);
				String fxn = String.format("acceptPromptRequest(`%s`)", sanitize(json));
				System.err.println(fxn);
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void acceptSessionNotification(SessionNotification notification) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(notification.update());
				String fxn = String.format("acceptSessionNotification(`%s`)", sanitize(json));
				System.err.println(fxn);
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void acceptSessionUserMessageChunk(ContentBlock block) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(block);
				String fxn = String.format("acceptSessionUserMessageChunk(`%s`)", sanitize(json));
				System.err.println(fxn);
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void acceptSessionAgentThoughtChunk(ContentBlock block) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(block);
				String fxn = String.format("acceptSessionAgentThoughtChunk(`%s`)", sanitize(json));
				System.err.println(fxn);
//				new JsonParser().parse(json);
//				new JsonParser().parse(sanitize(json));
				
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void acceptSessionAgentMessageChunk(ContentBlock block) {
		if (!browser.isDisposed()) {
			try {
				String json = mapper.writeValueAsString(block);
				String fxn = String.format("acceptSessionAgentMessageChunk(`%s`)", sanitize(json));
				System.err.println(fxn);
				Activator.getDisplay().syncExec(()-> {
					System.err.println(browser.evaluate(fxn));
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
}
