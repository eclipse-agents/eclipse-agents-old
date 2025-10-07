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

