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
function addPrompt(id) {
	console.log("addPrompt", id);
	
	const interaction = addDiv(document.body, id, "interaction");
	
	addDiv(interaction, id, "session_prompt");
	addDiv(interaction, id, "prompt");
	addDiv(interaction, id, "user_message_chunk");
	addDiv(interaction, id, "agent_thought_chunk");
	addDiv(interaction, id, "agent_message_chunk");
}
function setMessage(id, kind, content) {
	console.log("addMessage", id);
	const interaction = document.getElementById(id);
	const lastKind = getLastKindOf(interaction, kind);
	if (lastKind != null) {
		lastKind.innerHTML = marked.parse(content);
		Prism.highlightAll();
	}
}

function addSpan(id, kind, name, url) {
	
}

function setStyle(fontSize, foreground, background) {	
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
}

function addDiv(parent, id, kind) {
	const div = new document.createElement("div");
	div.classList.add(kind);
	div.id = kind + "-" + id;
	parent.appendChild(div);
	return div;
}

function getLastKindOf(parent, kind) {
	const kinds = parent.getElementsByClassName(kind);
	if (kinds != null) {
		return kinds[kinds.length-1];
	}
	return null;
}
