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
const agent_thoughts = "agent-thoughts";
const agent_messages= "agent-messages";

// SessionUpdate: "user_message_chunk")"agent_message_chunk")"agent_thought_chunk")"tool_call")"tool_call_update")"plan") "available_commands_update")"current_mode_update")
// ContentBlock "text")"image") "audio") "resource_link")"resource")
function addPromptTurn() {
	console.log("addPromptTurn");
	addChild(document.body, "prompt-turn");
}

function addSessionPrompt(content) {
	addChild(getTurn(), session_prompt);
	getTurnMessage().textContent = content;
	
	scrollToBottom();
}

function addUserMessageChunk() {

}

function addAgentThoughtChunk(content) {
	if (getTurnMessage() == null || getTurnMessage().tagName.toLowerCase() !== agent_thoughts) {
		addChild(getTurn(), agent_thoughts);
	}

	getTurnMessage().addChunk(content);	
	scrollToBottom();
}

function addAgentMessageChunk(content) {
	if (getTurnMessage() == null || getTurnMessage().tagName.toLowerCase() !== agent_messages) {
		addChild(getTurn(), agent_messages);
	}

	getTurnMessage().addChunk(content);	
	scrollToBottom();
}



function addResourceLink(text, url, icon) {
	console.log("addResourceLink", text, url, icon);

	const link = addChild(document.body.lastElementChild, "resource-link");
	link.setLink(text, url, icon);
					  
	scrollToBottom();
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

function scrollToBottom() {
	window.scrollTo(0, document.body.scrollHeight);
}

function demo() {
	setStyle(`13px`, `rgb(238, 238, 238)`, `rgb(52, 57, 61)`, `rgb(111, 197, 238)`, `rgb(138, 201, 242)`, `rgb(238, 238, 238)`, `rgb(81, 86, 88)`);
	addPromptTurn();
	
	addSessionPrompt("My question is asdf asdf asdf");
	addResourceLink("File1.txt", "", "fa-file");
	addResourceLink("folderName", "", "fa-folder");
	addAgentThoughtChunk(`**Im Thinking About**
- one thing
- another thing`);

	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentMessageChunk(`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"`
);
	addAgentMessageChunk(`
}}
\`\`\`
Anything else?`);

	addPromptTurn();
		addSessionPrompt("My question is asdf asdf asdf");
	//addResourceLink("File1.txt", "", "resource_link", "fa-file");
	//addResourceLink("folderName", "", "resource_link", "fa-folder");
	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentMessageChunk(`Here is what i also came up with:
\`\`\`json
{ "a": {
	"B": "C"`
);
	addAgentMessageChunk(`
}}
\`\`\`
Anything else?`);
}

function updateSession(sessionUpdateJson) {
	const sessionUpdate = JSON.parse(sessionUpdateJson);
	switch(sessionUpdate.sessionUpdate) {
		case "user_message_chunk":
			processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
			break;
		case "agent_message_chunk":
			processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
			break; 
		case "agent_thought_chunk":
			processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
			break;
		case "tool_call":
			break;
		case "tool_call_update":
			break;
		case "plan":
			break;
		case "available_commands_update":
			break;
		case "current_mode_update":
			break;
	}
	scrollToBottom();
}

function processSessionChunk(sessionUpdate, content) {
	
	switch (content.type) {
		case "text":
			processText(sessionUpdate, content);
			break;
		case "image":
			processImages(sessionUpdate, content);
			break;
		case "audio":
			processAudio(sessionUpdate, content);
			break;
		case "resource_link":
			processResourceLink(sessionUpdate, content);
			break;
		case "resource":
			processResource(sessionUpdate, content);
			break;
	}
}

function processText(sessionUpdate, content) {
	switch(sessionUpdate.sessionUpdate) {
	case "user_message_chunk":
		processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
		break;
	case "agent_message_chunk":
		processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
		break; 
	case "agent_thought_chunk":
		processSessionChunk(sessionUpdate.sessionUpdate, sessionUpdate.content);
		break;
	default:
		break;
	}
}
		
function processImages(sessionUpdate, content) {
	
}
			
function processAudio(sessionUpdate, content) {
	
}
	
function processResourceLink(sessionUpdate, content) {
	
}
			
function processResource(sessionUpdate, content) {
	
}
