# Eclipse Plug-In Developer Extension for Model Context Protocol Services

The [org.eclipse.mcp.modelContextProtocolServer extension point](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/modelContextProtocolServer.html) can be used to declare and instantiate Model Context Protocol (MCP) services such as Resources, Tools and Prompts.  They run on a single MCP server that runs within Eclipse IDE's VM.  This enables interactivity between Eclipse based experiences and LLM-powered Agentic experiences running within or outside of the Eclipse IDE.

It provides a simple mechanism to contribute MCP Tools and Resources to an MCP server running inside Eclipse.

Uses [MCP Annotations](https://github.com/spring-ai-community/mcp-annotations) to annotate MCP java contributions.

These annotations can abstract away the underlying usage of [modelcontextprotocol/java-sdk](https://github.com/modelcontextprotocol/java-sdk), though elements from the sdk are still available.

Provides a centralized location for users customize Eclipse MCP capabilities and preferences.

The built-in MCP Server makes available a suite MCP resources, templates and tools accessing the Eclipse platform, enabling agents to interact with your Eclipse IDEs workspace files, editors, consoles, problems and tasks.

## Documentation

- [Extension Point Documentation](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/modelContextProtocolServer.html)
- [Plugin Java Docs](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/javadocs/org/eclipse/mcp/package-summary.html)
- Summaries of Platform MCP contributions:
  - [Platform MCP Tools](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/javadocs/org/eclipse/mcp/platform/Tools.html)
  - [Platform Resource Templates and Template Completions](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/javadocs/org/eclipse/mcp/platform/ResaourceTemplates.html)
  - [Platform Resource Controller](https://eclipse-agents.github.io/eclipse-agents/org.eclipse.agents.docs/javadocs/org/eclipse/mcp/platform/ResourceController.html)

To expose an aspect of your IDE plugin as an MCP tool, do the following:

## Getting Started:  Lets create an MCP Tool using an Annotated method

1. Add plugins `org.eclipse.mcp` and `io.modelcontextprotocol` as dependencies to your plugin
2. On you project's Java Compiler preference page, check the option "Store information about method parameters (usable via reflection).
3. Create a method that uses [MCP Annotations](https://github.com/spring-ai-community/mcp-annotations) to annotate MCP tool contribution.  An example from the platform package is:

```java
	@McpTool (name = "openEditor", 
			description = "open an Eclipse IDE editor on a file URI and set an initial text selection", 
			annotations = @McpTool.McpAnnotations(
					title = "Open Editor"))
	public Editor openEditor(
			@McpToolParam(
					description = "Eclipse workspace file uri") 
					String fileUri,
			@McpToolParam(
					description = "offset of the text selection", 
					required = false) 
					int selectionOffset,
			@McpToolParam(
					description = "length of the text selection", 
					required = false) 
					int selectionLength) {

            // implementation
          }
}
```

These annotations in combination with Jackson JSON annotations on the input and output parameters are used to create the MCP tool spec shared with MCP clients:

```json
    {
      "name": "openEditor",
      "description": "open an Eclipse IDE editor on a file URI and set an initial text selection",
      "inputSchema": {
        "type": "object",
        "properties": {
          "fileUri": {
            "type": "string",
            "description": "Eclipse workspace file uri"
          },
          "selectionOffset": {
            "type": "integer",
            "format": "int32",
            "description": "offset of the text selection"
          },
          "selectionLength": {
            "type": "integer",
            "format": "int32",
            "description": "length of the text selection"
          }
        },
        "required": [
          "fileUri"
        ]
      },
      "outputSchema": {
        "type": "object",
        "properties": {
          "editor": {
            "$ref": "#/$defs/ResourceLink"
          },
          "file": {
            "$ref": "#/$defs/ResourceLink"
          },
          "isActive": {
            "type": "boolean"
          },
          "isDirty": {
            "type": "boolean"
          },
          "name": {
            "type": "string"
          }
        },
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "$defs": {
          "ResourceLink": {
            "type": "object",
            "properties": {
              "annotations": {
                ...
              },
              "description": {
                "type": "string"
              },
              "meta": {
                "type": "object"
              },
              "mimeType": {
                "type": "string"
              },
              "name": {
                "type": "string"
              },
              "size": {
                "type": "integer",
                "format": "int64"
              },
              "title": {
                "type": "string"
              },
              "uri": {
                "type": "string"
              }
            }
          }
        }
      },
      "annotations": {
        "title": "Open Editor",
        ...
      }
    }
```

3. Implement the logic for your tool.
4. Implement a org.eclipse.mcp.IFactoryProvider that returns your tool in method getAnnotatedObjects()

## Declare your tool as an extension in your plugin.xml

1. Using the Plugin Manifest Editor, add to your plugin.xml
    1. Add the 'org.eclipse.mcp.modelContextProtocolServer' extension
    2. Add the the extension a contributor
    3. Add to the contributor a factory
    4. Add an activity
       1. The activity can be used to enable/disable contributions from your extension

Example:

```xml
  <extension
         point="org.eclipse.mcp.modelContextProtocolServer"
         id="foo.com.my.extension.id"
         name="My MCP Extension">

      <contributor
         activityId="foo.com.my.activity.id">
         <factory class = "foo.com.MyToolFactoryProvider"/>
      </contributor>
   </extension>
```

Thats all that is required.  Upon startup, MCP servers will start up and serve content over HTTP for the registered tools.  Calls to tools will be delegated to your instances of IMCPTool

1. The infrastructure will handle generation of JSON input and output schema and conversion between JSON and basic Java types.
2. Use jackson JSON annotations such as @JsonProperty and @JsonPropertyDescription as necessary
3. Register your toolFactory using an org.eclipse.mcp.modelContextProtocolServer extension in your plugin.xml

### References

- [Model Context Protocol](https://www.anthropic.com/news/model-context-protocol)
- [java sdk](https://github.com/modelcontextprotocol/java-sdk)
- [mcp-annotations](https://github.com/spring-ai-community/mcp-annotations)

### Examples of Agentic IDE Tooling

- [Claude Code API Applied to other IDEs](https://github.com/anthropics/claude-code/issues/1234)
- [Windsurf Flow Awareness](https://windsurf.com/blog/windsurf-wave-9-swe-1)
- [The Hidden Algorithms Powering Your Coding Assistant](https://diamantai.substack.com/p/the-hidden-algorithms-powering-your?utm_campaign=post)

### Committers Setup

- Use and Eclipse package with PDE and Git.  I use [Eclipse J2EE package](https://www.eclipse.org/downloads/packages/release/2025-09/r/eclipse-ide-enterprise-java-and-web-developers)
- Add git repo to Git Repositories view
- Import all projects into workspace
- In Preference page Plug-in Development / Target Platform:
  - Select or add the target platform you want to work with
  - Hit Edit button for the target platform
  - In the "Edit Target Definition" dialog, hit Add…
  - Select "Target File" in the Add Content dialog and continue
  - In the Location: field enter: file:///${workspace_loc:/eclipse-mcp}/target-platforms/latest.target
  - Finish changes and apply and close the changes
- Create a new Run Configuration of type Eclipse Application and Run it.
select "Eclipse Agent config targeting current stable Eclipse release"
 