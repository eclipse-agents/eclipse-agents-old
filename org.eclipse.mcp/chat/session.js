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
	if (!isChunk || !document.lastElementChild.lastElementChild.classList.contains(_class)) {
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

function addResourceLink(text, url, _class) {
	console.log("addResourceLink", text, url);
	document.body.lastElementChild.append(document.createElement("div"));
	document.body.lastElementChild.lastElementChild.classList.add(_class);
	
	document.body.lastElementChild.lastElementChild.append(document.createElement("a"));
	document.body.lastElementChild.lastElementChild.lastElementChild.classList.add(_class);
	document.body.lastElementChild.lastElementChild.lastElementChild.href = url;
	document.body.lastElementChild.lastElementChild.lastElementChild.textContent = text;
}

function setStyle(fontSize, foreground, background) {
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
}

