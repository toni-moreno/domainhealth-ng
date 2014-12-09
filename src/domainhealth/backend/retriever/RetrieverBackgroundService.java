//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the 
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.backend.retriever;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import commonj.work.WorkItem;
import commonj.work.WorkManager;
import static domainhealth.core.env.AppProperties.*;
import domainhealth.backend.jmxpoll.StatisticCapturerJMXPoll;
import domainhealth.backend.wldfcapture.HarvesterWLDFModuleCreator;
import domainhealth.backend.wldfcapture.StatisticCapturerWLDFQuery;
import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.ContextAwareWork;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.FileUtil;
import domainhealth.core.util.ProductVersionUtil;

import domainhealth.backend.sender.GraphiteBackgroundSender;

/**
 * Statistics Retrieval Background Service which periodically (eg. every half 
 * a minute), initiates the process to collect statistics from every server in
 * the domain, placing the results into a set of CSV files. This is achieved 
 * by using a continuously looping background Java daemon thread. The process 
 * for capturing statistics is pluggable (eg. use JMX Polling to collect stats
 * or use WLDF harvesting of stats).
 * 
 * In addition to rhe background daemon thread, the Work Manager API is used 
 * to enable each server in the domain to be queried in separate parallel 
 * threads.
 */
public class RetrieverBackgroundService {

	/**
	 * Create new service with the root path to write CSV file to
	 *  
	 * @param appProps The system/application key/value pairs
	 */
	public RetrieverBackgroundService(AppProperties appProps) {
		this.domainhealthVersionNumber = appProps.getProperty(PropKey.VERSION_NUMBER_PROP);
		this.domainhealthCompilationTime = appProps.getProperty(PropKey.COMPILATION_TIME_PROP); 
		this.alwaysUseJMXPoll = appProps.getBoolProperty(PropKey.ALWAYS_USE_JMXPOLL_PROP);
		this.statisticsRetainNumDays = appProps.getIntProperty(PropKey.CSV_RETAIN_NUM_DAYS);
		this.statisticsStorage = new StatisticsStorage(appProps.getProperty(PropKey.STATS_OUTPUT_PATH_PROP),
							appProps.getProperty(PropKey.BACKEND_OUTPUT_PROP));		
		int queryIntervalSecs = appProps.getIntProperty(PropKey.QUERY_INTERVAL_SECS_PROP);

		if (queryIntervalSecs < MINIMUM_SLEEP_SECS) {
			AppLog.getLogger().warning("Specified query interval seconds of '" + queryIntervalSecs + "' is too low - changing to value '" + MINIMUM_SLEEP_SECS + "'");
			queryIntervalSecs = MINIMUM_SLEEP_SECS;
		}

		queryIntervalMillis 	= queryIntervalSecs * ONE_SECOND_MILLIS;
		minPollIntervalMillis 	= (int) (MIN_POLL_FACTOR * queryIntervalMillis);
		maxPollIntervalMillis 	= (int) (MAX_POLL_FACTOR * queryIntervalMillis);		
		componentBlacklist 	= tokenizeBlacklistText(appProps.getProperty(PropKey.COMPONENT_BLACKLIST_PROP));
		metricTypeSet 		= tokenizeTypesText(appProps.getProperty(PropKey.METRIC_TYPE_SET_PROP));
                for (String type : this.metricTypeSet) {
                      AppLog.getLogger().info("initialized Statistic Capturer metric SET: " + type.toString());
                }

		WorkManager localCaptureThreadsWkMgr = null;

		try {
			localCaptureThreadsWkMgr = getWorkManager(CAPUTURE_THREADS_WORK_MGR_JNDI);
		} catch (NamingException e) {
			throw new IllegalStateException(getClass() + " cannot be instantiateed because Work Manager '" + CAPUTURE_THREADS_WORK_MGR_JNDI + "' cannot be located. " + e.getMessage());
		}
		
		this.captureThreadsWkMgr = localCaptureThreadsWkMgr;
		
		boolean dhstats=appProps.getBoolProperty(PropKey.GRAPHITE_REPORT_DHSTATS_PROP,true);
                String backend_output=appProps.getProperty(PropKey.BACKEND_OUTPUT_PROP);
                boolean use_graphite=(backend_output.equals("graphite") | backend_output.equals("both"));

		this.sendDHStatsToGraphite=(dhstats & use_graphite);
	}

	public void setSender(GraphiteBackgroundSender gSender) {
		this.gSender=gSender;
		statisticsStorage.setSender(gSender);
	}
	
