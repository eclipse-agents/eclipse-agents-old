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

const prompt_turn = "prompt_turn";
const session_prompt = "session_prompt";
const user_message_chunk = "user_message_chunk";
const agent_thought_chunk = "agent_thought_chunk";
const agent_message_chunk = "agent_message_chunk";

// SessionUpdate: "user_message_chunk")"agent_message_chunk")"agent_thought_chunk")"tool_call")"tool_call_update")"plan") "available_commands_update")"current_mode_update")
// ContentBlock "text")"image") "audio") "resource_link")"resource")
function addPromptTurn() {
	console.log("addPrompt");
	addChild(document.body, "div").classList.add(prompt_turn);
}

function addSessionPrompt(content) {
	addChild(getTurn(), "div").classList.add(session_prompt);
	getTurnMessage().textContent = content;
	
	scrollToBottom();
}

function addUserMessageChunk() {

}

function addAgentThoughtChunk(content) {

	if (getTurnMessage() == null || !getTurnMessage().classList.contains(agent_thought_chunk)) {
		addChild(getTurn(), "button").classList.add(agent_thought_chunk);
		getTurnMessage().addEventListener("click", function() {
			this.classList.toggle("active");
			var content = this.nextElementSibling;
			if (content.style.maxHeight) {
				content.style.maxHeight = null;
			} else {
				content.style.maxHeight = content.scrollHeight + "px";
				content.style.display = "block";
			}
		});
		getTurnMessage().textContent = "Thought Process...";

		addChild(getTurn(), "div").classList.add(agent_thought_chunk);
	}

//	const split = content.split("\n");
//	for (i = split.length - 1; i >=0; i--) {
//		if (split[i].trim().length > 0) {
//			getTurnMessage().previousElementSibling.textContent = split[i];
//			break;
//		}
//	}

	getTurnMessage().innerHTML = marked.parse(content);
	Prism.highlightAll();
	
	scrollToBottom();
}

function addAgentMessageChunk(content) {
	if (getTurnMessage() == null || !getTurnMessage().classList.contains(agent_message_chunk)) {
		addChild(getTurn(), "div").classList.add(agent_message_chunk);
	}

	getTurnMessage().innerHTML = marked.parse(content);
	Prism.highlightAll();
	
	scrollToBottom();
}



function addResourceLink(text, url, _class, icon) {
	console.log("addResourceLink", text, url);

	const div = addChild(document.body.lastElementChild, "div");
	div.classList.add(_class);

	const span = addChild(div, "span");
	span.classList.add(_class);

	if (icon != null) {
		const i = addChild(span, "i");
		i.classList.add("fa");
		i.classList.add("fa-thin");
		i.classList.add(icon);
	}

	const a = addChild(span, "a");
	a.classList.add(_class);
	a.href = url;
	a.textContent = text;
	
	scrollToBottom();
}

function setStyle(fontSize, foreground, background) {
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
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
	addPromptTurn();
	
	addSessionPrompt("My question is asdf asdf asdf");
	addResourceLink("File1.txt", "", "resource_link", "fa-file");
	addResourceLink("folderName", "", "resource_link", "fa-folder");
	addAgentThoughtChunk(`**Im Thinking About**
- one thing
- another thing

**Im Also Thinking About**
- one thing
- another thing`);

	addAgentMessageChunk(`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"`
);
	addAgentMessageChunk(`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"
}}
\`\`\`
Anything else?`);

	addPromptTurn();
	addSessionPrompt("My second question is asdf asdf asdf",);
	addResourceLink("File1.txt", "", "resource_link", "fa-file");
	addResourceLink("folderName", "", "resource_link", "fa-folder");
	addAgentThoughtChunk(`**Im Thinking About**
	- one thing
	- another thing

	**Im Also Thinking About**
	- one thing
	- another thing`);

		addAgentMessageChunk(`Here is what i came up with:
	\`\`\`json
	{ "a": {
		"B": "C"`
	);
		addAgentMessageChunk(`Here is what i came up with:
	\`\`\`json
	{ "a": {
		"B": "C"
	}}
	\`\`\`
	Anything else?`);

}


