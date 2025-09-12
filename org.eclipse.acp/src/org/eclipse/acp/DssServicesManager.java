/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2023, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;

import com.ibm.db2.core.DssServer;
import com.ibm.db2.server.DssLauncher;
import com.ibm.ftt.common.tracing.Trace;
import com.ibm.systemz.db2.Activator;
import com.ibm.systemz.db2.Messages;
import com.ibm.systemz.db2.Tracer;
import com.ibm.systemz.db2.ide.ConnectionEnvironment;
import com.ibm.systemz.db2.ide.ConnectionSummary;
import com.ibm.systemz.db2.ide.Db2ToolingUnavailableException;
import com.ibm.systemz.db2.ide.preferences.Db2ToolsPortIterator;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants;
import com.ibm.systemz.db2.rse.db.model.LocationGeneralModel;
import com.ibm.systemz.db2.rse.db.model.LocationGeneralModel.AUTH_METHOD;
import com.ibm.systemz.db2.rse.subsystem.Db2ConnectorService;
import com.ibm.systemz.db2.rse.subsystem.Db2SubSystemJob;
import com.ibm.systemz.db2.spsql.debug.DebugTarget;

public class DssServicesManager {

	private final String launcherClass = "com.ibm.db2.server.DssLauncher"; //$NON-NLS-1$
	private Process serverProcess = null;
	private Integer port = null;
	private DssClientThread dssClientThread = null;
	private String dssNoFreePortError = null;
	private List<IStatusListener> statusListeners = new ArrayList<IStatusListener>();
	private static DssServicesManager instance = null;
	private Throwable lastException = null;
	
	private boolean enableRemoteDebug = Boolean.getBoolean("com.ibm.systemz.db2.enableRemoteDebug"); //$NON-NLS-1$
	
	// Prevents two restart jobs from overlapping execution
	private static ISchedulingRule RESTART_RULE = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	private DssServicesManager() {

	}
	
	public static DssServicesManager getInstance() {
		if (instance == null) {
			instance = new DssServicesManager();
		}
		return instance;
	}

	public void start() {
		start(null, null, null);
	}
	
	public void start(Runnable runAfterProxyAvailable, Runnable runIfProxyNotAvailable, Runnable runIfNoPortAvailable) {
		
		Tracer.trace(getClass(), Trace.FINER, "start() begin"); //$NON-NLS-1$
		
		try {
			lastException = null;
			String portRangesString = Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_DB2SQLSERVICEPORT);
			Db2ToolsPortIterator portIterator = new Db2ToolsPortIterator(portRangesString);
			portIterator.resolveAvailablePort();
			port = portIterator.getResolvedPort();
			
			if (port == null) {
				dssNoFreePortError = MessageFormat.format(Messages.Activator_no_free_port, portRangesString);
				Tracer.trace(getClass(), Trace.FINE, MessageFormat.format(Messages.Activator_no_free_port, portRangesString));
				notifyListeners();
				if (runIfNoPortAvailable != null) {
					runIfNoPortAvailable.run();
				}
			} else {
				dssNoFreePortError = null;

				List<String> commandAndArgs = new ArrayList<String>();
				commandAndArgs.add(getJavaExecutable());
				
				if (enableRemoteDebug) {
					commandAndArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:8123"); //$NON-NLS-1$
				}

				commandAndArgs.add(getFileEncoding());
				commandAndArgs.add(getCharacterConversion());
				commandAndArgs.add("-cp"); //$NON-NLS-1$
				commandAndArgs.add(getClassPath());
				commandAndArgs.add(launcherClass);
				commandAndArgs.add("" + getPort()); //$NON-NLS-1$
				commandAndArgs.add(getLogLevel());
				commandAndArgs.add(getLogPath());
				commandAndArgs.add(getMaxHeap());
				
				StringBuffer commandTrace = new StringBuffer();
				for (String s: commandAndArgs) {
					commandTrace.append(s + " "); //$NON-NLS-1$
				}
				Tracer.trace(getClass(), Trace.FINE, commandTrace.toString());
				
				ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
				pb.redirectErrorStream(true);

				dssClientThread = new DssClientThread(getPort(), runAfterProxyAvailable, runIfProxyNotAvailable) {
					@Override
					public void statusChanged() {
						notifyListeners();
					}					
				};

				serverProcess = pb.start();
				
				serverProcess.onExit().thenRun(new Runnable() {
					@Override
					public void run() {
						Tracer.trace(getClass(), Trace.FINE, "DSS Server process has exited with exit value: " + serverProcess.exitValue()); //$NON-NLS-1$
						notifyListeners();
					}
				});
				
				BufferedReader processReader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream(), Charset.forName("UTF-8"))); //$NON-NLS-1$
				Thread processReaderThread = new Thread() {
					@Override
					public void run() {
						try {
							Tracer.trace(getClass(), Trace.FINE, "DSS Server process Output Reader has started"); //$NON-NLS-1$
							String line = processReader.readLine();

							do {
								if (line != null) {
									Tracer.trace(DssLauncher.class, Trace.FINER, line);
									if (line.startsWith("The server is running on port") && !dssClientThread.isAlive()) { //$NON-NLS-1$ 
										dssClientThread.start();
									}
								}
								line = processReader.readLine();
							} while (line != null);
						} catch (IOException e) {
							e.printStackTrace();
						}
						Tracer.trace(getClass(), Trace.FINE, "DSS Server process Output Reader has stopped"); //$NON-NLS-1$
					}
				};
				processReaderThread.start();

