
class Markdown extends HTMLElement {

  setMarkdown(markdown) {
	this.innerHTML = marked.parse(markdown);
	Prism.highlightAllUnder(this);
  }
}
customElements.define("markdown-div", Markdown, { extends: "div" });

class ChunkedMarkdown extends Markdown {

  constructor() {
	super();
	this.chunks = [];
  }
  
  addChunk(chunk) {
	this.chunks.push(chunk);
	super.setMarkdown(this.chunks.join(""));
  }
}
customElements.define("chunked-markdown", ChunkedMarkdown);

class DivTemplate extends HTMLElement {
	
	constructor(templateId) {
		super();
			
		let template = document.getElementById(templateId);
		this.root  = this.attachShadow({ mode: "open" });
		this.root.appendChild(template.content.cloneNode(true));
	}
}

class PromptTurn extends HTMLElement {}
customElements.define("prompt-turn", PromptTurn, { extends: "div" });

class SessionPrompt extends DivTemplate {
	
	markdown;

	constructor() {
		super("session-prompt");
	}
}
customElements.define("session-prompt", SessionPrompt);
