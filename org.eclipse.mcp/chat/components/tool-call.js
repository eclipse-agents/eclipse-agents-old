class ToolCall extends DivTemplate {
	
	constructor() {
		super("tool-call");
	}

	connectedCallback() {
        this._div = this.root.querySelector('div');
		this._kind = this.root.querySelector('div i#kind');
        this._title = this.root.querySelector('div span#title');
        this._status = this.root.querySelector('div i#status');
    }

	create(toolCallId, title, kind, status) {
        this._toolCallId = toolCallId;
        this._div.id = toolCallId;
        this._title.textContent = title;
        
        switch(kind) { 
             case "read":
                this._kind.className = "fa fa-thin fa-glasses";
                break;
            case "edit":
                this._kind.className = "fa fa-thin fa-pen";
                break;
            case "delete":
                this._kind.className = "fa fa-thin fa-trash";
                break;
            case "move":
                this._kind.className = "fa fa-thin fa-arrows-up-down-left-right";
                break;
            case "search":
                this._kind.className = "fa fa-thin fa-magnifying-glass";
                break;
            case "execute":
                this._kind.className = "fa fa-thin fa-rocket";
                break;
            case "think":
                this._kind.className = "fa fa-thin fa-brain";
                break;
            case "fetch":
                this._kind.className = "fa fa-thin fa-download";
                break;
            case "switch_mode":
                this._kind.className = "fa fa-thin fa-arrow-right-arrow-left";
                break;
            case "other":
                this._kind.className = "fa fa-thin fa-toolbox";
                break;
        }
        this.updateStatus(status);
	}

    getToolCallId() {
        return this._toolCallId;
    }

    

    updateStatus(status) {
        switch(status) { 
             case "pending":
                //fall through
             case "in_progress":
                this._status.className = "fa fa-thin fa-spinner";
                break;
             case "completed":
                this._status.className = "fa fa-thin fa-check";
                break;
             case "failed":
                this._status.className = "fa fa-thin fa-xmark";
                break;
        }
    }
}
customElements.define("tool-call", ToolCall);