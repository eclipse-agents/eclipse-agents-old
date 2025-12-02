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

class AgentMessages extends DivTemplate {
	
	markdown;
	anchorElements;

	constructor() {
		super("agent-message-chunk");
	}

	connectedCallback() {
		// Create and append children to the shadow root
		this.markdown = this.root.querySelector('chunked-markdown');
	}

	addContentBlock(block) {
		this.markdown.addContentBlock(block);
		this.anchorElements = this.markdown.querySelectorAll('a');
		this.anchorElements.forEach(anchor => {
			anchor.tabIndex = 0;
		});
	}
}
customElements.define("agent-messages", AgentMessages);