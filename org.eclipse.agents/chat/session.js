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

// Configure marked to use Prism for syntax highlighting
marked.setOptions({
	highlight: function(code, lang) {
		if (Prism.languages[lang]) {
			return Prism.highlight(
				code,
				Prism.languages[lang],
				lang,
			);
		} else {
			return code;
		}
	},
});

const _prompt_turn = "prompt_turn";
const _session_prompt = "session_prompt";
const _user_message_chunk = "user_message_chunk";
const _agent_thought_chunk = "agent_thought_chunk";
const _agent_message_chunk = "agent_message_chunk";

const session_prompt = "session-prompt";
const user_thoughts = "user-thoughts";
const agent_thoughts = "agent-thoughts";
const agent_messages= "agent-messages";
const tool_call= "tool-call";
const permission_request = "permission-request";



function acceptPromptRequest(promptRequest) {
	addChild(document.body, "prompt-turn");
	const json = JSON.parse(promptRequest);
	addChild(getTurn(), session_prompt);
	for (let block of json.prompt) {
		getTurnMessage().addContentBlock(block);
	}
	scrollToBottom();
}

function acceptSessionUserMessageChunk(blockChunk) {
	if (getTurnMessage() == null || getTurnMessage().tagName.toLowerCase() !== user_messages) {
		addChild(getTurn(), user_messages);
	}
	getTurnMessage().addContentBlock(JSON.parse(blockChunk));
	scrollToBottom();	
}

function acceptSessionAgentThoughtChunk(blockChunk) {
	if (getTurnMessage() == null || getTurnMessage().tagName.toLowerCase() !== agent_thoughts) {
		addChild(getTurn(), agent_thoughts);
	}
	getTurnMessage().addContentBlock(JSON.parse(blockChunk));
	scrollToBottom();
}

function acceptSessionAgentMessageChunk(blockChunk) {
	if (getTurnMessage() == null || getTurnMessage().tagName.toLowerCase() !== agent_messages) {
		addChild(getTurn(), agent_messages);
	}
	getTurnMessage().addContentBlock(JSON.parse(blockChunk));
	scrollToBottom();
}

function acceptSessionToolCall(toolCallId, title, kind, status, content, options) {
	addChild(getTurn(), tool_call).id = toolCallId;
	let jsonOptions;
	if (options != null) {
		jsonOptions = JSON.parse(options);
	}
	getTurnMessage().create(toolCallId, title, kind, status, content, jsonOptions);
	scrollToBottom();
}

function acceptSessionToolCallUpdate(toolCallId, status, content) {
	const toolCall = getTurn().querySelector('tool-call#' + toolCallId);
	
	if (toolCall != null) {
		toolCall.updateStatus(status);
		if (content != null) {
			const contentJson = JSON.parse(content);
			// don't update content if an empty content array is received
			if(contentJson.length > 0) {
				toolCall.updateContent(content);
			}
		}
		scrollToBottom();
	}
}

function acceptPermissionRequest(toolCallId, options, title, input, output) {
	addChild(getTurn(), permission_request).id = toolCallId;
	const jsonOptions = JSON.parse(options);
	const permissionRequest = getTurnMessage().create(toolCallId, jsonOptions, title, input, output);
	scrollToBottom();
	return permissionRequest;
}


function setStyle(fontSize, foreground, background, link, linkActive, infoFg, infoBg) {
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
	
	const root = document.documentElement; // For global CSS variables
	root.style.setProperty('--link_fg', link);
	root.style.setProperty('--link_active_fg', linkActive);
	root.style.setProperty('--info_fg', infoFg);
	root.style.setProperty('--info_bg', infoBg);
	
}

function getTurn() {
	return document.body.lastElementChild
}

function getTurnMessage() {
	return getTurn().lastElementChild;
}

function addChild(parent, kind) {
	const child = document.createElement(kind)
	parent.append(child);
	return child;
}

function clearContents() {
	
	const body = document.body;
	const children = Array.from(body.children);
	const tagsToKeep = ['script', 'template'];
	children.forEach(element => {
		const tagName = element.tagName.toLowerCase();
 	   	if (!tagsToKeep.includes(tagName)) {
    		element.remove();
    	}
  	});
}

function scrollToBottom() {
	window.scrollTo(0, document.body.scrollHeight);
}

function mimeToMarkdownCodeBlock(mimeType) {
    
    let language = "text";
    
    if (mimeType != null) {
        if (mimeType.startsWith("application/x-")) {
            language = mimeType.substring("application/x-".length);
        } else if (mimeType.startsWith("text/x-")) {
            language = mimeType.substring("text/x-".length);
        } else if (mimeType.startsWith("text/")) {
            language = mimeType.substring("text/".length);
        } else if (mimeType.startsWith("application/")) {
            language = mimeType.substring("application/".length);
        }
    }

	if (Prism.languages[language] == null) {
		language = "text";
	}

    return language;
}


// From org.eclipse.swt.SWT.java

const TRAVERSE_NONE = 0;
const TRAVERSE_ESCAPE = 1 << 1;
const TRAVERSE_RETURN = 1 << 2;
const TRAVERSE_TAB_PREVIOUS = 1 << 3;
const TRAVERSE_TAB_NEXT = 1 << 4;
const TRAVERSE_ARROW_PREVIOUS = 1 << 5;
const TRAVERSE_ARROW_NEXT = 1 << 6;
const TRAVERSE_MNEMONIC = 1 << 7;
const TRAVERSE_PAGE_PREVIOUS = 1 << 8;
const TRAVERSE_PAGE_NEXT = 1 << 9;

const ALT = 1 << 16;
const SHIFT = 1 << 17;
const CTRL = 1 << 18;
const CONTROL = CTRL;
const COMMAND = 1 << 22;

// TODO: Keyboard accessibility support
function keyTraversed(traverseEvent) {
	const e = JSON.parse(traverseEvent);
	e.character
	e.detail;
	e.doit;
	e.keyCode;
	e.keyLocation;
	e.stateMask;
	e.time;
	
	console.log(e);
	return e.doit;
}
