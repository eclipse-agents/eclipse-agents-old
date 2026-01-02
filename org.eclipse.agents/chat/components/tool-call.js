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
		this._toolCallContainer = this.root.querySelector('div#toolCall');
        this._div = this.root.querySelector('div div');
		this._kind = this.root.querySelector('div div img#kind');
        this._title = this.root.querySelector('div div span#title');
        this._status = this.root.querySelector('div div img#status');
		this._buttonContainer = this.root.querySelector('div div#permissionButtonContainer');
		
		this._content = this.root.querySelector('div div#contentContainer div#toolCallContent');
		
		this._expandCollapseContent = this.root.querySelector('div button#expandCollapseContentButton');
		this._expandCollapseContentImg = this._expandCollapseContent.querySelector('img');		
		
		this._contentFooterButton = this.root.querySelector('div div button#contentFooterButton');
		this._contentFooterButtonImg = this._contentFooterButton.querySelector('img');
	}

	create(toolCallId, title, kind, status, content, permissionOptions) {
        this._toolCallId = toolCallId;
        this._div.id = toolCallId;
		// make sure spaces are rendered correctly
		this._title.textContent = title.replaceAll(' ', '\u00A0');
		
		this.setKind(kind);
		
		this._expandCollapseContent.addEventListener("click", (event) => this.toggleContent(event));
		this._contentFooterButton.addEventListener("click", (event) => this.collapseContent(event));
		
		if (content != null) {
			// TODO: Currently displaying the raw content.
			// we'll need to render the content based on the type of content provided
			this._content.textContent = content;
			this._expandCollapseContentImg.src = "icons/collapse.png";
			this._contentFooterButtonImg.src = "icons/collapse.png";
		} else {
			this._expandCollapseContent.style.display = "none";
			this._contentFooterButton.style.display = "none";
			this._content.style.display = "none";
		}
		
		if (permissionOptions != null) {
			this._isPermissionRequest = true;
			for(const option of permissionOptions) {
				const permissionRequestButton = addChild(this._buttonContainer, "permission-request-button");
				permissionRequestButton.create(option.kind, option.name, option.optionId, this);
			}
			this._toolCallContainer.classList.add("requestToolCall");
			
			// TODO: Gemini can return empty info for the title.
			// For now, replace it with the toolCallId if it's empty
			if (title === "{}" || title === "" || title == null) {
				this._title.textContent = toolCallId;
			}
		} else {
			this._isPermissionRequest = false;
			this._buttonContainer.style.display = "none";
		}
		
		this.updateStatus(status);
	}

    getToolCallId() {
        return this._toolCallId;
    }
	
	setKind(kind) {
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
	
	updateContent(content) {
		if (this._content.textContent === null || this._content.textContent === "") {
			// expand when receiving first update
			this._content.style.display = "unset";
			this._expandCollapseContentImg.src = "icons/collapse.png";
			this._contentFooterButtonImg.src = "icons/collapse.png";
			this._expandCollapseContent.style.display = "flex";
			this._contentFooterButton.style.display = "unset";	
		}
		
		this._content.textContent = content;
	}
	
	toggleContent(event) {
		if (this._content.style.display === "none") {
			this._content.style.display = "unset";
			this._expandCollapseContentImg.src = "icons/collapse.png";
			this._contentFooterButton.style.display = "unset"
		} else {
			this.collapseContent();
		}
	}

	collapseContent(event) {
		this._content.style.display = "none";
		this._contentFooterButton.style.display = "none";
		this._expandCollapseContentImg.src = "icons/expand.png";
	}
	
	handlePermissionResponse(optionId) {
		this._buttonContainer.remove();
		this.collapseContent();
		// Pass response info to eclipse browser location handler
		window.location = "response:" + this._toolCallId + "/" + optionId;
	}
	
	isPermissionRequest() {
		return this._isPermissionRequest;
	}
}
customElements.define("tool-call", ToolCall);