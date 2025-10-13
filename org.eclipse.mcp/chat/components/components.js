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
 
class Markdown extends HTMLElement {
  setMarkdown(markdown) {
    this.innerHTML = marked.parse(markdown);
    Prism.highlightAllUnder(this);
  }
}
customElements.define("markdown-div", Markdown, { extends: "div" });

class ChunkedMarkdown extends Markdown {
  constructor() {
    super();
    this.chunks = [];

    this.TEXT = "text";
    this.IMAGE = "image";
    this.AUDIO = "audio";
    this.RESOURCE_LINK = "resource_link";
    this.RESOURCE = "resource";
    this.NONE = "NONE";

    this.lastBlockType = this.NONE;
  }

  addContentBlock(block) {
    if (this.lastBlockType === this.RESOURCE && block.type !== this.RESOURCE) {
      this.chunks.push("\n```\n");
    } else if (this.lastBlockType !== this.RESOURCE && block.type === this.RESOURCE) {
      this.chunks.push("\n```text\n");
    }

    if (block.type === "text") {
      this.addText(block);
    } else if (block.type === "image") {
      this.addImage(block);
    } else if (block.type === "audio") {
      this.addAudio(block);
    } else if (block.type === "resource_link") {
      this.addResourceLink(block);
    } else if (block.type === "resource") {
      this.addResource(block);
    }

    if (block.type  === this.RESOURCE) {
      console.log(this.chunks.join("") + "\n```");
      super.setMarkdown(this.chunks.join("") + "\n```");
    } else {
      console.log(this.chunks.join(""));
      super.setMarkdown(this.chunks.join(""));
    }

    this.lastBlockType = block.type;
  }

  addText(block) {
    this.chunks.push(block.text);
  }

  addImage(block) {
    return;
  }

  addAudio(block) {
    return;
  }

  addResourceLink(block) {
    this.chunks.push(`<span class="resource-link">`);
    let addedImage = false;
    if (getProgramIcon != null) {
      let base64Icon = getProgramIcon(block.uri);
      if (base64Icon != null) {
        this.chunks.push(
          `<img src="` + base64Icon + `"/> `
        );
        addedImage = true;
      }
    }
  
    this.chunks.push(`<a href="` + block.uri + `">` + block.name + `</a>`);
    this.chunks.push(`</span>`);
  }

  addResource(block) {
    //TODO do something with block.resource.uri;

    if (block.resource.text != undefined) {
      this.chunks.push(block.resource.text);
    } else if (block.resource.blob != undefined) {
      this.chunks.push(block.resource.blob);
    }
  }
}
customElements.define("chunked-markdown", ChunkedMarkdown);

class DivTemplate extends HTMLElement {
  constructor(templateId) {
    super();

    let template = document.getElementById(templateId);
    this.root = this.attachShadow({ mode: "open" });
    this.root.appendChild(template.content.cloneNode(true));
  }
}

class PromptTurn extends HTMLElement {}
customElements.define("prompt-turn", PromptTurn, { extends: "div" });
