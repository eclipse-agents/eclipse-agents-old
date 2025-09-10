package com.ibm.systemz.db2.mcp;

import java.util.UUID;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mcp.MCPException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.services.IServiceLocator;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ibm.systemz.db2.Activator;
import com.ibm.systemz.db2.Messages;
import com.ibm.systemz.db2.ide.Db2RunQueryJob;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_CONNECTION_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants.E_ON_SUCCESS_OPTIONS;
import com.ibm.systemz.db2.ide.preferences.model.RunningOptions;
import com.ibm.systemz.db2.mcp.tools.properties.IPreferenceConstants;
import com.ibm.systemz.db2.rse.db.queries.QueryModel;


public class Tools implements IPreferenceConstants {


	@McpTool (name = "runSQL", 
			description = "Run a Db2 for z/OS SQL Query", 
			annotations = @McpTool.McpAnnotations(
					title = "Run SQL"))
	public String runSQL(
			@McpToolParam(
					description = "Db2 for z/OS SQL Statement") 
					String sqlStatement) {
		

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
			Db2RunQueryToolJob job = new Db2RunQueryToolJob(UUID.fromString(connectionId), sqlStatement);
			Activator.getDisplay().syncExec(()->{
				job.initialize();
			});
			
			if (job.initialized()) {
				
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
					throw new MCPException(e);
					
				}
				QueryModel model = job.getQueryModel();
				if (model != null) {

					
					try {
						ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
						String result = ow.writeValueAsString(model);
						return result;
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	//				Activator.getDisplay().asyncExec(new Runnable() {
	//					@Override
	//					public void run() {
	//						// update history
	//						summary.getDb2SubSystem().queryHistoryAdded(model);
	//						
	//						// display results
	//						//ConnectionEnvironment.displayExecutionResults(summary.getDb2SubSystem(), model);
	//						//TODO update tables
	//					}
	//				});
				} else {
					throw new MCPException("no result set returned");
				}
			}
		}
		return null;
	}

	
	class Db2RunQueryToolJob extends Db2RunQueryJob {

		String statement;
		public Db2RunQueryToolJob(UUID locationId, String statement) {
			super(locationId);
			this.statement = statement;
		}

		@Override
		protected String getParsedSqlStatement() {
			return statement;
		}

		@Override
		protected IServiceLocator getServiceLocator() {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
		}

		@Override
		protected String getFileName() {
			return null;
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
	};
}
