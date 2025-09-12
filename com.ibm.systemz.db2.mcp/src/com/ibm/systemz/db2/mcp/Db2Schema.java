package com.ibm.systemz.db2.mcp;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Db2Schema {
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Schemas (
			@JsonProperty
			String[] schemas) {
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("Db2 for z/OS Table or View")
	public record Table (
		
		@JsonProperty
		String name,
		
		@JsonProperty
		String schema,
		
		@JsonProperty
		String type,
		
		@JsonProperty
		Column[] columns) {
	}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Tables (
			@JsonProperty
			Table[] table) {
	}
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("Db2 for z/OS Table or View")
	public record Column (
		
		@JsonProperty
		String name,
		
		@JsonProperty
		String dataType,
		
		@JsonProperty
		String length,
		
		@JsonProperty
		String scale,

		@JsonProperty
		String nullable,
		
		@JsonProperty
		String updates,
		
		@JsonProperty
		String defaultValule,
		
		@JsonProperty
		String encodingScheme,
		
		@JsonProperty
		String generatedAttribute) {

	}
	
	
//	
//	StoredProcedure
//	
//	Parameter
//	
//	UserDefinedFunction
}
