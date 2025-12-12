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

class PermissionRequest extends DivTemplate {

	constructor() {
		super("permission-request");
	}

	connectedCallback() {
		this._title = this.root.querySelector('div div span#permissionRequestTitle');
		this._buttonContainer = this.root.querySelector('div span#permissionButtonContainer');
		this._input = this.root.querySelector('div chunked-markdown#permissionRequestInput');
		this._output = this.root.querySelector('div chunked-markdown#permissionRequestOutput');
		
		this._expandCollapseInput = this.root.querySelector('div button#expandCollapseInputButton');
		this._expandCollapseInputImg = this._expandCollapseInput.querySelector('img');
		this._expandCollapseInput.addEventListener("click", (event) => this.toggleInput(event));
		
		this._inputFooterButton = this.root.querySelector('div div button#inputFooterButton');
		this._inputFooterButtonImg = this._inputFooterButton.querySelector('img');
		this._inputFooterButton.addEventListener("click", (event) => this.collapseInput(event));
		
		this._expandCollapseOutput = this.root.querySelector('div button#expandCollapseOutputButton');
		this._expandCollapseOutputImg = this._expandCollapseOutput.querySelector('img');
		this._expandCollapseOutput.addEventListener("click", (event) => this.toggleOutput(event));
		
		this._outputFooterButton = this.root.querySelector('div div button#outputFooterButton');
		this._outputFooterButtonImg = this._outputFooterButton.querySelector('img');
		this._outputFooterButton.addEventListener("click", (event) => this.collapseOutput(event));
	}

	create(toolCallId, options, title, input, output) {
		this._toolCallId = toolCallId;
		this._buttonContainer.id = toolCallId;
		// make sure spaces are rendered correctly
		this._title.textContent = title.replaceAll(' ', '\u00A0');
		
		for(const option of options) {
			const permissionRequestButton = addChild(this._buttonContainer, "permission-request-button");
			permissionRequestButton.create(option.kind, option.name, option.optionId, this);
		}
		
		if (input != null) {
			// TODO: using "```json" in the prototype, but this may be handled a lot differently in the final version 
			const inputBlock= {"text":"```json\n" + input + "\n```","type":"text"};
			this._input.addContentBlock(inputBlock);
			this._expandCollapseInputImg.src = "icons/collapse.png";
			this._inputFooterButtonImg.src = "icons/collapse.png";
		} else {
			this._expandCollapseInput.style.display = "none";
			this._inputFooterButton.style.display = "none";
		}
		
		if (output != null) {
			// TODO: using "```json" in the prototype, but this may be handled a lot differently in the final version
			const outputBlock = {"text":"```json\n" + output + "\n```","type":"text"};
			this._output.addContentBlock(outputBlock);
			this._expandCollapseOutputImg.src = "icons/collapse.png";
			this._outputFooterButtonImg.src = "icons/collapse.png";
		} else {
			this._expandCollapseOutput.style.display = "none";
			this._outputFooterButton.style.display = "none";
		}
		
		return this;
	}
	
	updateInput(input) {
		// TODO: using "```json" in the prototype, but this may be handled a lot differently in the final version 
		const inputBlock= {"text":"```json\n" + input + "\n```","type":"text"};
		this._input.addContentBlock(inputBlock);
		this._input.style.display = "unset";
		this._expandCollapseInputImg.src = "icons/collapse.png";
		this._inputFooterButtonImg.src = "icons/collapse.png";
		this._expandCollapseInput.style.display = "flex";
		this._inputFooterButton.style.display = "unset";
	}
	
	updateOutput(output) {
		// TODO: using "```json" in the prototype, but this may be handled a lot differently in the final version
		const outputBlock = {"text":"```json\n" + output + "\n```","type":"text"};
		this._output.addContentBlock(outputBlock);
		this._output.style.display = "unset";
		this._expandCollapseOutputImg.src = "icons/collapse.png";
		this._outputFooterButtonImg.src = "icons/collapse.png";
		this._expandCollapseOutput.style.display = "flex";
		this._outputFooterButton.style.display = "unset";
	}

	getToolCallId() {
		return this._toolCallId;
	}
	
	updateStatus(status) {
		// TODO: Update the status image based on status that is passed in
	}
	
	handleClick() {
		this._buttonContainer.remove();
		this.collapseInput();
		this.collapseOutput();
		// TODO: handle API call
	}
	
	toggleInput(event) {
		if (this._input.style.display == "none") {
			this._input.style.display = "unset";
			this._expandCollapseInputImg.src = "icons/collapse.png";
			this._inputFooterButton.style.display = "unset"
		} else {
			this.collapseInput();
		}
	}
	
	collapseInput(event) {
		this._input.style.display = "none";
		this._inputFooterButton.style.display = "none";
		this._expandCollapseInputImg.src = "icons/expand.png";
	}
	
	toggleOutput(event) {
		if (this._output.style.display == "none") {
			this._output.style.display = "unset";
			this._expandCollapseOutputImg.src = "icons/collapse.png";
			this._outputFooterButton.style.display = "unset"
		} else {
			this.collapseOutput()
		}
	}
	
	collapseOutput(event) {
		this._output.style.display = "none";
		this._outputFooterButton.style.display = "none";
		this._expandCollapseOutputImg.src = "icons/expand.png";
	}
}
customElements.define("permission-request", PermissionRequest);