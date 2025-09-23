package com.ibm.systemz.mcp.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mcp.MCPException;
import org.eclipse.mcp.platform.resource.ResourceSchema;
import org.eclipse.mcp.platform.resource.ResourceSchema.ElicitationChoice;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.services.IServiceLocator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.systemz.db2.Messages;
import com.ibm.systemz.db2.ide.ConnectionEnvironment;
import com.ibm.systemz.db2.ide.ConnectionSummary;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_CONNECTION_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_ON_SUCCESS_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.model.RunningOptions;
import com.ibm.systemz.db2.ide.services.ExternalServices;
import com.ibm.systemz.db2.ide.services.ExternalServices.RunSqlStatus;
import com.ibm.systemz.db2.rse.db.queries.QueryModel;
import com.ibm.systemz.mcp.db2.tools.properties.IPreferenceConstants;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

public class Util implements IPreferenceConstants {

	private static Util instance = new Util();
	private static String sessionConnectionId = null;
	
	public static Util instance() {
		return instance;
	}
	
	private Util() {}
	
	public QueryModel runSQL(String sqlStatement, McpSyncServerExchange exchange) {
		
		String connectionId = //(sessionConnectionId == null) 
			//	? Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID) 
			//	:
					sessionConnectionId;
		
		if (connectionId == null || connectionId.isBlank()) {
					
			//TODO use MCP elicitation if available
			if (exchange.getClientCapabilities().elicitation() != null) {
				
				List<String> enums = new ArrayList<String>();
				List<String> enumNames = new ArrayList<String>();
				ConnectionEnvironment.getLocationSummaries().forEach(c->{
					enums.add(c.getId().toString());
					enumNames.add(c.getName());
				});
				
				ElicitationChoice selectConnection = new ElicitationChoice(
						"string",
						"Select a Db2 for z/OS Connection",
						"Select a Db2 for z/OS Connection",
						enums.toArray(String[]::new),
						enumNames.toArray(String[]::new),
						enums.getFirst());
				
				ElicitationChoice remember = new ElicitationChoice(
						"string",
						"Remember this selection",
						"Remember this selection",
						new String[] { 
								"session", 
								"always", 
								"ask"},
						new String[] { 
								"Remember for this session", 
								"Remember for this and future sessions",
								"Always ask"},
						"session");
		
				ElicitResult result = null;
				try {
					result = exchange.createElicitation(ElicitRequest.builder()
							.message("select a connection")
							.requestedSchema(ResourceSchema.createEliciationRequestSchema(Map.of(
									"connection", selectConnection,
									"remember", remember
								), new String[] {
									"connection", "remember"
								}))
							.build());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				switch (result.action()) {
				case ACCEPT:
					connectionId = result.content().get("connection").toString();
					
					String rememberResult = result.content().get("remember").toString();
					if (rememberResult.equals("session")) {
						sessionConnectionId = connectionId;
					} else if (rememberResult.equals("always")) {
						Activator.getDefault().getPreferenceStore().setValue(P_CONNECTIONID, connectionId);
					}
					break;
				case CANCEL:
					break;
				case DECLINE:
					break;
				default:
					break;
				
				}

			} else {
				connectionId = Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID);
				if (connectionId == null) {
					Activator.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(Activator.getDisplay().getActiveShell(), Messages.RunSQLAction_ErrorDialogTitle, Messages.RunSQLAction_NoActiveConnection);
							
							PreferencesUtil.createPreferenceDialogOn(
									Activator.getDisplay().getActiveShell(),
									"com.ibm.systemz.mcp.db2.tools.properties.Db2zOSMcpProperties", //$NON-NLS-1$
									new String[] {
											"com.ibm.systemz.mcp.db2.tools.properties.Db2zOSMcpProperties" //$NON-NLS-1$
									},
									null).open();
						}
					});
				}
				connectionId = Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID);
			}
				
//			connectionId = Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID);
		}
		
		if (connectionId != null && !connectionId.isBlank()) {
			ConnectionSummary summary = ConnectionEnvironment.getConnectionSummary(UUID.fromString(connectionId));
			
			RunningOptions runningOptions = new RunningOptions();
			runningOptions.setConnectionOption(E_CONNECTION_OPTIONS.UseNewConnection.toString());
			
			if ("true".equals(Activator.getDefault().getPreferenceStore().getString(P_ENABLEWRITES))) {
				runningOptions.setSuccessOption(E_ON_SUCCESS_OPTIONS.CommitOnEachStmt.toString());
			} else {
				runningOptions.setSuccessOption(E_ON_SUCCESS_OPTIONS.RollBackOnComplete.toString());
			}
			
			IServiceLocator[] serviceLocator = new IServiceLocator[] { null };
			Activator.getDisplay().syncExec(()->{
				serviceLocator[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
			});
			
			IStatus status = ExternalServices.instance().runSqlStatement(summary, sqlStatement, runningOptions, serviceLocator[0]);
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			if (status instanceof RunSqlStatus) {
				RunSqlStatus rsStatus = (RunSqlStatus)status;
				if (rsStatus.getJsonObject() != null) {
					try {
						if (rsStatus.isOK()) {
							return (QueryModel)rsStatus.getJsonObject();
						} else {
							throw new MCPException(mapper.writeValueAsString(new SimpleError(rsStatus)));
						}
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						throw new MCPException(e);
					}
				}
			} 
			throw new MCPException(status);
		}
		
		throw new MCPException("Db2 connection not selected");
	}
	
	public String queryModelToJson(QueryModel model) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String result;
		try {
			result = mapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			throw new MCPException(e);
		}
		
		
		if (model.hasFailures()) {
			throw new MCPException(result);
		} 
		
		return result;
	}
	
	public class SimpleError {
		public String title;
		public String exceptionMessage = null;
		public Object details;
		
		public SimpleError(RunSqlStatus status) {
			super();
			this.title = status.getMessage();
			if (status.getException() != null) {
				this.exceptionMessage = status.getException().getLocalizedMessage();
			}
			this.details = status.getJsonObject();
		}
	}
}
