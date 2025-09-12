package com.ibm.systemz.db2.mcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mcp.MCPException;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.systemz.db2.mcp.Db2Schema.Column;
import com.ibm.systemz.db2.mcp.Db2Schema.Schemas;
import com.ibm.systemz.db2.mcp.Db2Schema.Table;
import com.ibm.systemz.db2.mcp.Db2Schema.Tables;
import com.ibm.systemz.db2.mcp.tools.properties.IPreferenceConstants;
import com.ibm.systemz.db2.rse.db.queries.QueryModel;
import com.ibm.systemz.db2.rse.db.queries.ResultSet;


public class Tools implements IPreferenceConstants {


	@McpTool (name = "db2RunSQL", 
			description = "Run a Db2 for z/OS SQL Query", 
			annotations = @McpTool.McpAnnotations(
					title = "Run SQL"))
	public String runSQL(
			@McpToolParam(
					description = "Db2 for z/OS SQL Statement") 
					String sqlStatement) {
		
		QueryModel model = Util.instance().runSQL(sqlStatement);
		return Util.instance().queryModelToJson(model);
	
	}
	
	@McpTool (name = "db2ListSchemas", 
			description = "Retrieve Db2 for z/OS schemas", 
			annotations = @McpTool.McpAnnotations(
					title = "Get Db2 Schemas")) 
	public Schemas getDb2Schemas() {
		List<String> schemas = new ArrayList<String>();
		QueryModel model = Util.instance().runSQL("SELECT DISTINCT CREATOR FROM SYSIBM.SYSTABLES");
		if (model.successes == 1) {
			ResultSet set = model.executions.get(0).resultSets[0];
			for (String[] row: set.getRows()) {
				schemas.add(row[0]);
			}
		}
		return new Schemas(schemas.toArray(String[]::new));
		
	}
	
	@McpTool (name = "db2ListTables", 
			description = "Retrieve Db2 for z/OS tables and views", 
			annotations = @McpTool.McpAnnotations(
					title = "Get Db2 Tables")) 
	public Tables getDb2Tables(
			@McpToolParam(
					description = "Db2 for z/OS schema", 
					required = false) 
			String schema) {
		
		List<Table> tables = new ArrayList<Table>();
		
		String schemaClause = (schema == null || schema.isBlank()) 
				? "CURRENT_SCHEMA" : "'" + schema + "'";

		QueryModel columnModel = Util.instance().runSQL("""
SELECT TBCREATOR, TBNAME,
NAME, COLNO,  
CASE
  WHEN SOURCETYPEID <> 0 THEN RTRIM(TYPESCHEMA) CONCAT '.' CONCAT TYPENAME 
  ELSE TYPENAME 
END, 
CASE
  WHEN COLTYPE = 'BLOB' THEN LENGTH2
  WHEN COLTYPE = 'CLOB' THEN LENGTH2
  WHEN COLTYPE = 'DBCLOB' THEN LENGTH2
  WHEN COLTYPE = 'ROWID' THEN LENGTH2
  WHEN SOURCETYPEID = 404 THEN LENGTH2
  WHEN SOURCETYPEID = 408 THEN LENGTH2
  WHEN SOURCETYPEID = 412 THEN LENGTH2
  WHEN SOURCETYPEID = 904 THEN LENGTH2
  ELSE LENGTH 
END, 
SCALE,
CASE WHEN NULLS = 'Y' THEN 'Yes' ELSE 'No' END, 
CASE WHEN UPDATES = 'Y' THEN 'Yes' ELSE 'No' END, 
DEFAULTVALUE, 
TRIM(CASE
  WHEN ENCODING_SCHEME = 'A' THEN 'ASCII'
  WHEN ENCODING_SCHEME = 'E' THEN 'EBCDIC'
  WHEN ENCODING_SCHEME = 'U' THEN 'Unicode'
  ELSE ENCODING_SCHEME 
END), 
TRIM(CASE
  WHEN GENERATED_ATTR = 'A' THEN 'Always'
  WHEN GENERATED_ATTR = 'D' THEN 'Default'
  ELSE GENERATED_ATTR 
END)
FROM SYSIBM.SYSCOLUMNS 
WHERE TBCREATOR = """ + schemaClause + " ORDER BY 1, 2, 4");
	
		if (columnModel.successes == 1) {

			QueryModel tableModel = Util.instance().runSQL("""
SELECT 
  TB1.NAME AS \"Name\", 
  RTRIM(TB1.CREATOR) AS \"Schema\",
  CASE TB1.TYPE
    WHEN 'T' THEN 'Table'
    WHEN 'M' THEN 'Materialized query table'
    WHEN 'A' THEN 'Alias'
    WHEN 'C' THEN 'Clone table'
    WHEN 'D' THEN 'Accelerator-only table'
    WHEN 'G' THEN 'Created global temporary table'
    WHEN 'H' THEN 'History table'
    WHEN 'M' THEN 'Materialized query table'
    WHEN 'R' THEN 'Archive table'
    WHEN 'X' THEN 'Auxiliary table'
    WHEN 'V' THEN 'View'
    ELSE 'Table' 
  END AS \"Type\" 
  FROM SYSIBM.SYSTABLES TB1
  WHERE TB1.TYPE NOT IN ('P', 'X')
  AND TB1.CREATOR = """ + schemaClause + " ORDER BY 2, 1");
		
			if (tableModel.successes == 1) {
				Map<String, List<Column>> columns = new HashMap<String, List<Column>>();
				
				for (String[] row: columnModel.executions.get(0).resultSets[0].getRows()) {
					String schemaTable = row[0].stripTrailing() + "." + row[1].stripTrailing();
					if (!columns.containsKey(schemaTable)) {
						columns.put(schemaTable, new ArrayList<Column>());
					}
					columns.get(schemaTable).add(
							new Column(row[2], row[4], row[5], row[6], row[7],
									row[8], row[9], row[10], row[11]));
						
				}
				
				for (String[] row: tableModel.executions.get(0).resultSets[0].getRows()) {
					String schemaTable = row[1].stripTrailing() + "." + row[0].stripTrailing();
					tables.add(new Table(row[0], row[1], row[2], columns.get(schemaTable).toArray(Column[]::new)));
				}
			} else {
				Util.instance().queryModelToJson(tableModel);
			}
		} else {
			Util.instance().queryModelToJson(columnModel);
		}

		return new Tables(tables.toArray(Table[]::new));
	}
}
