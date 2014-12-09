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
package domainhealth.core.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static domainhealth.core.jmx.JavaMBeanPropConstants.*;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;

/**
 * Map of WebLogic MBean property names and their associated attributes 
 * (property name, property title, property units). Also provides constants
 * listing all the attributes that should be monitored.
 */
public class MonitorProperties {

	/**
	 * Set the deep   for a property in the list
	 * 
	 * @param mode The metric set mode
	 * @return The property key
	 */
	public static String[] ArrayConcat(String[] a,String[] b) {
   		int aLen = a.length;
   		int bLen = b.length;
  		String[] c= new String[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	public static String[] ArrayConcatAll(String[]... jobs) {
        	int len = 0;
        	for (final String[] job : jobs) {
            		len += job.length;
        	}

        	final String[] result = new String[len];

        	int currentPos = 0;
        	for (final String[] job : jobs) {
            		System.arraycopy(job, 0, result, currentPos, job.length);
            		currentPos += job.length;
        	}

        	return result;
    	}
	

	/**
	 * Set the deep   for a property in the list
	 * 
	 * @param mode The metric set mode
	 * @return The property key
	 */
	public static void setMetricDeep(String mode) {
		//jvm
		String[] j_cl=null; //classLoading
		String[] j_comp=null; //compilation
		String[] j_gc=null; //Garbage Collector
		String[] j_mem=null; //Memory Manager
		String[] j_memp=null; //Memory pool
		String[] j_thread=null;
		//core
		String[] server_list=null;
		String[] jvm_list=null;
		String[] jvm_list_header=null;
		String[] jrockit_list=null;
		String[] jrockit_list_min=null; 
		String[] threadpool_list=null;
		String[] jta_list=null;
		//datasource
		String[] jdbc_list=null;
		//jms destinations
		String[] jms_list=null;
		//webapp
		String[] webapp_list=null;
		//ejb 
		String[] ejbpool_list=null;
		String[] ejbtrans_list=null;
		//wkmgr
		String[] wkmgr_list=null;
		//srvch
		String[] srvch_list=null;
		//host
		String[] host_list=null;

		
		if((mode.equalsIgnoreCase("basic") | mode.equalsIgnoreCase("extended"))) {
			//jvm
			j_cl		= new String[] {J_CURRENT_LOADED_CLASS_COUNT,J_TOTAL_LOADED_CLASS_COUNT,J_TOTAL_UNLOADED_CLASS_COUNT};
			j_comp		= new String[] {J_TOTAL_COMPILATION_TIME_CLASS};
			j_gc		= new String[] {J_OLD_COLLECTION_COUNT,J_OLD_COLLECTION_TIME,J_YOUNG_COLLECTION_COUNT,J_YOUNG_COLLECTION_TIME};
			j_mem		= new String[] {J_HEAP_COMMITTED,J_HEAP_INIT,J_HEAP_MAX,J_HEAP_USED,J_NOT_HEAP_COMMITTED,J_NOT_HEAP_INIT,J_NOT_HEAP_MAX,J_NOT_HEAP_USED };
			j_memp		= new String[] {J_MEMPOOL_CM_COMMITTED,J_MEMPOOL_CM_INIT,J_MEMPOOL_CM_MAX,J_MEMPOOL_CM_USED,J_MEMPOOL_CB_COMMITTED,J_MEMPOOL_CB_INIT,J_MEMPOOL_CB_MAX,J_MEMPOOL_CB_USED,J_MEMPOOL_NURSERY_COMMITTED,J_MEMPOOL_NURSERY_INIT,J_MEMPOOL_NURSERY_MAX,J_MEMPOOL_NURSERY_USED,J_MEMPOOL_OLD_COMMITTED,J_MEMPOOL_OLD_INIT,J_MEMPOOL_OLD_MAX,J_MEMPOOL_OLD_USED };
			j_thread	= new String[] {J_CUR_DAEMON_THREAD_COUNT,J_CUR_NON_DAEMON_THREAD_COUNT,J_CUR_TOTAL_THREAD_COUNT,J_TOTAL_STARTED_THREAD_COUNT };
			//core
			server_list 	= new String[] {SERVER_STATE,OPEN_SOCKETS};
			jvm_list	= new String[] {HEAP_SIZE_CURRENT, HEAP_FREE_CURRENT,HEAP_FREE_PERCENT}; //can not change this !!
			jvm_list_header = new String[] {HEAP_SIZE_CURRENT, HEAP_FREE_CURRENT,HEAP_USED_CURRENT,HEAP_FREE_PERCENT}; //can not change this !!
			jrockit_list	= new String[] {JVM_PROCESSOR_LOAD,TOTAL_GC_COUNT,TOTAL_GC_TIME,TOTAL_NURSERY_SIZE,HEAP_SIZE_MAX};
			jrockit_list_min = new String[] {JVM_PROCESSOR_LOAD};
			threadpool_list = new String[] {EXECUTE_THREAD_TOTAL_COUNT,HOGGING_THREAD_COUNT,EXECUTE_THREAD_IDLE_COUNT,STANDBY_THREAD_COUNT,THROUGHPUT};
			jta_list	= new String[] {TRANSACTION_TOTAL_COUNT,TRANSACTION_COMMITTED_COUNT,TRANSACTION_ROLLEDBACK_COUNT,TRANSACTIONS_ACTIVE_TOTAL_COUNT};
			//jdbc
			jdbc_list       = new String[] {NUM_AVAILABLE,NUM_UNAVAILABLE,ACTIVE_CONNECTONS_CURRENT_COUNT, CONNECTION_DELAY_TIME,WAITING_FOR_CONNECTION_CURRENT_COUNT,FAILED_RESERIVE_REQUEST_COUNT};
			//jms
			jms_list        = new String[] {MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, CONSUMERS_CURRENT_COUNT};
			//webapp
			webapp_list	= new String[] {SESSIONS_TOTAL_COUNT};
			//ejb 
			ejbpool_list	= new String[] {BEAN_ACCESS_TOTAL_COUNT, BEANS_INUSE_CURRENT_COUNT, BEAN_WAITING_TOTAL_COUNT};
			ejbtrans_list	= new String[] {BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT};
			//wkmgr
			wkmgr_list 	= new String[] {COMPLETED_REQUESTS, PENDING_REQUESTS};
			//srvch_list	
			srvch_list	= new String[] {CONNECTIONS_COUNT, CHNL_MESSAGES_RECEIVED_COUNT};
			//host
			host_list	= new String[] {NETWORK_RX_ERRORS, NETWORK_RX_MEGABYTES, NETWORK_TX_MEGABYTES, NETWORK_TX_ERRORS, PHYSICAL_MEMORY_USED_PERCENT, PHYSICAL_SWAP_USED_PERCENT, PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, PROCESSOR_USAGE_PERCENT};
			
		} else  {
			//jvm
			j_cl		= new String[] {J_CURRENT_LOADED_CLASS_COUNT,J_TOTAL_LOADED_CLASS_COUNT,J_TOTAL_UNLOADED_CLASS_COUNT};
			j_comp		= new String[] {J_TOTAL_COMPILATION_TIME_CLASS};
			j_gc		= new String[] {J_OLD_COLLECTION_COUNT,J_OLD_COLLECTION_TIME,J_YOUNG_COLLECTION_COUNT,J_YOUNG_COLLECTION_TIME};
			j_mem		= new String[] {J_HEAP_COMMITTED,J_HEAP_INIT,J_HEAP_MAX,J_HEAP_USED,J_NOT_HEAP_COMMITTED,J_NOT_HEAP_INIT,J_NOT_HEAP_MAX,J_NOT_HEAP_USED };
			j_memp		= new String[] {J_MEMPOOL_CM_COMMITTED,J_MEMPOOL_CM_INIT,J_MEMPOOL_CM_MAX,J_MEMPOOL_CM_USED,J_MEMPOOL_CB_COMMITTED,J_MEMPOOL_CB_INIT,J_MEMPOOL_CB_MAX,J_MEMPOOL_CB_USED,J_MEMPOOL_NURSERY_COMMITTED,J_MEMPOOL_NURSERY_INIT,J_MEMPOOL_NURSERY_MAX,J_MEMPOOL_NURSERY_USED,J_MEMPOOL_OLD_COMMITTED,J_MEMPOOL_OLD_INIT,J_MEMPOOL_OLD_MAX,J_MEMPOOL_OLD_USED };
			j_thread	= new String[] {J_CUR_DAEMON_THREAD_COUNT,J_CUR_NON_DAEMON_THREAD_COUNT,J_CUR_TOTAL_THREAD_COUNT,J_TOTAL_STARTED_THREAD_COUNT };

			//core
			server_list 	= new String[] {SERVER_STATE, OPEN_SOCKETS};
			//jvm has different attributes for WLDF harvester module and for final header line because of we are generating HEAP_USED_CURRENT 
			jvm_list	= new String[] {HEAP_SIZE_CURRENT, HEAP_FREE_CURRENT, HEAP_FREE_PERCENT};
			jvm_list_header	= new String[] {HEAP_SIZE_CURRENT, HEAP_FREE_CURRENT,HEAP_USED_CURRENT, HEAP_FREE_PERCENT};

			jrockit_list    = new String[] {JVM_PROCESSOR_LOAD,TOTAL_GC_COUNT,TOTAL_GC_TIME,TOTAL_NURSERY_SIZE,HEAP_SIZE_MAX};	
			jrockit_list_min = new String[] {JVM_PROCESSOR_LOAD};
			threadpool_list	= new String[] {EXECUTE_THREAD_TOTAL_COUNT, HOGGING_THREAD_COUNT, PENDING_USER_REQUEST_COUNT, THREAD_POOL_QUEUE_LENGTH, COMPLETED_REQUEST_COUNT, EXECUTE_THREAD_IDLE_COUNT, MIN_THREADS_CONSTRAINT_COMPLETED, MIN_THREADS_CONSTRAINT_PENDING, STANDBY_THREAD_COUNT, THROUGHPUT};
			jta_list	= new String[] {TRANSACTION_TOTAL_COUNT, TRANSACTION_COMMITTED_COUNT, TRANSACTION_ROLLEDBACK_COUNT, TRANSACTION_HEURISTICS_TOTAL_COUNT, TRANSACTION_ABANDONED_TOTAL_COUNT, TRANSACTIONS_ACTIVE_TOTAL_COUNT};
			//jdbc
			jdbc_list 	= new String[] { NUM_AVAILABLE, NUM_UNAVAILABLE, ACTIVE_CONNECTONS_CURRENT_COUNT, CONNECTION_DELAY_TIME, FAILED_RESERIVE_REQUEST_COUNT, FAILURES_TO_RECONNECT_COUNT, LEAKED_CONNECTION_COUNT, WAITING_FOR_CONNECTION_CURRENT_COUNT, WAITING_FOR_CONNECTION_FAILURES_TOTAL, WAITING_SECONDS_HIGH_COUNT};
			//jms
			jms_list	= new String[] {MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, MESSAGES_RECEIVED_COUNT, MESSAGES_HIGH_COUNT, CONSUMERS_CURRENT_COUNT, CONSUMERS_HIGH_COUNT, CONSUMERS_TOTAL_COUNT};
			//webapp
			webapp_list	= new String[] {SESSIONS_CURRENT_COUNT, SESSIONS_HIGH_COUNT, SESSIONS_TOTAL_COUNT};
			//ejb
			ejbpool_list	= new String[] {BEANS_POOLED_CURRENT_COUNT, BEAN_ACCESS_TOTAL_COUNT, BEANS_INUSE_CURRENT_COUNT, BEAN_WAITING_CURRENT_COUNT, BEAN_WAITING_TOTAL_COUNT};
			ejbtrans_list	= new String[] {BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT};
			//wkmgr
			wkmgr_list	= new String[] {COMPLETED_REQUESTS, PENDING_REQUESTS, STUCK_THREAD_COUNT};
			//srvch_list
			srvch_list	= new String[] {ACCEPT_COUNT, CONNECTIONS_COUNT, CHNL_MESSAGES_RECEIVED_COUNT, CHNL_MESSAGES_SENT_COUNT};
			//host_list
			host_list	= new String[] {JVM_INSTANCE_CORES_USED, JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, NATIVE_PROCESSES_COUNT, NETWORK_RX_MEGABYTES, NETWORK_RX_DROPPED, NETWORK_RX_ERRORS, NETWORK_RX_FRAME, NETWORK_RX_OVERRUNS, NETWORK_MILLIONS_RX_PACKETS, NETWORK_TX_MEGABYTES, NETWORK_TX_CARRIER, NETWORK_TX_COLLISIONS,NETWORK_TX_DROPPED, NETWORK_TX_ERRORS, NETWORK_TX_OVERRUNS, NETWORK_MILLIONS_TX_PACKETS, PHYSICAL_MEMORY_USED_PERCENT, PHYSICAL_SWAP_USED_PERCENT, PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, PROCESSOR_USAGE_PERCENT, ROOT_FILESYSTEM_USED_PERCENT, TCP_CLOSE_WAIT_COUNT, TCP_ESTABLISHED_COUNT, TCP_LISTEN_COUNT, TCP_TIME_WAIT_COUNT};

		}
		//setting list
		J_CLASSLOADING_MBEAN_ATTR_LIST		=j_cl;
		J_COMPILATION_MBEAN_ATTR_LIST		=j_comp;
		J_GARBAGECOLLECTOR_MBEAN_ATTR_LIST	=j_gc;
		J_MEMORY_MBEAN_ATTR_LIST		=j_mem;
		J_MEMORYPOOL_MBEAN_ATTR_LIST		=j_memp;
		J_TREAD_MBEAN_ATTR_LIST			=j_thread;
		J_MBEAN_ALL				=ArrayConcatAll(j_cl,j_comp,j_gc,j_mem,j_memp,j_thread);

		//core list
		SERVER_MBEAN_MONITOR_ATTR_LIST		=server_list;
		JVM_MBEAN_MONITOR_ATTR_LIST		=jvm_list; //can not be changed beacause of hardcoded parameters over 
		
		JROCKIT_MBEAN_MONITOR_ATTR_LIST		=jrockit_list;
		THREADPOOL_MBEAN_MONITOR_ATTR_LIST	=threadpool_list;
		JTA_MBEAN_MONITOR_ATTR_LIST		=jta_list;

		//used only to create header line on csv files
		//J_CORE_ALL				=ArrayConcatAll(server_list,jvm_list_header,jrockit_list,threadpool_list,jta_list);
		J_CORE_WITH_JVM = ArrayConcatAll(server_list,jvm_list_header,jrockit_list,threadpool_list,jta_list);
		J_CORE_WITHOUT_JVM = ArrayConcatAll(server_list,jrockit_list_min,threadpool_list,jta_list);
		//needed for harvester
		JROCKIT_FULL_MBEAN_MONITOR_ATTR_LIST	= ArrayConcat(jvm_list,jrockit_list);
		//jdbc
		JDBC_MBEAN_MONITOR_ATTR_LIST		=jdbc_list;
		//jms
		JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST	=jms_list;
		//webapp
		WEBAPP_MBEAN_MONITOR_ATTR_LIST 		=webapp_list;
		//ejb
		EJB_POOL_MBEAN_MONITOR_ATTR_LIST	=ejbpool_list;
		EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST =ejbtrans_list;

		EJB_MBEAN_MONITOR_ATTR_LIST 		= ArrayConcat(ejbpool_list,ejbtrans_list);	
		//wkmgr
		WKMGR_MBEAN_MONITOR_ATTR_LIST 		=wkmgr_list;
		//srvchannel
		SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST 	=srvch_list;
		//host
		HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST = host_list;
	}


	/**
	 * Gets the key for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property key
	 */
	public static String key(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getKey();
		}
	}

	/**
	 * Gets the title for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property title
	 */
	public static String title(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getTitle();
		}
	}

