/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
class ToolCall extends DivTemplate {
	
	constructor() {
		super("tool-call");
	}

	connectedCallback() {
        this._div = this.root.querySelector('div');
		this._kind = this.root.querySelector('div img#kind');
        this._title = this.root.querySelector('div span#title');
        this._status = this.root.querySelector('div img#status');
    }

	create(toolCallId, title, kind, status) {
        this._toolCallId = toolCallId;
        this._div.id = toolCallId;
        this._title.textContent = title;
        
        switch(kind) { 
             case "read":
                this._kind.src = "icons/read.png";
                break;
            case "edit":
                this._kind.src = "icons/edit.png";
                break;
            case "delete":
                this._kind.src = "icons/delete.png";
                break;
            case "move":
                this._kind.src = "icons/move.gif";
                break;
            case "search":
                this._kind.src = "icons/search.png";
                break;
            case "execute":
                this._kind.src = "icons/execute.png";
                break;
            case "think":
                this._kind.src = "icons/think.png";
                break;
            case "fetch":
                this._kind.src = "icons/fetch.png";
                break;
            case "switch_mode":
                this._kind.src = "icons/switch_mode.gif";
                break;
            case "other":
                this._kind.src = "icons/other.png";
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
                this._status.src = "icons/in_progress.png";
                break;
             case "completed":
                this._status.src = "icons/completed.png";
                break;
             case "failed":
                this._status.src = "icons/failed.png";
                break;
        }
    }
}
customElements.define("tool-call", ToolCall);