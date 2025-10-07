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