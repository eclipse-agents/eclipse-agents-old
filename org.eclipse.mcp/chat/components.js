
class Markdown extends HTMLElement {

  setMarkdown(markdown) {
	this.innerHTML = marked.parse(markdown);
	Prism.highlightAll();
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
	Prism.highlightAll();
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

class AgentThoughts extends DivTemplate {
	
	markdown;

	constructor() {
		super("agent-thought-chunk");
	}

	connectedCallback() {
        // Create and append children to the shadow root
		this.markdown = this.root.querySelector('span chunked-markdown');
		this.root.querySelector('span button').addEventListener("click", function() {
			this.classList.toggle("active");
			let content = this.nextElementSibling;
			if (content.style.maxHeight) {
				content.style.maxHeight = null;
			} else {
				content.style.maxHeight = content.scrollHeight + "px";
				content.style.display = "block";
			}
		});
	}
	
	addChunk(chunk) {
		this.markdown.addChunk(chunk);
	}
}
customElements.define("agent-thoughts", AgentThoughts);

class AgentMessages extends DivTemplate {
	
	markdown;

	constructor() {
		super("agent-message-chunk");
		markdown = querySelector('chunked-markdown');
	}
	
	addChunk(chunk) {
		markdown.addChunk(chunk);
	}
}
customElements.define("agent-messages", AgentMessages);