				notifyListeners();
			}
              
		} catch (IOException e) {
			lastException = e;
			e.printStackTrace();
		} catch (URISyntaxException e) {
			lastException = e;
			e.printStackTrace();
		}
		
		Tracer.trace(getClass(), Trace.FINER, "start() END"); //$NON-NLS-1$
		
	}
	
	public void stop() {
		Tracer.trace(getClass(), Trace.FINER, "stop() BEGIN"); //$NON-NLS-1$
		
		if (serverProcess != null && serverProcess.isAlive()) {
			serverProcess.destroyForcibly();
		}
		serverProcess = null;
		
		// dssClientThread should exit on its own when server dies
		if (dssClientThread != null && dssClientThread.isAlive()) {
			Tracer.trace(getClass(), Trace.FINE, "DssClientThread still alive after server process stopped"); //$NON-NLS-1$
		}
		lastException = null;
		dssClientThread = null;
		port = null;
		dssNoFreePortError = null;
		
		// Terminate any in-progress debug sessions
		for (IDebugTarget target: DebugPlugin.getDefault().getLaunchManager().getDebugTargets()) {
			if (target instanceof DebugTarget) {
				try {
					if (target.canTerminate()) {
						target.terminate();
					}
				}catch (DebugException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		Tracer.trace(getClass(), Trace.FINER, "stop() END"); //$NON-NLS-1$
		
	}
	
	public void restart() {
		Tracer.trace(getClass(), Trace.FINER, "restart() ENTER"); //$NON-NLS-1$
		List<ConnectionSummary> connectedServers = getConnectedSummaries();
		List<Job> disconnectJobs = new ArrayList<Job>();
		List<Job> connectJobs = new ArrayList<Job>();
		
		// If users quickly apply preference changes, 
		// restart requests may be submitted before prior request completes.
		// Since only one restart job runs at a time due to scheduling rule, if there is
		// already a job waiting for another to complete, and no need to add a third
		IJobManager jobManager = Job.getJobManager();
		int restartJobsRunning = 0;
		int restartJobsWaiting = 0;
		for (Job job: jobManager.find(null)) {
			if (job.getName().equals(Messages.DssServicesManager_restartJobName)) {
				if (job.getState() == Job.RUNNING) {
					restartJobsRunning++;
				} else if (job.getState() == Job.WAITING) {
					restartJobsWaiting++;
				}
				Tracer.trace(getClass(), Trace.FINER, "Found DSS Restart Job; STATE: " + job.getState()); //$NON-NLS-1$
			}
		}
		
		if (restartJobsWaiting > 0) {
			Tracer.trace(getClass(), Trace.FINE, 
					MessageFormat.format("Found {0} waiting and {1} running DSS Restart Jobs... no need to schedule another", restartJobsWaiting, restartJobsRunning)); //$NON-NLS-1$
			Tracer.trace(getClass(), Trace.FINER, "restart() EXIT"); //$NON-NLS-1$
			return;
		}
		
		Job restartDssJob = new Job(Messages.DssServicesManager_restartJobName) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				
				monitor.beginTask(getName(), (2 * connectedServers.size()) + 1);

				Tracer.trace(getClass(), Trace.FINER, "restart() disconnecting"); //$NON-NLS-1$
				for (ConnectionSummary server: connectedServers) {
					Job disconnectJob = Db2SubSystemJob.createDisonnectJob(server);
					disconnectJob.schedule();
					disconnectJobs.add(disconnectJob);
				}

				for (Job job: disconnectJobs) {
					monitor.subTask(MessageFormat.format(Messages.DssServicesManager_subtask_waiting, job.getName()));
					try {
						job.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					monitor.worked(1);
				}
				Tracer.trace(getClass(), Trace.FINER, "restart() disconnected"); //$NON-NLS-1$

				IStatus result = Status.OK_STATUS;
				
				Runnable runAfterProxyIsAvailable = new Runnable() {
					@Override
					public void run() {
						Tracer.trace(getClass(), Trace.FINER, "restart() connecting"); //$NON-NLS-1$

						for (ConnectionSummary server: connectedServers) {
							LocationGeneralModel generalModel = new LocationGeneralModel(server.getDb2SubSystem(), server.getId());
							if (generalModel.getAuthMethod() == AUTH_METHOD.PASSWORD) {
								Job connectJob = Db2SubSystemJob.createConnectJob(server);
								connectJob.schedule();
								connectJobs.add(connectJob);
							}
							generalModel.resetProperties();
						}
						
						for (Job connectJob: connectJobs) {
							try {
								monitor.subTask(MessageFormat.format(Messages.DssServicesManager_subtask_waiting, connectJob.getName()));
								connectJob.join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							monitor.worked(1);
						}
						
						Tracer.trace(getClass(), Trace.FINER, "restart() connected"); //$NON-NLS-1$

						monitor.done();
						done(Status.OK_STATUS);
					}
				};
				
				Runnable runIfProxyNotAvailable = new Runnable() {
					@Override
					public void run() {
						String statusMessage = MessageFormat.format(Messages.DssClientThread_remote_proxy_error, "" + port); //$NON-NLS-1$
						Tracer.trace(getClass(), Trace.FINER, statusMessage);
						monitor.done();
						Status status = new Status(IStatus.ERROR, Activator.kPluginID, statusMessage);
						done(status);
					}
				};
				
				Runnable runIfNoPortAvailable = new Runnable() {
					@Override
					public void run() {
						String portRangesString = Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_DB2SQLSERVICEPORT);
						String statusMessage = MessageFormat.format(Messages.Activator_no_free_port, portRangesString);
						Tracer.trace(getClass(), Trace.FINER, statusMessage);
						monitor.done();
						Status status = new Status(IStatus.ERROR, Activator.kPluginID, statusMessage);
						done(status);
					}
				};

				if (serverProcess != null && serverProcess.isAlive()) {
					Tracer.trace(getClass(), Trace.FINER, "restart() dss process is alive"); //$NON-NLS-1$
					monitor.subTask(Messages.DssServicesManager_subtask_stopping);
					result = ASYNC_FINISH;

					serverProcess.destroyForcibly().onExit().thenRun(new Runnable() {
						@Override
						public void run() {
							Tracer.trace(getClass(), Trace.FINER, "restart() dss process forcibly destroyed"); //$NON-NLS-1$
							monitor.worked(1);
							monitor.subTask(Messages.DssServicesManager_subtask_starting);
							
							if (dssClientThread != null && dssClientThread.isAlive()) {
								//TODO thread ends on its own when the server is killed
								//			dssClientThread.interrupt();
								Tracer.trace(getClass(), Trace.FINE, "restart() dss client thread is still alive"); //$NON-NLS-1$
							}
							serverProcess = null;
							dssClientThread = null;
							port = null;
							dssNoFreePortError = null;

							// Terminate any in-progress debug sessions
							for (IDebugTarget target: DebugPlugin.getDefault().getLaunchManager().getDebugTargets()) {
								if (target instanceof DebugTarget) {
									try {
										if (target.canTerminate()) {
											target.terminate();
										}
									}catch (DebugException e) {
										e.printStackTrace();
									}
								}
							}

							start(runAfterProxyIsAvailable, runIfProxyNotAvailable, runIfNoPortAvailable);
							
							monitor.worked(1);
						}
					});
					
				} else {
					Tracer.trace(getClass(), Trace.FINER, "restart() dss process is not alive"); //$NON-NLS-1$

					stop();
					start(runAfterProxyIsAvailable, runIfProxyNotAvailable, runIfNoPortAvailable);
				}
				
				return result;
			}
		};
		
		restartDssJob.setRule(RESTART_RULE);
		restartDssJob.schedule();

		Tracer.trace(getClass(), Trace.FINE, "restart() EXIT"); //$NON-NLS-1$
		
	}
	
	public IStatus getStatus(boolean includePreferencesLocation) {
		
		if (dssNoFreePortError != null) {
			MultiStatus ms = new MultiStatus(Activator.kPluginID, Status.ERROR, Messages.Activator_tooling_unavailable);
			ms.add(new Status(Status.ERROR, Activator.kPluginID, dssNoFreePortError));
			if (includePreferencesLocation) {
				ms.add(new Status(Status.ERROR, Activator.kPluginID, Messages.Activator_fix_port_ranges_instructions));
			}
			
			return ms;
		} 
		
		IStatus clientStatus = getClientStatus();
		IStatus serverStatus = getServerStatus();
		
		if (clientStatus.isOK() && serverStatus.isOK()) {
			return Status.OK_STATUS;
		}
		MultiStatus ms = new MultiStatus(Activator.kPluginID, Status.ERROR, Messages.Activator_tooling_unavailable);
		if (!clientStatus.isOK()) {
			ms.add(clientStatus);
		}
		if (!serverStatus.isOK()) {
			ms.add(serverStatus);
		}

		return ms;
	}
	
	public DssServer getDssServer() throws Db2ToolingUnavailableException {
		
		IStatus status = getStatus(true);
		
		if (status.getSeverity() == IStatus.ERROR && dssClientThread!= null && dssClientThread.isAlive()) {
			// In case of auto-connect requesting server shortly after activation
			// First two client connect attempts separated by 2 seconds
			Tracer.trace(getClass(), Trace.FINE, "DssServer is null, sleeping for 3 seconds"); //$NON-NLS-1$
			try {
				if (Thread.currentThread() == Activator.getDisplay().getThread()) {
					Tracer.trace(getClass(), Trace.FINEST, "getDssServer() dispatching UI events"); //$NON-NLS-1$
					Activator.getDisplay().readAndDispatch();
				}
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		status = this.getStatus(true);
		
		if (status.getSeverity() == IStatus.ERROR) {
			Tracer.trace(getClass(), Trace.FINE, "getDssServer() throwing Db2ToolingUnavailableException"); //$NON-NLS-1$
			throw new Db2ToolingUnavailableException(status);
		}
		Tracer.trace(getClass(), Trace.FINEST, "getDssServer() returning server"); //$NON-NLS-1$

		return dssClientThread.getDssServer();
	}
	
	public interface IStatusListener {
		public void statusChanged(IStatus status);
		public boolean includePreferencesLocation();
	}
	
	public void addDssServiceStatusListener(IStatusListener listener) {
		statusListeners.add(listener);
	}
	
	public void removeDssServiceStatusListener(IStatusListener listener) {
		statusListeners.remove(listener);
	}
	
	// PRIVATE METHODS

	private String getFileEncoding() {
		return "-Dfile.encoding=UTF-8"; //$NON-NLS-1$
	}
	
	private String getCharacterConversion() {
		String conversionBehavior = Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_CHARACTERCONVERSION);
		String encoderMode = IPreferenceConstants.E_CHARACTERCONVERSION.replace.toString().equals(conversionBehavior) ? "3" : "1"; //$NON-NLS-1$ //$NON-NLS-2$
		return "-Ddb2.jcc.charsetDecoderEncoder=" + encoderMode; //$NON-NLS-1$
		
	}
    
	private String getClassPath() throws IOException, URISyntaxException {
		File dssFile = Activator.getInstance().getBundleFile("lib/dss-dist-2.2.3.jar"); //$NON-NLS-1$

		char separator =  SystemUtils.IS_OS_WINDOWS ? ';' : ':';
		
		StringBuffer classpath = new StringBuffer();
		classpath.append(dssFile.getAbsolutePath());

		classpath.append(separator);
		classpath.append(getDriverFile().getAbsolutePath());
		
		File licenseFile = getLicenseFile();
		if (licenseFile != null && licenseFile.exists()) {
			classpath.append(separator);
			classpath.append(licenseFile.getAbsolutePath());
		}
		
		File sqljFile = Activator.getInstance().getBundleFile("lib/sqlj4.zip"); //$NON-NLS-1$
		classpath.append(separator);
		classpath.append(sqljFile.getAbsolutePath());
		
		return classpath.toString();
	}
	
	private File getDriverFile() throws IOException, URISyntaxException {
		File driverFile = null;
		
		String userDriverPath = Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_DRIVERPATH);
		if (userDriverPath != null && userDriverPath.length() > 0) {
			driverFile = new File(userDriverPath);
		} else {
			driverFile = Activator.getInstance().getBundleFile("lib/db2jcc4.jar"); //$NON-NLS-1$
		}
		return driverFile;
	}
	
	private File getLicenseFile() throws IOException, URISyntaxException {
		File licenseFile = null;
		String userLicensePath = Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_LICENSEPATH);
		if (userLicensePath != null && userLicensePath.length() > 0) {
			licenseFile = new File(userLicensePath);
		}
		return licenseFile;
	}
	
	private String getLogLevel() {
		return Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_DEBUGLOGLEVEL);
	}
	
	private String getLogPath() {
		return Activator.getLogsFolder().getPath();
	}
	
	private String getMaxHeap() {
		return "-Xmx" + Activator.getInstance().getPreferenceStore().getString(IPreferenceConstants.P_MAXHEAPSIZE) + "M"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public Integer getPort() {
		return port;
	}
	
	private String getJavaExecutable() {
		return System.getProperty("java.home") +  //$NON-NLS-1$
				System.getProperty("file.separator") + "bin" + //$NON-NLS-1$ //$NON-NLS-2$
				System.getProperty("file.separator") + "java"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private IStatus getServerStatus() {
		if (lastException != null) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					Messages.DssServicesManager_exception, lastException); //$NON-NLS-2$
		} else if (serverProcess == null || !serverProcess.isAlive()) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					Messages.DssServicesManager_server_missing);
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus getClientStatus() {
		return dssClientThread != null ? dssClientThread.getStatus() : new Status(Status.ERROR, Activator.kPluginID, Messages.DssServicesManager_client_missing);
	}
	

	public void notifyListeners() {
		final IStatusListener[] listeners = statusListeners.toArray(new IStatusListener[0]);
		for (IStatusListener listener: listeners) {
			IStatus status = getStatus(listener.includePreferencesLocation());
			listener.statusChanged(status);
		}
	}
	
	private List<ConnectionSummary> getConnectedSummaries() {
		List<ConnectionSummary> connectedSummaries = new ArrayList<ConnectionSummary>();
		List<ConnectionSummary> allLocations = ConnectionEnvironment.getLocationSummaries();
		List<ConnectionSummary> allTuningServers = ConnectionEnvironment.getTuningServerSummaries();
		
		for (ConnectionSummary cs: allLocations) {
			Db2ConnectorService connectorService = cs.getDb2SubSystem().getDb2ConnectorService();
			if (connectorService.getConnectionObject(cs.getId()) != null) {
				connectedSummaries.add(cs);
			}
		}
		for (ConnectionSummary cs: allTuningServers) {
			Db2ConnectorService connectorService = cs.getDb2SubSystem().getDb2ConnectorService();
			if (connectorService.getTuningServerClient(cs.getId()) != null) { //||
				connectedSummaries.add(cs);
			}
		}
		return connectedSummaries;
	}
}
