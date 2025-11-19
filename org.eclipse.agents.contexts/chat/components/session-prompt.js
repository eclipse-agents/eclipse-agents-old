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