	/**
	 * Start the continuously repeating sleep-gather-schedule background 
	 * daemon thread.
	 */
	public void startup() {
		try {
			AppLog.getLogger().info("Statistics Retriever Background Service starting up");
			File rootDir = FileUtil.createOrRetrieveDir(statisticsStorage.getRootDirectoryPath());
			AppLog.getLogger().notice("Statistic CSV files location: " + rootDir.getCanonicalPath());			
			Thread backgroundThread = new Thread(new CaptureRunnable(gSender), this.getClass().getName());
			backgroundThread.setDaemon(true);
			backgroundThread.start();
			AppLog.getLogger().debug("Created background Java daemon thread to drive data retrieval process");
		} catch (Exception e) {
			AppLog.getLogger().critical("Statistics Retriever Background Service has been disabled. Reason: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Send signal to the continuously repeating sleep-gather-schedule 
	 * background process should terminate as soon as possible.
	 */
	public void shutdown() {
		keepRunning = false;
		AppLog.getLogger().info("Statistics Retriever Background Service shutting down");
	}

	/**
	 * Runnable which will be spawned as a background daemon thread 
	 * responsible for initiating the statistics capture process and
	 * then repeating the cycle over and over again.
	 */
	private class CaptureRunnable implements Runnable {
		GraphiteBackgroundSender gSender;
		public CaptureRunnable(GraphiteBackgroundSender gSender) {
			this.gSender=gSender;
		}
		public void run() {
			while (keepRunning) {
				try {
					AppLog.getLogger().debug("About to sleep and then perform processing run via the background Java daemon thread");
					performProcessingRun(gSender);
				} catch (Exception e) {
					AppLog.getLogger().warning("Statistics Retriever Background Service processing iteration (using Java Daemon Thread) has failed abnormally for this run. Reason: " + e.toString());
				}
			}			
		}
	}
	
	/**
	 * Main method responsible for running the statistics capture process. The
	 * first thing this does, when run is sleep for 1 minute before waking up 
	 * and initiating the statistics capture process to each server in the 
	 * domain, in parallel.
	 *  
	 * If this is the first time that an attempt has been made to run this 
	 * process since the server was started or this app was deployed, on the 
	 * first run, this method first performs the DomainHealth initialisation 
	 * steps, before running the normal statistic capture work.
	 * 
	 * Note: At every major step in this runnable work item code, the status 
	 * of the 'keepRunning' member variable is checked to be sure that a 
	 * signal has not been received by the background daemon to instruct it 
	 * to terminate. This allows the code to terminate as soon as possible 
	 * without producing incomplete CSV file entries.
	 */
	private void performProcessingRun(GraphiteBackgroundSender gSender ) {

		this.gSender=gSender;

		startWorkTime = System.currentTimeMillis();		

		if (keepRunning) {
			try { Thread.sleep(sleepPeriodMillis); } catch (Exception e) {}			
		}

		long newSleepIntervalMillis = queryIntervalMillis;
		
		if ((keepRunning) && (!firstTimeProcessingRanOK)) {
			try {
				runFirstTimeProcessing();
				firstTimeProcessingRanOK = true;
			} catch (Exception e) {					
				AppLog.getLogger().error("Statistics Retriever Background Service - first time processing initialisation failed - will attempt initialisation again after a pause (this can be caused if the server takes a while to start-up, in which case, the retry should sort things out). Cause: "  + e.toString());
				AppLog.getLogger().debug("First time processing failure cause: + " + e.getMessage(), e);
				newSleepIntervalMillis = INITIALISATION_ATTEMPT_AGAIN_SLEEP_DURATION;
			}
		}
			
		if ((keepRunning) && (firstTimeProcessingRanOK)) {
			runNormalProcessing();
		}
			
		if (keepRunning) {
			// Attempt to compensate for the fact that the start time of each 
			// run will drift out a bit due to excess processing time consumed
			// during each iteration 

			long lagMillis = Math.max(System.currentTimeMillis() - startWorkTime - sleepPeriodMillis, 0);
			sleepPeriodMillis = Math.max(minPollIntervalMillis, newSleepIntervalMillis - lagMillis);
		}
		
	}

	/**
	 * First time processing. Runs to set up the monitoring environment. Does 
	 * not run as part of application deployment / start-up because at that 
	 * point certain vital resources such as the servers' JMX trees may not 
	 * yet be available to access.
	 * 
	 * @throws WebLogicMBeanException Indicates a problem in initialising the monitoring environment - indicates that first time processing will have to be run again at a later time
	 */
	private void runFirstTimeProcessing() throws WebLogicMBeanException {
		AppLog.getLogger().debug("Statistics Retriever Background Service running first time processing initialisation steps");
		
		if (!DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()) {
			AppLog.getLogger().error("Attempt made to run 'DomainHealth' application on a managed server. Attempt halted. Undeploy 'DomainHealth' and re-deploy to run on the domain's Admin Server only");
			shutdown();
			return;
		}

		wlsVersionNumber = getWLSDomainVersion();		
		jvmVersion 	 = getJVMVersion();
		
		if (alwaysUseJMXPoll) {
			useWLDFHarvester = false;				
		} else {
			useWLDFHarvester = ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y(wlsVersionNumber, WLS_MIN_VERSION_FOR_USING_WLDF_RELIABLY);
		}
		
		if (useWLDFHarvester) {				
			HarvesterWLDFModuleCreator harvesterModule = new HarvesterWLDFModuleCreator(queryIntervalMillis, domainhealthVersionNumber, wlsVersionNumber, domainhealthCompilationTime, metricTypeSet);
			
			if (harvesterModule.isDomainHealthAbleToUseWLDF()) {
				useWLDFHarvester = true;					
				//harvesterModule.createIfNeeded();
				harvesterModule.createAlways(); 
			} else {
				useWLDFHarvester = false;					
				AppLog.getLogger().warning("WLDF module creation problems occurred. WLDF Capture mode can't be used - must use JMX Poll mode instead");
			}
		} 
		
		if (useWLDFHarvester) {				
			AppLog.getLogger().notice("Server statistics retrieval mode: WLDF Harvested Data Capture");
		} else {
			AppLog.getLogger().notice("Server statistics retrieval mode: JMX MBean Attribute Polling");
		}

		AppLog.getLogger().info("Statistics Retriever Background Service first time processing initialisation steps finished successfully");
	}

	/**
	 * Runs the normal statistic capture process, by iterating through the 
	 * list of currently running servers in the domain, and for each of 
	 * these, schedule the query of the servers stats in a separate work item
	 * to run in parallel in the WebLogic thread pool.
	 */
	private void runNormalProcessing() {
		DomainRuntimeServiceMBeanConnection conn = null;
		
		try {
			AppLog.getLogger().debug("Statistics Retriever Background Service running another iteration to capture and log stats");
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();			
			int length = serverRuntimes.length;
			List<WorkItem> pollerWorkItemList = new ArrayList<WorkItem>();
			
			for (int i = 0; i < length; i++) {
				final String serverName = conn.getTextAttr(serverRuntimes[i], NAME);
				final String serverHost = conn.getTextAttr(serverRuntimes[i],"CurrentMachine");

				if(( serverHost==null ) || ( serverHost.length() == 0 )) 
					AppLog.getLogger().info("SERVER :"+serverName+" OVER MACHINE: <NOT SET IN CONFIG>");
				else 	
					AppLog.getLogger().info("SERVER :"+serverName+" OVER MACHINE: "+serverHost);

				final StatisticCapturer capturer = getStatisticCapturer(conn, serverRuntimes[i], serverName);
				capturer.setHost(serverHost);

				pollerWorkItemList.add(captureThreadsWkMgr.schedule(new ContextAwareWork() {
					public void doRun() {
						try {
							if(sendDHStatsToGraphite) { 
								//internal metric counters
								gSender.resetCounter(serverName);
								long start_time=System.currentTimeMillis();	
								capturer.captureAndLogServerStats();
								long end_time=System.currentTimeMillis();
								String elapsed=Long.toString(end_time-start_time);
								//send gathering statistics metrics over the graphite backend 
								gSender.sendDHData("servers",serverName,"retrieve_time",elapsed);
								AppLog.getLogger().info("CAPTURER LOOP for server "+ serverName +" : complete :" +elapsed+ " ms");
							}
							else capturer.captureAndLogServerStats();

						} catch (Exception e) {
							AppLog.getLogger().error(e.toString(),e);
							AppLog.getLogger().error("Statistics Retriever Background Service - unable to retrieve statistics for specific server '" + serverName + "' for this iteration");
						}						
					}					
				}));				
			}			
			
			boolean allCompletedSuccessfully = captureThreadsWkMgr.waitForAll(pollerWorkItemList, maxPollIntervalMillis);
			warnIfTimedOut(allCompletedSuccessfully);
			cleanupOldStatisticsIfNecessary();
			AppLog.getLogger().info("Statistics Retriever Background Service completing another iteration successfully");
		} catch (Exception e) {
			AppLog.getLogger().error(e.toString());
			e.printStackTrace();
			AppLog.getLogger().error("Statistics Retriever Background Service - unable to retrieve statistics for domain's servers for this iteration");
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * Returns the implementation of the Statistics Capturer (eg. JMX Poll, 
	 * WLDF Harvest).
	 * 
	 * @param conn JMX Connection to domain runtime
	 * @param serverRuntime Handle on the specific server runtime to do capturing for
	 * @param serverName The name of the specific server runtime to do capturing for
	 * @return The new instance of the Statistics Capturer implementation
	 */
	private StatisticCapturer getStatisticCapturer(DomainRuntimeServiceMBeanConnection conn, ObjectName serverRuntime, String serverName) {
		if (useWLDFHarvester) {
			return new StatisticCapturerWLDFQuery(statisticsStorage, conn, serverRuntime, serverName, queryIntervalMillis, componentBlacklist, metricTypeSet,wlsVersionNumber,jvmVersion);
		} else {
			return new StatisticCapturerJMXPoll(statisticsStorage, conn, serverRuntime, serverName, queryIntervalMillis, componentBlacklist, metricTypeSet,wlsVersionNumber,jvmVersion);
		}
	}
		
	/**
	 * If all Work Manager work items have not completed when timeout occurs, 
	 * log warning
	 * 
	 * @param allCompletedSuccessfully Flag indicating if all Work Items completed successfully  
	 */
	private void warnIfTimedOut(boolean allCompletedSuccessfully) {
		if (!allCompletedSuccessfully) {
			AppLog.getLogger().warning(getClass().getName() + " timed-out when retrieving data from one or more servers in the domain");
		}
	}

	/**
	 * Determine the running WebLogic domain's name.
	 * 
	 * @return The version text (user_domain)
	 */
	public String getWLSDomainName() {
		String dom_name = null;
		DomainRuntimeServiceMBeanConnection conn = null;
		
		try {
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName domainConfig = conn.getDomainConfiguration();
			dom_name= conn.getTextAttr(domainConfig, "Name");
			
		} catch (WebLogicMBeanException e) {
			// Assume caused by "DomainVersion" attribute not existing which
			// would indicate that this is a 9.0 or 9.1 domain version
			// Detected a bug when AdminServer is primary initialized cd 
			AppLog.getLogger().error(e.toString(),e);
			dom_name = "default_domain";
		} finally {
			AppLog.getLogger().info("Domain NAME SET TO :" + dom_name);
			if (conn != null) {
				conn.close();
			}
		}	

		return dom_name;
	}


	/**
	 * Determine the running WebLogic domain's version.
	 * 
	 * @return The version text (e.g. 10.3.5)
	 */
	private String getWLSDomainVersion() {
		String version = null;
		DomainRuntimeServiceMBeanConnection conn = null;
		
		try {
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName domainConfig = conn.getDomainConfiguration();
			version = conn.getTextAttr(domainConfig, DOMAIN_VERSION);
			
		} catch (WebLogicMBeanException e) {
			// Assume caused by "DomainVersion" attribute not existing which
			// would indicate that this is a 9.0 or 9.1 domain version
			AppLog.getLogger().error(e.toString(),e);
			version = DEFAULTED_WLS_VERSION;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}	

		return version;
	}

	/**
	 * Determine the running JVM  version.
	 * 
	 * @return The version text (e.g. 10.3.5)
	 */
	private String getJVMVersion() {
		String vmvendor =null;
		String vendor =null;
		String version = null;
		String[] parts = {"0","0"};
		DomainRuntimeServiceMBeanConnection conn = null;
		/*we are assuming all Domain has the same JVM !!*/
		
		try {
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			ObjectName jvm = conn.getChild(serverRuntimes[0], JVM_RUNTIME);
			vendor = conn.getTextAttr(jvm,"JavaVendor");
			version = conn.getTextAttr(jvm,"JavaVersion");
			vmvendor = conn.getTextAttr(jvm,"JavaVMVendor");
			parts=version.split("[-.]+");
			
		} catch (Exception e) {
			// Assume caused by "DomainVersion" attribute not existing which
			// would indicate that this is a 9.0 or 9.1 domain version
			AppLog.getLogger().error(e.toString(),e);
			version = "0";
		} finally {
			AppLog.getLogger().info("JVM Version set to VENDOR :" +vendor+ " VERSION: "+version+" VMVENDOR: "+vmvendor+" SUB: "+parts[0]+" SUB: "+parts [1] );
			if (conn != null) {
				conn.close();
			}
		}	

		return parts[1];
	}


	/**
	 * Retrieves a named work manager from the local JNDI tree. 
	 * 
	 * @param wkMgrName The name of the work manager to retrieve
	 * @return The found work manager
	 * @throws NamingException Indicates that the work manager could not be located
	 */
	private WorkManager getWorkManager(String wkMgrName) throws NamingException {
		InitialContext ctx = null;
		
		try {
		    ctx = new InitialContext();
		    return (WorkManager) ctx.lookup(wkMgrName);
		} finally {
		    try { ctx.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Gets list of types to   gather data
	   statistics collected and shown.
	 * 
	 * @param metricTypeSet The text containing comma separated list of names to ignore
	 * @return A strongly type list of types to g 
	 */
	private List<String> tokenizeTypesText(String metricTypeText) {
		List<String> typelist = new ArrayList<String>();
		String[] typelistArray = null;
		
		if (metricTypeText != null) {
			typelistArray = metricTypeText.split(BLACKLIST_TOKENIZER_PATTERN);
		}
		
		if ((typelistArray != null) && (typelistArray.length > 0)) {
			typelist = Arrays.asList(typelistArray);
		} else {
			typelist = new ArrayList<String>();
			typelist.add("core");
		}
				
		return typelist;
	}


	/**
	 * Gets list of names of web-app and ejb components which should not have 
	 * statistics collected and shown.
	 * 
	 * @param blacklistText The text containing comma separated list of names to ignore
	 * @return A strongly type list of names to ignore
	 */
	private List<String> tokenizeBlacklistText(String blacklistText) {
		List<String> blacklist = new ArrayList<String>();
		String[] blacklistArray = null;
		
		if (blacklistText != null) {
			blacklistArray = blacklistText.split(BLACKLIST_TOKENIZER_PATTERN);
		}
		
		if ((blacklistArray != null) && (blacklistArray.length > 0)) {
			blacklist = Arrays.asList(blacklistArray);
		} else {
			blacklist = new ArrayList<String>();
		}
				
		return blacklist;
	}
	
	/**
	 * Clean up the old day statistics directories according to number of days
	 * back the DH configuration settings indicates that statistics files 
	 * should be retained for. Only attempts clean-up once per hour to avoid 
	 * excessive unnecessary checks.
	 */
	public final void cleanupOldStatisticsIfNecessary() {
		if (statisticsRetainNumDays <= 0) {
			return;
		}
		
		long timeNowMillis = System.currentTimeMillis(); 
		
		if ((timeNowMillis - lastCSVCleanupTimeMillis) > HOURLY_CSV_CLEANUP_CHECK_IN_MILLIS) {
			lastCSVCleanupTimeMillis = timeNowMillis;
			statisticsStorage.cleanupOldDirectories(statisticsRetainNumDays);
		}
	}
	
	// Members

	protected GraphiteBackgroundSender gSender;
	private final boolean sendDHStatsToGraphite;

	private final String domainhealthVersionNumber;
	private final String domainhealthCompilationTime; 
	private final boolean alwaysUseJMXPoll;
	private final int statisticsRetainNumDays;
	private final int queryIntervalMillis;
	private final int minPollIntervalMillis;
	private final int maxPollIntervalMillis;
	private final List<String> componentBlacklist;
	private final List<String> metricTypeSet;
	private String wlsVersionNumber = null;
	private String jvmVersion = null;
	private boolean useWLDFHarvester = false;
	private final StatisticsStorage statisticsStorage;
	private final WorkManager captureThreadsWkMgr;
	private long sleepPeriodMillis = INITIAL_SLEEP_DURATION;
	private long startWorkTime = System.currentTimeMillis(); 
	private volatile boolean keepRunning = true;
	private boolean firstTimeProcessingRanOK = false;
	private long lastCSVCleanupTimeMillis = 0L;

	// Constants
	private final static String DEFAULTED_WLS_VERSION = "9.0.0";
	private static final String WLS_MIN_VERSION_FOR_USING_WLDF_RELIABLY = "10.3";
	private final static int ONE_SECOND_MILLIS = 1000;
	private final static int MINIMUM_SLEEP_SECS = 15;
	private final static float MIN_POLL_FACTOR = 0.1F;
	private final static float MAX_POLL_FACTOR = 0.9F;
	private final static int INITIAL_SLEEP_DURATION = 30 * 1000;
	private final static int INITIALISATION_ATTEMPT_AGAIN_SLEEP_DURATION = 90 * 1000;	
	private final static String BLACKLIST_TOKENIZER_PATTERN = ",\\s*";
	private final static String CAPUTURE_THREADS_WORK_MGR_JNDI = "java:comp/env/DomainHealth_IndividualServerStatCapturerWorkMngr";
	private final static long HOURLY_CSV_CLEANUP_CHECK_IN_MILLIS = 60 * 60 * 1000;
}
