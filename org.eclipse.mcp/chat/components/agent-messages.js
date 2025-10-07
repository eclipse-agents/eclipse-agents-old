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