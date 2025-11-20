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
package org.eclipse.mcp.platform.resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.mcp.MCPException;
import org.eclipse.mcp.platform.resource.ResourceSchema.Marker;
import org.eclipse.mcp.platform.resource.ResourceSchema.PRIORITY;
import org.eclipse.mcp.platform.resource.ResourceSchema.Problems;
import org.eclipse.mcp.platform.resource.ResourceSchema.SEVERITY;
import org.eclipse.mcp.platform.resource.ResourceSchema.TYPE;
import org.eclipse.mcp.platform.resource.ResourceSchema.Tasks;
import org.eclipse.mcp.resource.IResourceAdapter;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * support for Problem and Task markers
 */
public class MarkerAdapter implements IResourceAdapter<Marker> {
	
	TYPE type;
	String message;
	int charStart;
	int charEnd;
	int lineNumber;
	boolean done;
	String location = "";
	McpSchema.ResourceLink resource_link;
	long id;
	long creationTime;
	SEVERITY severity;
	PRIORITY priority;
	
	public MarkerAdapter(IMarker marker) {
		try {
			processMarker(marker);
			resource_link = new WorkspaceResourceAdapter(marker.getResource()).toResourceLink();
		} catch (Exception e) {
			throw new MCPException(e);
		}
	}
	
	public MarkerAdapter(Annotation annotation, Position position, IDocument document, ITextEditor editor) {
		try {
			String annType = annotation.getType();
		
			if (annotation instanceof SimpleMarkerAnnotation) {
				processMarker(((SimpleMarkerAnnotation)annotation).getMarker());
			} else if ("org.eclipse.ui.workbench.texteditor.error".equals(annType)) {
				type = TYPE.Problem;
				severity = SEVERITY.ERROR;
			} else if ("org.eclipse.ui.workbench.texteditor.warning".equals(annType)) {
				type = TYPE.Problem;
				severity = SEVERITY.WARNING;
			} else if ("org.eclipse.ui.workbench.texteditor.info".equals(annType)) {
				type = TYPE.Problem;
				severity = SEVERITY.INFO;
			} else if ("org.eclipse.ui.workbench.texteditor.task".equals(annType)) {
				type = TYPE.Task;
			} else if ("org.eclipse.ui.workbench.texteditor.bookmark".equals(annType)) {
				type = TYPE.Bookmark;
			} else {
				type = TYPE.Text;
			}

			message = annotation.getText();
			charStart = position.offset;
			charEnd = position.getOffset() + position.getLength();
			lineNumber = document.getLineOfOffset(position.getOffset());
			resource_link = new EditorAdapter().fromEditorName(editor.getTitle()).toResourceLink();
		} catch (Exception e) {
			throw new MCPException(e);
		}
	}

	@Override
	public Marker toJson() {
		return new Marker(
				type,
				message,
				charStart,
				charEnd,
				lineNumber,
				 done,
				location,
				resource_link,
				id,
				creationTime,
				severity,
				priority);
				
	}


	
	private void processMarker(IMarker marker) throws Exception {
		id = marker.getId();
		creationTime = marker.getCreationTime();
		
		Map<?, ?> map = marker.getAttributes();
		
		if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
			type = TYPE.Bookmark;
		} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
			type = TYPE.Problem;
		} else if (marker.isSubtypeOf(IMarker.TASK)) {
			type = TYPE.Task;
		} else if (marker.isSubtypeOf(IMarker.TEXT)) {
			type = TYPE.Text;
		}
		
		Object obj = marker.getAttribute(IMarker.PRIORITY);
		if (obj != null) {
			if (obj.equals(IMarker.PRIORITY_HIGH)) {
				priority = PRIORITY.HIGH;
			} else if (obj.equals(IMarker.PRIORITY_NORMAL)) {
				priority = PRIORITY.NORMAL;
			} else if (obj.equals(IMarker.PRIORITY_LOW)) {
				priority = PRIORITY.LOW;
			}
		}

		obj = marker.getAttribute(IMarker.SEVERITY);
		if (obj != null) {
			if (obj.equals(IMarker.SEVERITY_ERROR)) {
				severity = SEVERITY.ERROR;
			} else if (obj.equals(IMarker.SEVERITY_INFO)) {
				severity = SEVERITY.INFO;
			} else if (obj.equals(IMarker.SEVERITY_WARNING)) {
				severity = SEVERITY.WARNING;
			}
		}

		if (map.containsKey(IMarker.MESSAGE)) {
			message = map.get(IMarker.MESSAGE).toString();
		}

		if (map.containsKey(IMarker.CHAR_START) && 
				map.get(IMarker.CHAR_START) instanceof Integer) {
			charStart = (int)map.get(IMarker.CHAR_START);
		}

		if (map.containsKey(IMarker.CHAR_END) && 
				map.get(IMarker.CHAR_END) instanceof Integer) {
			charEnd = (int)map.get(IMarker.CHAR_END);
		}
		
		if (map.containsKey(IMarker.LINE_NUMBER) && 
				map.get(IMarker.LINE_NUMBER) instanceof Integer) {
			lineNumber = (int)map.get(IMarker.LINE_NUMBER);
		}
		
		if (map.containsKey(IMarker.DONE)) {
			done = "true".equals(map.get(IMarker.DONE).toString());
		}
		
		if (map.containsKey(IMarker.LOCATION) && 
				map.get(IMarker.LOCATION) instanceof String) {
			location = (String)map.get(IMarker.LOCATION);
		}
	}

	public static Problems getProblems(IResource resource) {
		try {
			List<Marker> children = new ArrayList<Marker>();
			for (IMarker marker: resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
	    		MarkerAdapter adapter = new MarkerAdapter(marker);
	    		children.add(adapter.toJson());
			}	
			return new Problems(children.toArray(Marker[]::new));

		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Problems getProblems(ITextEditor editor) {
		List<Marker> children = new ArrayList<Marker>();
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		IAnnotationModel model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			MarkerAdapter adapter = new MarkerAdapter(annotation, model.getPosition(annotation), document, editor);
			Marker marker = adapter.toJson();
			if (TYPE.Problem.equals(marker.type())) {
				children.add(marker);
			}
		}
		
		return new Problems(children.toArray(Marker[]::new));
	}
	
	public static Tasks getTasks(IResource resource) {
		try {
			List<Marker> children = new ArrayList<Marker>();
			for (IMarker marker: resource.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE)) {
	    		MarkerAdapter adapter = new MarkerAdapter(marker);
	    		children.add(adapter.toJson());
			}	
			return new Tasks(children.toArray(Marker[]::new));

		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Tasks getTasks(ITextEditor editor) {
		List<Marker> children = new ArrayList<Marker>();
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		IAnnotationModel model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			MarkerAdapter adapter = new MarkerAdapter(annotation, model.getPosition(annotation), document, editor);
			Marker marker = adapter.toJson();
			if (TYPE.Task.equals(marker.type())) {
				children.add(marker);
			}
		}
		
		return new Tasks(children.toArray(Marker[]::new));
	}
		
}
