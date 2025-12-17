/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.agents.test.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.agents.chat.controller.workspace.WorkspaceController;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public final class WorkspaceControllerTest {

	String[] lines = new String[] {
			"public class HelloWorld {",
			"    public static void main(String[] args) {",
			"        System.out.println(\"Hello, World!\");",
			"    }",
			"}"
	};
	
	String[] changedLines = new String[] {
			"// Class Hello World",
			"public class HelloWorld {",
			"    // Main Method",
			"    public static void main(String[] args) {",
			"        System.out.println(\"Hello, World!!!\");",
			"    }",
			"}"
	};
	
	
	
	String content = String.join("\n", Arrays.asList(lines));
	String modifiedContent = String.join("\n", Arrays.asList(changedLines));
 
	IFile file;
	ITextEditor editor;
	

	@BeforeAll
	public void setup() throws CoreException, IOException {
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		final IProject project = workspace.getRoot().getProject("Project");
		try {
			IWorkspaceRunnable create = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.create(null, null);
					project.open(null);
				}
			};
			workspace.run(create, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		file = project.getFile("HelloWorld.java");

		final File f = new File(file.getFullPath().toOSString());
		System.out.println(f.toURI());
//				 file.getFullPath()

		if (!file.exists()) {
			byte[] bytes = content.getBytes();

			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			file.create(stream, true, null);
			stream.close();
		}
		
		project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IWorkbenchPart part = page.getActivePart();
					part.dispose();

					editor = (ITextEditor)IDE.openEditor(page, file, true);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor) editor;
						textEditor.selectAndReveal(7, 5);
						page.activate(textEditor);
					}
					Map attr = new HashMap();
					attr.put(IMarker.MESSAGE, "There is a problem");
					attr.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

					attr.put(IMarker.CHAR_START, 7);
					attr.put(IMarker.CHAR_END, 12);
					attr.put(IMarker.LINE_NUMBER, 1);

					file.createMarker(IMarker.PROBLEM, attr);

					page.getActivePart();
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	//------------------------
	// READ FILE TESTS
	//------------------------
	
	@Test
	//@DisplayName(absoluteFile3)
	public void testReadFile() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(content, controller.readFromFile(path, null, null));
	}
	
	@Test
	//@DisplayName(absoluteFile3)
	public void testReadFileFirstLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(lines[0], controller.readFromFile(path, 0, 1));
	}
	
	@Test
	public void testReadFileMiddleLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(lines[1], controller.readFromFile(path, 1, 1));
	}
	
	@Test
	public void testReadFileLastLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(lines[4], controller.readFromFile(path, 4, 1));
	}
	
	@Test
	public void testReadFileMiddleLines() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(lines[1] + "\n" + lines[2] + "\n" + lines[3], controller.readFromFile(path, 1, 3));
	}
	
	@Test
	public void testReadFileFromMiddleLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		Path path = (Path)file.getRawLocation();
		testEquals(lines[1] + "\n" + lines[2] + "\n" + lines[3] + "\n" + lines[4], 
				controller.readFromFile(path, 1, null));
	}
	
	//------------------------
	// READ EDITOR TESTS
	//------------------------
		
	@Test
	//@DisplayName(absoluteFile3)
	public void testReadEditor() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(content, controller.readFromEditor(editor, null, null));
	}
	
	@Test
	//@DisplayName(absoluteFile3)
	public void testReadEditorFirstLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(lines[0], controller.readFromEditor(editor, 0, 1));
	}
	
	@Test
	public void testReadEditorMiddleLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(lines[1], controller.readFromEditor(editor, 1, 1));
	}
	
	@Test
	public void testReadEditorLastLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(lines[4], controller.readFromEditor(editor, 4, 1));
	}
	
	@Test
	public void testReadEditorMiddleLines() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(lines[1] + "\n" + lines[2] + "\n" + lines[3], controller.readFromEditor(editor, 1, 3));
	}
	
	@Test
	public void testReadEditorFromMiddleLine() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		testEquals(lines[1] + "\n" + lines[2] + "\n" + lines[3] + "\n" + lines[4], 
				controller.readFromEditor(editor, 1, null));
	}
	
	//------------------------
	// WRITE EDITOR TESTS
	//------------------------
	@Test
	public void testWriteEditor() {
		WorkspaceController controller = new WorkspaceController(UUID.randomUUID().toString());
		controller.writeToEditor(editor, modifiedContent);
		testEquals(modifiedContent, 
				controller.readFromEditor(editor, null, null));
		controller.writeToEditor(editor, content);
	}
		
	
	////////////////
	

	public void testEquals(String left, String right) {
		System.out.println(left + " == " + right);
		Assert.assertEquals(left, right);
	}

	public void testArrayEquals(String message, String[] left, String[] right) {
		System.out.println(message + ":: " + Arrays.toString(left) + " == " + Arrays.toString(right));
		Assert.assertArrayEquals(message, left, right);
	}

}
