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

class PermissionRequestButton extends DivTemplate {
		constructor() {
			super("permission-request-button");
		}

		connectedCallback() {
			this._div = this.root.querySelector('div');
			this._kind = this.root.querySelector('div img#kind')
			this._name = this.root.querySelector('div span#name');
		}

		create(kind, name, optionId, toolCall) {
			this._optionId= optionId;
			this._name.textContent = name;
			this.root.querySelector('button').addEventListener("click", function(){
				toolCall.handlePermissionResponse(optionId);
			});
			// handle when agent returns integer or string as kind
			switch(kind) {
				case "allow_once":
				case 0:
					this._kind.src = "icons/check.png";
					break;
				case "allow_always":
				case 1:
					this._kind.src = "icons/check_all.png";
					break;
				case "reject_once":
				case 2:
					this._kind.src = "icons/remove.png";
					break;
				case "reject_always":
				case 3:
					this._kind.src = "icons/remove_all.gif";
					break;
			}
		}
	}
	customElements.define("permission-request-button", PermissionRequestButton);