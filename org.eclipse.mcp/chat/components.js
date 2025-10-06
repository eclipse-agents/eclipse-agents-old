
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

class AgentThoughts extends DivTemplate {
	
	markdown;
	thougts;

	constructor() {
		super("agent-thought-chunk");
		this.thoughts = 0;
	}

	connectedCallback() {
        // Create and append children to the shadow root
		this.markdown = this.root.querySelector('span chunked-markdown');
		this.button = this.root.querySelector('span button');
		this.button.addEventListener("click", function() {
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
		this.button.textContent = "Thoughts Processed (" + ++this.thoughts + ")";
	}
}
customElements.define("agent-thoughts", AgentThoughts);

class AgentMessages extends DivTemplate {
	
	markdown;

	constructor() {
		super("agent-message-chunk");
	}

	connectedCallback() {
        // Create and append children to the shadow root
		this.markdown = this.root.querySelector('chunked-markdown');
	}
	
	addChunk(chunk) {
		this.markdown.addChunk(chunk);
	}
}
customElements.define("agent-messages", AgentMessages);

class ResourceLink extends DivTemplate {
	
	constructor() {
		super("resource-link");
	}

	connectedCallback() {
        this.icon = this.root.querySelector('span i');
		this.link = this.root.querySelector('span a');
	}
	
	setLink(name, url, icon) {
		this.icon.classList.add(icon);
		this.link.setAttribute("href", url);
		this.link.textContent = name;
	}
}
customElements.define("resource-link", ResourceLink);