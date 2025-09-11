package com.ibm.systemz.db2.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mcp.resource.IResourceTemplate;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;

/**
 * support for resource template: file://workspace/{relativePath}
 */
public class SchemaAdapter implements IResourceTemplate<String, ResourceLink> {
	
	final String template = "db2zos://schema/{schemaName}";
	final String prefix = template.substring(0, template.indexOf("{"));
	
	String schemaName;
	
	public SchemaAdapter() {}

	public SchemaAdapter(String uri) {
		
		DefaultMcpUriTemplateManager relative = new DefaultMcpUriTemplateManager(template);
		if (relative.matches(uri)) {
			Map<String, String> variables = relative.extractVariableValues(uri);
			schemaName = variables.get("schemaName");
		}
	}

	@Override
	public String[] getTemplates() {
		return new String[] { 
			template
		};
	}
	
	@Override
	public SchemaAdapter fromUri(String uri) {
		return new SchemaAdapter(uri);
	}

	@Override
	public SchemaAdapter fromModel(String schemaName) {
		return new SchemaAdapter(prefix + schemaName);
	}


	@Override
	public String getModel() {
		return schemaName;
	}

	@Override
	public ResourceLink toJson() {
		return toResourceLink();
	}

	@Override
	public ResourceLink toResourceLink() {
		McpSchema.ResourceLink.Builder builder =  McpSchema.ResourceLink.builder()
				.uri(toUri())
				.name(schemaName)
				.description("Db2 for z/OS Schema")
				.mimeType("application/json");

		return builder.build();
		
	}

	@Override
	public String toUri() {
		return prefix + schemaName;
	}

	@Override
	public String toContent() {
		
		Tools tools = new Tools();
		return tools.runSQL("SELECT TBCREATOR, TBNAME, NAME FROM SYSIBM.SYSCOLUMNS");
		
	}
}
