
# Eclipse IDE integrations with Coding Agents over ACP and MCP

**NOTE: This code base is a work in progress and is a pre-release.  It is being shared at this time for communication and co-ordination purposes**

This feature adds an open-source Coding Agent chat experience to Eclipse, built atop two open protocols:

- [Model Context Protocol](https://modelcontextprotocol.io/docs/getting-started/intro)
  - Standardizes communication between agents and external resources, tools and prompts.
- [Agent Client Protocol](https://agentclientprotocol.com/overview/introduction)
  - Standardizes communication between IDEs and coding agents.

## Model Context Protocol services for Eclipse IDE

This feature runs a Model Context Protocol server within the Eclipse IDE VM enabling interactivity between Eclipse based experiences and LLM-powered Agentic experiences running within or outside of the Eclipse IDE.  Features include:

- An MCP server using the [mcp java sdk](https://github.com/modelcontextprotocol/java-sdk) running inside the workbench that can serve over HTTP
- A set of built-in platform services including:
  - Access to workspace, editors, consoles, markers, annotations, …
  - Resource templates with variables and content assist
- An extension point for plugins to contribute MCP tools and resources to the Eclipse MCP server
  - Contribute MCP resources using annotated functions via [MCP Annotations](https://github.com/spring-ai-community/mcp-annotations) and Jackson Annotations
- Centralized preferences, tracing, capabilities

### MCP Integrations

- Eclipse MCP services can be consumed by any MCP client such as:
  - Desktop Apps such as Claude Desktop
  - Terminal CLIs such as Gemini and Claude Code
- IDEs such as Eclipse and VS Code
  - Within Eclipse using Copilot for Eclipse
  - Run a CLI inside an Eclipse terminal
  - The Agent Client Protocol powered chat described below

## Agent Client Protocol (ACP) Services for Eclipse IDE

- The [Agent Client Protocol](https://agentclientprotocol.com/overview/introduction) formalizes an IDE to Coding Agent protocol
  - A "Coding Agent" is a local app, typically a CLI, that can access files, run approved terminal commands, serve as an MCP client, and use LLMs to perform complex tasks as prompted by the end user.
- Existing implementations include:
  - [Editor Implementations](https://github.com/zed-industries/agent-client-protocol#editors)
    - Zed
    - emacs
    - neovim
    - marimo notebook
  - [Agent Implementations](https://github.com/zed-industries/agent-client-protocol#editors)
    - Gemini
    - Claude Code
    - Goose
- Features of the protocol include:
  - Chat Session Lifecycle
  - User Prompt Lifecycle
  - Client can forward list of MCP Servers to Agent
  - Tool Use Confirmation
  - API for read/write file/editor buffers
  - Embedded and referenced resources
  - / # @ commands

![MCP Contexts](org.eclipse.agents.docs/images/protocol.png)

## Screenshots

### Enable the internal MCP Server to run on an HTTP port

![MCP Contexts](org.eclipse.agents.docs/images/mcpPrefs.png)

### Prompt the Coding Agent to write code and run tools

![acp prompt](org.eclipse.agents.docs/images/acp.png)

### Prompt for sample code

![acp code blocks](org.eclipse.agents.docs/images/acpCodeBlock.png)

### Prompt for explanations

![acp code blocks](org.eclipse.agents.docs/images/acpExplain.png)

## Demonstrations

- [Coding Agent developing SQL](https://www.youtube.com/watch?v=z1d67Xzq3UU)
- [HTML and Markdown display, Code Blocks, Resource Links](https://www.youtube.com/watch?v=1G8pt1H27fQ)
- [Claude Conversation with Eclipse MCP: What's wrong with my java project](https://claude.ai/share/31968356-df7e-471b-8fec-3b85868a2376)

## Installation

- Download a recent version of [Eclipse J2EE package](https://www.eclipse.org/downloads/packages/release/2025-09/r/eclipse-ide-enterprise-java-and-web-developers) that has this plugins dependencies built-in.
- Download the [Eclipse Agents plugin](https://ibm.box.com/v/eclipse-agents) and unzip it
- From Eclipse navigate to Help > Install New Software...
- Click "Add..." > "Archive..." and navigate to the org.eclipse.mcp.update folder, then click "Open"
- Uncheck "Group items by category"
- Toggle the checkbox for the feature that shows up in the view
- Click "Next" until "Finish"
- Click "OK" to the security warning and allow the patch to install
- When prompted to restart, click "Yes"

### To connect the built-in MCP server to an external MCP client

- Navigate to preference page "Coding Agents/Agent Contexts"
- Select the "Enable MCP HTTP Server" checkbox and apply the changes.
- Use the "Copy to clipboard" button to copy the MCP's SSE Endpoint to the clipboard.
- Add the HTTP SSE MCP endpoint to your MCP client of choice.

### To use the Coding Agent view with Google Gemini CLI

- Install the [Google Gemini CLI](https://github.com/google-gemini/gemini-cli) locally
- Start up gemini in a terminal and log in using google.
- In Eclipse navigate to preference page "Coding Agents/Agent CLIs".
- Enter in a valid location for your node executable and the gemini cli js file
  - while we await improvements on automatically finding gemini files...
    - use `where node` and `where gemini` in a terminal to find locations
    - you can use quotes if either contains a space
    - on windows you may find the gemini js file somewhere like  
      - C:\Users\<user>\AppData\Roaming\npm\node_modules\@google\gemini-cli\dist\index.js
- Open the Coding Agent view
- Select Coding Agent: Gemini CLI
- Type into the chat input, press enter to submit prompt