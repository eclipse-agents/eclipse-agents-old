class SessionPrompt extends DivTemplate {
  markdown;

  constructor() {
    super("session-prompt");
  }

  connectedCallback() {
    // Create and append children to the shadow root
    this.markdown = this.root.querySelector("chunked-markdown");
  }

  addContentBlock(block) {
    this.markdown.addContentBlock(block);
  }
}
customElements.define("session-prompt", SessionPrompt);
