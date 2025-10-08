class AgentMessages extends DivTemplate {
	
	markdown;

	constructor() {
		super("agent-message-chunk");
	}

	connectedCallback() {
        // Create and append children to the shadow root
		this.markdown = this.root.querySelector('chunked-markdown');
	}

	addContentBlock(block) {
		this.markdown.addContentBlock(block);
	}
}
customElements.define("agent-messages", AgentMessages);