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