	/**
	 * Gets the units for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property units
	 */
	public static String units(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getUnits();
		}
	}

	/**
	 * 'weblogic.management.runtime.%sMBean' MBean type template
	 */
	public final static String RUNTIME_MBEAN_TYPE_TEMPLATE = "weblogic.management.runtime.%sMBean";	

	/**
	 * 'com.bea:Name=weblogic.kernel.Default,ServerRuntime=%s,Type=WorkManagerRuntime' MBean name template
	 */
	public final static String WKMGR_MBEAN_NAME_TEMPLATE = "com.bea:Name=weblogic.kernel.Default,ServerRuntime=%s,Type=WorkManagerRuntime";	

	/**
	 * 'WLHostMachineStats' MBean instance name
	 */
	public final static String HOST_MACHINE_MBEAN_NAME = "WLHostMachineStats";

	/**
	 * 'wlhostmachinestats:Location=%s,name=WLHostMachineStats' MBean name template
	 */
	public final static String HOST_MACHINE_MBEAN_FULLNAME_TEMPLATE = "wlhostmachinestats:Location=%s,name=" + HOST_MACHINE_MBEAN_NAME;	

	/**
	 * Default Name of core resource - empty string ''
	 */
	public final static String CORE_RSC_DEFAULT_NAME = "";

	/**
	 * Name of the 'core' category of resource for core server statistics
	 */
	public final static String JVM_RESOURCE_TYPE = "jvm";

	/**
	 * Name of the 'core' category of resource for core server statistics
	 */
	public final static String CORE_RESOURCE_TYPE = "core";
	
	/**
	 * Name of the 'datasource' category of resource for Data Source related statistics
	 */
	public final static String DATASOURCE_RESOURCE_TYPE = "datasource";
	
	/**
	 * Name of the 'destination' category of resource for Destination related statistics
	 */
	public final static String DESTINATION_RESOURCE_TYPE = "destination";
	
	/**
	 * Name of the 'webapp' category of resource for Web App related statistics
	 */
	public final static String WEBAPP_RESOURCE_TYPE = "webapp";	
	
	/**
	 * Name of the 'ejb' category of resource for EJB related statistics
	 */
	public final static String EJB_RESOURCE_TYPE = "ejb";	
	
	/**
	 * Name of the 'workmgr' category of resource for Work Manager related statistics
	 */
	public final static String WORKMGR_RESOURCE_TYPE = "workmgr";	
	
	/**
	 * Name of the 'svrchnl' category of resource for Server Channel related statistics
	 */
	public final static String SVRCHNL_RESOURCE_TYPE = "svrchnl";	

	/**
	 * Name of the 'hostmachine' category of resource for WL Host Machine custom MBean related statistics
	 */
	public final static String HOSTMACHINE_RESOURCE_TYPE = "hostmachine";	

	/**
	 * List of names or allowable resource types (eg. core, datasource)
	 */
	public final static List<String> LEGAL_RESOURCE_TYPES = Arrays.asList(JVM_RESOURCE_TYPE, CORE_RESOURCE_TYPE, DATASOURCE_RESOURCE_TYPE, DESTINATION_RESOURCE_TYPE, WEBAPP_RESOURCE_TYPE, EJB_RESOURCE_TYPE, WORKMGR_RESOURCE_TYPE, SVRCHNL_RESOURCE_TYPE, HOSTMACHINE_RESOURCE_TYPE);


	public static String[]	J_CLASSLOADING_MBEAN_ATTR_LIST;
	public static String[]	J_COMPILATION_MBEAN_ATTR_LIST;
	public static String[]  J_GARBAGECOLLECTOR_MBEAN_ATTR_LIST;
	public static String[]  J_MEMORY_MBEAN_ATTR_LIST;
	public static String[]	J_MEMORYPOOL_MBEAN_ATTR_LIST;
	public static String[]  J_TREAD_MBEAN_ATTR_LIST;

	public static String[]	J_MBEAN_ALL;


	/**
	 * List of Server MBean Attributes to be monitored
	 */
	public static String[] SERVER_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of JVM MBean Attributes to be monitored
	 */
	public static String[] JVM_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of JRockit MBean Attributes to be monitored
	 */
	public static String[] JROCKIT_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of FULL( combined previous both) JRockit MBean Attributes to be monitored
	 */
	public static String[] JROCKIT_FULL_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of Thread Pool MBean Attributes to be monitored
	 */
	public static String[] THREADPOOL_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of JTA MBean Attributes to be monitored
	 */
	public static String[] JTA_MBEAN_MONITOR_ATTR_LIST;

	public static String[] J_CORE_WITH_JVM;
	
	public static String[] J_CORE_WITHOUT_JVM;

	/**
	 * List of JDBC Data Source MBean Attributes to be monitored
	 */
	//public final static String[] JDBC_MBEAN_MONITOR_ATTR_LIST;
	public static String[] JDBC_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of JMS Destination MBean Attributes to be monitored
	 */
	public static String[] JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of WebApp MBean Attributes to be monitored
	 */
	public static String[] WEBAPP_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of EJB Pool Runtime MBean Attributes to be monitored
	 */
	public static String[] EJB_POOL_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of EJB Transaction Runtime MBean Attributes to be monitored
	 */
	public static String[] EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST;	

	/**
	 * List of EJB MBean Attributes to be monitored
	 */
	public static String[] EJB_MBEAN_MONITOR_ATTR_LIST;
	
	/**
	 * List of Work Manager MBean Attributes to be monitored
	 */
	public static String[] WKMGR_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * List of Server Channel MBean Attributes to be monitored
	 */
	public static String[] SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST;
	
	/**
	 * List of  MBean Attributes to be monitored
	 */
	public static String[] HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST;

	/**
	 * 'weblogic.kernel.Default' Default Work Manager name
	 */
	public static final String DEFAULT_WKMGR_NAME = "weblogic.kernel.Default";

	/**
	 * List of internal webapp & ejb names which should not be monitored
	 */
	public final static List<String> XAPPNAME_BLACKLIST = Arrays.asList("domainhealth", "console", "consolehelp", "bea_wls9_async_response", "bea_wls_cluster_internal", "bea_wls_deployment_internal", "bea_wls_internal", "_async", "Mejb", "bea_wls_diagnostics", "bea_wls_management_internal", "bea_wls_management_internal2", "uddi", "uddiexplorer", "wls-wsat");	

	/**
	 * "Time" property value units type
	 */
	public final static String TIME_UNITS = "Time";
	
	/**
	 * "Number" property value units type
	 */
	public final static String NUMBER_UNITS = "Number";

	/**
	 * "Number (millions)" property value units type
	 */
	public final static String NUMBER_MILLION_UNITS = "Number (millions)";

	/**
	 * "Requests per second" property value units type
	 */
	public final static String REQS_PER_SEC_UNITS = "Requests per second";

	/**
	 * "State" property value units type
	 */
	public final static String STATE_UNITS = "State";

	/**
	 * "Megabytes" property value units type
	 */
	public final static String MEGABYTES_UNITS = "Megabytes";

	/**
	 * "Bytes" property value units type
	 */
	public final static String BYTES_UNITS = "Bytes";

	/**
	 * "Percent" property value units type
	 */
	public final static String PERCENT_UNITS = "Percent";

	/**
	 * "Seconds" property value units type
	 */
	public final static String SECONDS_UNITS = "Seconds";

	/**
	 * "Milliseconds" property value units type
	 */
	public final static String MILLISECONDS_UNITS = "Milliseconds";

	/**
	 * "Name" property value units type
	 */
	public final static String NAME_UNITS = "Name";

	// Constants
	private final static Map<String, WLProperty> propList = new HashMap<String, WLProperty>();
	
	// Static initialiser of the map of WebLogic properties
	static {
		// Add Time property
		propList.put(DATE_TIME, new WLProperty(DATE_TIME, "Date-Time", TIME_UNITS));

		// Add Core properties
		propList.put(SERVER_STATE, new WLProperty(SERVER_STATE, "Server State", STATE_UNITS));
		propList.put(OPEN_SOCKETS, new WLProperty(OPEN_SOCKETS, "Open Sockets", NUMBER_UNITS));
		propList.put(HEAP_SIZE_CURRENT, new WLProperty(HEAP_SIZE_CURRENT, "Heap Size", MEGABYTES_UNITS)); 
		propList.put(HEAP_FREE_CURRENT, new WLProperty(HEAP_FREE_CURRENT, "Heap Free", MEGABYTES_UNITS)); 
		propList.put(HEAP_USED_CURRENT, new WLProperty(HEAP_USED_CURRENT, "Heap Used", MEGABYTES_UNITS)); 
		propList.put(HEAP_FREE_PERCENT, new WLProperty(HEAP_FREE_PERCENT, "Heap Free", PERCENT_UNITS)); 
		//>>new in 1.1.7 version
		propList.put(JVM_PROCESSOR_LOAD, new WLProperty(JVM_PROCESSOR_LOAD, "jvm proc load", PERCENT_UNITS));
		propList.put(TOTAL_GC_COUNT, new WLProperty(TOTAL_GC_COUNT, "GC count", NUMBER_UNITS));
		propList.put(TOTAL_GC_TIME, new WLProperty(TOTAL_GC_TIME, "GC time", MILLISECONDS_UNITS));
		propList.put(TOTAL_NURSERY_SIZE, new WLProperty(TOTAL_NURSERY_SIZE, "Heap Free", MEGABYTES_UNITS));
		propList.put(HEAP_SIZE_MAX, new WLProperty(HEAP_SIZE_MAX, "Heap Max", MEGABYTES_UNITS));
		//<<
		propList.put(EXECUTE_THREAD_TOTAL_COUNT, new WLProperty(EXECUTE_THREAD_TOTAL_COUNT, "Thread Pool Execute Threads", NUMBER_UNITS)); 
		propList.put(HOGGING_THREAD_COUNT, new WLProperty(HOGGING_THREAD_COUNT, "Thread Pool Hogging Threads", NUMBER_UNITS)); 
		propList.put(PENDING_USER_REQUEST_COUNT, new WLProperty(PENDING_USER_REQUEST_COUNT, "Thread Pool Pending User Requests", NUMBER_UNITS)); 
		propList.put(THREAD_POOL_QUEUE_LENGTH, new WLProperty(THREAD_POOL_QUEUE_LENGTH, "Thread Pool Queue Length", NUMBER_UNITS)); 
		propList.put(COMPLETED_REQUEST_COUNT, new WLProperty(COMPLETED_REQUEST_COUNT, "Thread Pool Completed Requests", NUMBER_UNITS)); 
		propList.put(EXECUTE_THREAD_IDLE_COUNT, new WLProperty(EXECUTE_THREAD_IDLE_COUNT, "Thread Pool Idle Threads", NUMBER_UNITS)); 
		propList.put(MIN_THREADS_CONSTRAINT_COMPLETED, new WLProperty(MIN_THREADS_CONSTRAINT_COMPLETED, "Thread Pool Min Threads Constraint Completed", NUMBER_UNITS)); 
		propList.put(MIN_THREADS_CONSTRAINT_PENDING, new WLProperty(MIN_THREADS_CONSTRAINT_PENDING, "Thread Pool Min Threads Constraint Pending", NUMBER_UNITS)); 
		propList.put(STANDBY_THREAD_COUNT, new WLProperty(STANDBY_THREAD_COUNT, "Thread Pool Standby Threads", NUMBER_UNITS));
		propList.put(THROUGHPUT, new WLProperty(THROUGHPUT, "Thread Pool Throughput", REQS_PER_SEC_UNITS)); 		
		propList.put(TRANSACTION_TOTAL_COUNT, new WLProperty(TRANSACTION_TOTAL_COUNT, "Transactions Total", NUMBER_UNITS)); 
		propList.put(TRANSACTION_COMMITTED_COUNT, new WLProperty(TRANSACTION_COMMITTED_COUNT, "Transaction Committed", NUMBER_UNITS)); 
		propList.put(TRANSACTION_ROLLEDBACK_COUNT, new WLProperty(TRANSACTION_ROLLEDBACK_COUNT, "Transaction RolledBack", NUMBER_UNITS)); 
		propList.put(TRANSACTION_HEURISTICS_TOTAL_COUNT, new WLProperty(TRANSACTION_HEURISTICS_TOTAL_COUNT, "Transaction Heuristics", NUMBER_UNITS)); 
		propList.put(TRANSACTION_ABANDONED_TOTAL_COUNT, new WLProperty(TRANSACTION_ABANDONED_TOTAL_COUNT, "Transaction Abandoned", NUMBER_UNITS)); 
		propList.put(TRANSACTIONS_ACTIVE_TOTAL_COUNT, new WLProperty(TRANSACTIONS_ACTIVE_TOTAL_COUNT, "Transactions Active", NUMBER_UNITS));

		// Add JDBC Data Source properties
		propList.put(NUM_AVAILABLE, new WLProperty(NUM_AVAILABLE, "Number Available", NUMBER_UNITS)); 
		propList.put(NUM_UNAVAILABLE, new WLProperty(NUM_UNAVAILABLE, "Number Unavailable", NUMBER_UNITS)); 
		propList.put(ACTIVE_CONNECTONS_CURRENT_COUNT, new WLProperty(ACTIVE_CONNECTONS_CURRENT_COUNT, "Active Connections", NUMBER_UNITS)); 
		propList.put(CONNECTION_DELAY_TIME, new WLProperty(CONNECTION_DELAY_TIME, "Average Connection Delay", MILLISECONDS_UNITS)); 
		propList.put(FAILED_RESERIVE_REQUEST_COUNT, new WLProperty(FAILED_RESERIVE_REQUEST_COUNT, "Failed Reserve Request", NUMBER_UNITS)); 
		propList.put(FAILURES_TO_RECONNECT_COUNT, new WLProperty(FAILURES_TO_RECONNECT_COUNT, "Failures To Reconnect", NUMBER_UNITS)); 
		propList.put(LEAKED_CONNECTION_COUNT, new WLProperty(LEAKED_CONNECTION_COUNT, "Leaked Connections", NUMBER_UNITS)); 
		propList.put(WAITING_FOR_CONNECTION_CURRENT_COUNT, new WLProperty(WAITING_FOR_CONNECTION_CURRENT_COUNT, "Waiting For Connection Current", NUMBER_UNITS)); 
		propList.put(WAITING_FOR_CONNECTION_FAILURES_TOTAL, new WLProperty(WAITING_FOR_CONNECTION_FAILURES_TOTAL, "Waiting For Connection Failures", NUMBER_UNITS)); 
		propList.put(WAITING_SECONDS_HIGH_COUNT, new WLProperty(WAITING_SECONDS_HIGH_COUNT, "Wait Seconds High", SECONDS_UNITS)); 				

		// Add JMS Destination properties
		propList.put(MESSAGES_CURRENT_COUNT, new WLProperty(MESSAGES_CURRENT_COUNT, "Messages Current", NUMBER_UNITS)); 
		propList.put(MESSAGES_PENDING_COUNT, new WLProperty(MESSAGES_PENDING_COUNT, "Messages Pending", NUMBER_UNITS)); 
		propList.put(MESSAGES_RECEIVED_COUNT, new WLProperty(MESSAGES_RECEIVED_COUNT, "Messages Received", NUMBER_UNITS)); 
		propList.put(MESSAGES_HIGH_COUNT, new WLProperty(MESSAGES_HIGH_COUNT, "Messages High", NUMBER_UNITS)); 
		propList.put(CONSUMERS_CURRENT_COUNT, new WLProperty(CONSUMERS_CURRENT_COUNT, "Consumers Current", NUMBER_UNITS)); 
		propList.put(CONSUMERS_HIGH_COUNT, new WLProperty(CONSUMERS_HIGH_COUNT, "Consumers High", NUMBER_UNITS));
		propList.put(CONSUMERS_TOTAL_COUNT, new WLProperty(CONSUMERS_TOTAL_COUNT, "Consumers Total", NUMBER_UNITS));

		// Add WebApp properties
		propList.put(SESSIONS_CURRENT_COUNT, new WLProperty(SESSIONS_CURRENT_COUNT, "Open Sessions Current", NUMBER_UNITS)); 
		propList.put(SESSIONS_HIGH_COUNT, new WLProperty(SESSIONS_HIGH_COUNT, "Open Sessions High", NUMBER_UNITS)); 
		propList.put(SESSIONS_TOTAL_COUNT, new WLProperty(SESSIONS_TOTAL_COUNT, "Open Sessions Total", NUMBER_UNITS)); 

		// Add EJB properties
		propList.put(BEANS_POOLED_CURRENT_COUNT, new WLProperty(BEANS_POOLED_CURRENT_COUNT, "Pooled Instances Current", NUMBER_UNITS)); 
		propList.put(BEAN_ACCESS_TOTAL_COUNT, new WLProperty(BEAN_ACCESS_TOTAL_COUNT, "Bean Access Total", NUMBER_UNITS)); 
		propList.put(BEANS_INUSE_CURRENT_COUNT, new WLProperty(BEANS_INUSE_CURRENT_COUNT, "Instances In Use Current", NUMBER_UNITS)); 
		propList.put(BEAN_WAITING_CURRENT_COUNT, new WLProperty(BEAN_WAITING_CURRENT_COUNT, "Waiting For Instance Current", NUMBER_UNITS)); 
		propList.put(BEAN_WAITING_TOTAL_COUNT, new WLProperty(BEAN_WAITING_TOTAL_COUNT, "Waiting For Instances Total", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, "Transactions Committed", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, "Transactions Rolledback", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT, "Transactions Timedout", NUMBER_UNITS)); 
				
		// Add Work Manager properties
		propList.put(COMPLETED_REQUESTS, new WLProperty(COMPLETED_REQUESTS, "Completed Requests", NUMBER_UNITS)); 
		propList.put(PENDING_REQUESTS, new WLProperty(PENDING_REQUESTS, "Pending Requests", NUMBER_UNITS)); 
		propList.put(STUCK_THREAD_COUNT, new WLProperty(STUCK_THREAD_COUNT, "Stuck Threads", NUMBER_UNITS)); 		

		// Add Server Channel properties
		propList.put(ACCEPT_COUNT, new WLProperty(ACCEPT_COUNT, "Accept Count", NUMBER_UNITS)); 	
		propList.put(CONNECTIONS_COUNT, new WLProperty(CONNECTIONS_COUNT, "Connections Count", NUMBER_UNITS)); 	
		propList.put(CHNL_MESSAGES_RECEIVED_COUNT, new WLProperty(CHNL_MESSAGES_RECEIVED_COUNT, "Messages Received", NUMBER_UNITS)); 	
		propList.put(CHNL_MESSAGES_SENT_COUNT, new WLProperty(CHNL_MESSAGES_SENT_COUNT, "Messages Sent", NUMBER_UNITS));

		// Add WLHostMachineStats properties (optional deployed custom MBean)
		propList.put(JVM_INSTANCE_CORES_USED, new WLProperty(JVM_INSTANCE_CORES_USED, "JVM Instance Cores Used", NUMBER_UNITS)); 	
		propList.put(JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, new WLProperty(JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, "JVM Instance Physical Memory Used", MEGABYTES_UNITS)); 	
		propList.put(NATIVE_PROCESSES_COUNT, new WLProperty(NATIVE_PROCESSES_COUNT, "Native Processes Count", NUMBER_UNITS));
		propList.put(NETWORK_RX_MEGABYTES, new WLProperty(NETWORK_RX_MEGABYTES, "Network Rx Size", MEGABYTES_UNITS)); 	
		propList.put(NETWORK_RX_DROPPED, new WLProperty(NETWORK_RX_DROPPED, "Network Rx Dropped", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_ERRORS, new WLProperty(NETWORK_RX_ERRORS, "Network Rx Errors", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_FRAME, new WLProperty(NETWORK_RX_FRAME, "Network Rx Frame", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_OVERRUNS, new WLProperty(NETWORK_RX_OVERRUNS, "Network Rx Overruns", NUMBER_UNITS)); 	
		propList.put(NETWORK_MILLIONS_RX_PACKETS, new WLProperty(NETWORK_MILLIONS_RX_PACKETS, "Network Rx Packets", NUMBER_MILLION_UNITS)); 	
		propList.put(NETWORK_TX_MEGABYTES, new WLProperty(NETWORK_TX_MEGABYTES, "Network Tx Size", MEGABYTES_UNITS)); 	
		propList.put(NETWORK_TX_CARRIER, new WLProperty(NETWORK_TX_CARRIER, "Network Tx Carrier", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_COLLISIONS, new WLProperty(NETWORK_TX_COLLISIONS, "NetworkTxCollisions", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_DROPPED, new WLProperty(NETWORK_TX_DROPPED, "Network Tx Dropped", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_ERRORS, new WLProperty(NETWORK_TX_ERRORS, "Network Tx Errors", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_OVERRUNS, new WLProperty(NETWORK_TX_OVERRUNS, "Network Tx Overruns", NUMBER_UNITS)); 	
		propList.put(NETWORK_MILLIONS_TX_PACKETS, new WLProperty(NETWORK_MILLIONS_TX_PACKETS, "Network Tx Packets", NUMBER_MILLION_UNITS)); 	
		propList.put(PHYSICAL_MEMORY_USED_PERCENT, new WLProperty(PHYSICAL_MEMORY_USED_PERCENT, "Physical Memory Used", PERCENT_UNITS)); 	
		propList.put(PHYSICAL_SWAP_USED_PERCENT, new WLProperty(PHYSICAL_SWAP_USED_PERCENT, "Physical Swap Used", PERCENT_UNITS)); 	
		propList.put(PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, new WLProperty(PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, "Average Processor Workload", NUMBER_UNITS)); 	
		propList.put(PROCESSOR_USAGE_PERCENT, new WLProperty(PROCESSOR_USAGE_PERCENT, "Processor Usage", PERCENT_UNITS)); 	
		propList.put(ROOT_FILESYSTEM_USED_PERCENT, new WLProperty(ROOT_FILESYSTEM_USED_PERCENT, "Root Filesystem Used", PERCENT_UNITS)); 	
		propList.put(TCP_CLOSE_WAIT_COUNT, new WLProperty(TCP_CLOSE_WAIT_COUNT, "Tcp Close Wait Count", NUMBER_UNITS)); 	
		propList.put(TCP_ESTABLISHED_COUNT, new WLProperty(TCP_ESTABLISHED_COUNT, "Tcp Established Count", NUMBER_UNITS)); 	
		propList.put(TCP_LISTEN_COUNT, new WLProperty(TCP_LISTEN_COUNT, "Tcp Listen Count", NUMBER_UNITS)); 	
		propList.put(TCP_TIME_WAIT_COUNT, new WLProperty(TCP_TIME_WAIT_COUNT, "Tcp Time Wait Count", NUMBER_UNITS)); 	
	}
}

/**
 * Encapsulates a WebLogic property key-title-units triplet
 */
class WLProperty {
	/**
	 * Creates a new WebLogic property
	 * 
	 * @param key Property key
	 * @param title Property title
	 * @param units Property units
	 */
	public WLProperty(String key, String title, String units) {
		this.key = key;
		this.title = title;
		this.units = units;
	}

	/**
	 * Gets the key for the property
	 * 
	 * @return Property key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the title for the property
	 * 
	 * @return Property title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Gets the units for the property
	 * 
	 * @return Property units
	 */
	public String getUnits() {
		return units;
	}

	// Members
	private final String key;
	private final String title;
	private final String units;
}
