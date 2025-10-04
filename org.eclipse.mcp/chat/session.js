// Configure marked to use Prism for syntax highlighting
marked.setOptions({
    highlight: function (code, lang) {
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

// SessionUpdate: "user_message_chunk")"agent_message_chunk")"agent_thought_chunk")"tool_call")"tool_call_update")"plan") "available_commands_update")"current_mode_update")
// ContentBlock "text")"image") "audio") "resource_link")"resource")
function addPromptTurn() {
	console.log("addPrompt");

	document.body.append(document.createElement("div"));
	document.body.lastElementChild.classList.add("prompt_turn");
}

function addMessage(_class, content, isMarkdown, isChunk) {
	if (!isChunk || 
			document.body.lastElementChild.lastElementChild == null || 
			!document.body.lastElementChild.lastElementChild.classList.contains(_class)) {

		document.body.lastElementChild.append(document.createElement("div"));
		document.body.lastElementChild.lastElementChild.classList.add(_class);
	}
	
	if (isMarkdown) {
		document.body.lastElementChild.lastElementChild.innerHTML = marked.parse(content);
		Prism.highlightAll();
	} else {
		document.body.lastElementChild.lastElementChild.textContent = content;
	}
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
}

function setStyle(fontSize, foreground, background) {
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
}

function addChild(parent, kind) {
	const child = document.createElement(kind)
	parent.append(child);
	return child;
}

function demo() {
	addPromptTurn();
	addMessage("session_prompt", "My question is asdf asdf asdf", false, false);
	addResourceLink("File1.txt", "", "resource_link", "fa-file");
	addResourceLink("folderName", "", "resource_link", "fa-folder");
	addMessage("agent_thought_chunk", `**Im Thinking About**
- one thing
- another thing

**Im Also Thinking About**
- one thing
- another thing`, true, true);
	
	addMessage("agent_message_chunk", `Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"`, true, true);
	addMessage("agent_message_chunk", 	`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"
}}
\`\`\`
Anything else?`, true, true);

	addPromptTurn();
	addMessage("session_prompt", "My second question is asdf asdf asdf", false, false);
	addResourceLink("File1.txt", "", "resource_link", "fa-file");
	addResourceLink("folderName", "", "resource_link", "fa-folder");
	addMessage("agent_thought_chunk", `**Im Thinking Now About**
- one thing
- another thing

**Im Also Thinking About one last thing**
- one thing
- another thing`, true, true);
	
	addMessage("agent_message_chunk", `Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"`, true, true);
	addMessage("agent_message_chunk", 	`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"
}}
\`\`\`
Anything else?`, true, true);

}


