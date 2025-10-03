package org.eclipse.mcp.acp.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mcp.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

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
			public void completed(ProgressEvent event) {

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

			}
		});
		// Cancel opening of new windows
		browser.addOpenWindowListener(event -> event.required= true);

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
	
	public void addMessage(String id, String clazz, String content) {
		if (!browser.isDisposed()) {
			String sanitized = sanitize(content);
			String fxn = MessageFormat.format("addMessage(`{0}`, `{1}`, `{2}`)", id, clazz, sanitized);
			System.err.println(fxn);
			Activator.getDisplay().syncExec(()->browser.evaluate(fxn));
		}
	}
	
	public void addLinkedResources(String id, String clazz, String name, String url) {
		
	}
	
	public void updateMessage(String id, String content) {
		if (!browser.isDisposed()) {
			String sanitized = sanitize(content);
			String fxn = MessageFormat.format("updateMessage(`{0}`, `{1}`)", id, sanitized);
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
