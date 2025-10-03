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

function addMessage(id, clazz, content) {
	console.log("addMessage", id);
	const newDiv = document.createElement("div");
    newDiv.innerHTML = marked.parse(content);
    newDiv.id = id;
    newDiv.classList.add(clazz);
    document.body.appendChild(newDiv);
    Prism.highlightAll();
}

function updateMessage(id, content) {
    console.log("updateMessage", id);
    document.getElementById(id).innerHTML = marked.parse(content);
    Prism.highlightAll();
}

function setStyle(fontSize, foreground, background) {	
	document.body.style.color = foreground;
	document.body.style.backgroundColor = background;
	document.body.style.fontSize = fontSize;
}
