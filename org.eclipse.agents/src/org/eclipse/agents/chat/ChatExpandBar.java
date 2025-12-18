package org.eclipse.agents.chat;


import org.eclipse.agents.Activator;
import org.eclipse.agents.contexts.Images;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;

public class ChatExpandBar implements ExpandListener {
	
	ExpandBar expandBar = null;
	Composite todoComposite= null;
	ExpandItem todoExpand= null;
	Composite fileChangesComposite= null;
	ExpandItem fileChangesExpand = null;
	
	public ChatExpandBar(Composite parent) {
		expandBar = new ExpandBar(parent, SWT.V_SCROLL);
		expandBar.addExpandListener(this);
		expandBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fileChangesComposite = new Composite (expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		fileChangesComposite.setLayout(layout);
		fileChangesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(fileChangesComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(fileChangesComposite, SWT.NONE);
		label.setText("MyFile1.java");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button button = new Button (fileChangesComposite, SWT.PUSH);
		button.setText("Revert");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		label = new Label(fileChangesComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label = new Label(fileChangesComposite, SWT.NONE);
		label.setText("MyFile3.java deleted");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		button = new Button (fileChangesComposite, SWT.PUSH);
		button.setText("Revert");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		label = new Label(fileChangesComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(fileChangesComposite, SWT.NONE);
		label.setText("MyFile3.java moved");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		button = new Button (fileChangesComposite, SWT.PUSH);
		button.setText("Revert");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		fileChangesExpand = new ExpandItem(expandBar, SWT.NONE, 0);
		fileChangesExpand.setText("3 files changed");
		fileChangesExpand.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		fileChangesExpand.setHeight(fileChangesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		fileChangesExpand.setControl(fileChangesComposite);
		
		todoComposite = new Composite (expandBar, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		todoComposite.setLayout(layout);
		todoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(todoComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(todoComposite, SWT.NONE);
		label.setText("Task 1");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label = new Label(todoComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label = new Label(todoComposite, SWT.NONE);
		label.setText("Task 2");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label = new Label(todoComposite, SWT.NONE);
		label.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(todoComposite, SWT.NONE);
		label.setText("Task 3");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		todoExpand = new ExpandItem(expandBar, SWT.NONE, 0);
		todoExpand.setText("Plan (3/3)");
		todoExpand.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		todoExpand.setHeight(todoComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		todoExpand.setControl(todoComposite);
		
		/////////
		///
		///
		
		
//		final int TRIAL_WIDTH = 100;
//		final int trimWidth = fileChangesComposite.computeTrim(0, 0, TRIAL_WIDTH, 100).width - TRIAL_WIDTH;
//		fileChangesComposite.addListener(SWT.Modify, event -> {
//			Point size = fileChangesComposite.computeSize(fileChangesComposite.getSize().x - trimWidth, SWT.DEFAULT);
//			if (fileChangesExpand.getHeight() != size.y) {
//				fileChangesExpand.setHeight(size.y);
//			}
//		});
		
		
		expandBar.addListener(SWT.Resize, event -> Activator.getDisplay().asyncExec(() -> {
			resize();
		}));
		
		expandBar.addExpandListener(this);

		todoExpand.setExpanded(true);
		
		expandBar.setSpacing(8);
		
		
	}

	@Override
	public void itemCollapsed(ExpandEvent e) {
		resize();
	}

	@Override
	public void itemExpanded(ExpandEvent e) {
		resize();
	}
	
	public void resize() {
		Activator.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				expandBar.getParent().layout(true, true);
			}
			
		});
	}

}
