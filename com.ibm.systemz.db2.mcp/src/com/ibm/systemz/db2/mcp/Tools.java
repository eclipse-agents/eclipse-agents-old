package com.ibm.systemz.db2.mcp;

import java.util.UUID;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.services.IServiceLocator;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.systemz.db2.Activator;
import com.ibm.systemz.db2.Messages;
import com.ibm.systemz.db2.ide.ConnectionEnvironment;
import com.ibm.systemz.db2.ide.ConnectionSummary;
import com.ibm.systemz.db2.ide.RunQueryJobFactory;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_CONNECTION_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_ON_SUCCESS_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.model.RunningOptions;
import com.ibm.systemz.db2.mcp.tools.properties.IPreferenceConstants;
import com.ibm.systemz.db2.rse.db.model.LocationModel;
import com.ibm.systemz.db2.rse.db.queries.QueryModel;
import com.ibm.systemz.db2.rse.db.queries.ResultSet;


public class Tools implements IPreferenceConstants {


	@McpTool (name = "runSQL", 
			description = "Run a Db2 for z/OS SQL Query", 
			annotations = @McpTool.McpAnnotations(
					title = "Run SQL"))
	public String runSQL(
			@McpToolParam(
					description = "Db2 for z/OS SQL Statement") 
					String sqlStatement) {
		

		RunQueryJobFactory factory = new RunQueryJobFactory() {

			@Override
			protected LocationModel getLocationModel() {
				String connectionId = Activator.getInstance().getPreferenceStore().getString(P_CONNECTIONID);
				if (connectionId == null || connectionId.isBlank()) {
					
					//TODO use MCP elicitation if available
					
					
						Activator.getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openError(Activator.getDisplay().getActiveShell(), Messages.RunSQLAction_ErrorDialogTitle, Messages.RunSQLAction_NoActiveConnection);
								
								PreferencesUtil.createPreferenceDialogOn(
										Activator.getDisplay().getActiveShell(),
										"com.ibm.systemz.db2.mcp.tools.properties.Db2zOSMcpProperties", //$NON-NLS-1$
										new String[] {
												"com.ibm.systemz.db2.mcp.tools.properties.Db2zOSMcpProperties" //$NON-NLS-1$
										},
										null).open();
							}
						});
						
						connectionId = Activator.getInstance().getPreferenceStore().getString(P_CONNECTIONID);
				}
				
				if (connectionId != null && !connectionId.isBlank()) {
					ConnectionSummary summary = ConnectionEnvironment.getConnectionSummary(UUID.fromString(connectionId));
					return new LocationModel(summary.getDb2SubSystem(), summary.getId());
				}
				return null;
			}

			@Override
			protected String getParsedSqlStatement() {
				return sqlStatement;
			}

			@Override
			protected RunningOptions getRunningOptions() {
				RunningOptions options = new RunningOptions();
				options.setConnectionOption(E_CONNECTION_OPTIONS.UseNewConnection.toString());
				
				if ("true".equals(Activator.getInstance().getPreferenceStore().getString(P_ENABLEWRITES))) {
					options.setSuccessOption(E_ON_SUCCESS_OPTIONS.CommitOnEachStmt.toString());
				} else {
					options.setSuccessOption(E_ON_SUCCESS_OPTIONS.RollBackOnComplete.toString());
				}
				return options;
			}

			@Override
			protected IServiceLocator getServiceLocator() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
			}

			@Override
			protected String getFileName() {
				//TODO enable save to history
				return null;
			}
		};
		
		try {
			factory.create().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final QueryModel model = factory.getQueryModel();
		if (model != null) {
//			final ConnectionSummary summary = ConnectionEnvironment.getConnectionSummary(model.locationId);
			
//			Activator.getDisplay().asyncExec(new Runnable() {
//				@Override
//				public void run() {
//					// update history
//					summary.getDb2SubSystem().queryHistoryAdded(model);
//					
//					// display results
//					//ConnectionEnvironment.displayExecutionResults(summary.getDb2SubSystem(), model);
//					//TODO update tables
//				}
//			});
			
			ResultSet rs = model.executions.get(0).resultSets[0];
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = mapper.createObjectNode();
			ArrayNode arrayNode = rootNode.putArray("sqlResults");
			

			for (String[] row: rs.getRows()) {
				ObjectNode childNode = mapper.createObjectNode();
				for (int i = 0; i < rs.colCount; i++) {
					childNode.put(rs.columnNames[i], row[i]);
				}
				arrayNode.add(childNode);
			}
			
			return rootNode.toString();
		}
		
		return null;
		
	}
}
