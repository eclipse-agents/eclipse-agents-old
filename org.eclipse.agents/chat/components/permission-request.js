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
		this._title = this.root.querySelector('div span#permissionRequestTitle');
		this._buttonContainer = this.root.querySelector('div span#permissionButtonContainer');
		this._input = this.root.querySelector('div chunked-markdown#permissionRequestInput');
		this._output = this.root.querySelector('div chunked-markdown#permissionRequestOutput');
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
			const inputBlock= {"text":"```json\n" + input + "\n```","type":"text"};
			this._input.addContentBlock(inputBlock);
		}
		if (output != null) {
			const outputBlock = {"text":"```json\n" + output + "\n```","type":"text"};
			this._output.addContentBlock(outputBlock);
		}
	}

	getToolCallId() {
		return this._toolCallId;
	}
	
	handleClick() {
		this._title.style.borderBottom = 'none';
		this._buttonContainer.remove();
		// TODO handle API call
	}

}
customElements.define("permission-request", PermissionRequest);