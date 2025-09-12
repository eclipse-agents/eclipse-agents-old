package com.ibm.systemz.db2.mcp;

import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mcp.MCPException;
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
import com.ibm.systemz.db2.mcp.tools.properties.IPreferenceConstants;
import com.ibm.systemz.db2.rse.db.queries.QueryModel;

public class Util implements IPreferenceConstants {

	private static Util instance = new Util();
	
	public static Util instance() {
		return instance;
	}
	
	private Util() {}
	
	public QueryModel runSQL(String sqlStatement) {
		
		String connectionId = Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID);
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
				
			connectionId = Activator.getDefault().getPreferenceStore().getString(P_CONNECTIONID);
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
