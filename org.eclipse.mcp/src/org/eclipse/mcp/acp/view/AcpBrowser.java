package org.eclipse.mcp.acp.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.mcp.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
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

		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				
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
			browser.evaluate(fxn);
		}
	}
	
	public void updateMessage(String id, String content) {
		if (!browser.isDisposed()) {
			String sanitized = sanitize(content);
			String fxn = MessageFormat.format("addMessage(`{0}`, `{1}`)", id, sanitized);
			browser.evaluate(fxn);
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
