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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import javax.management.openmbean.CompositeData;
import java.lang.management.MemoryUsage;

import java.util.Set;
import java.util.Iterator;


import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;

import static domainhealth.core.jmx.JavaMBeanPropConstants.*;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.statistics.StatisticsStorage.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;
import domainhealth.backend.retriever.HeaderLine;



/**
 * Base class implementation for a concrete implementation class which will 
 * capture a specific WebLogic server's Core, JDBC and JMS and other 
 * statistics. An implementation may use JMX Polling of WLDF Harvesting to 
 * obtain statistics, for example. The implementation stores the captured 
 * statistics in a set of CSV files.
 */
public abstract class StatisticCapturer {
	/**
	 * Base class constructor for statistic retriever logger implementation
	 * which stores the WebLogic server connection details.
	 * 
	 * @param appProps The system/application key/value pairs
	 * @param csvStats Meta-data about the server statistics CSV file being generated
	 * @param conn Connection to the server's MBean tree
	 * @param serverRuntime Handle on the server's main runtime MBean
	 * @param serverName Name of the server to retrieve statistics for
	 * @param componentBlacklist Names of web-apps/ejbs than should not haves results collected/shown
	 * @param metricTypeSet metric types to collect
	 * @param wlsVersionNumber The version of the host WebLogic Domain
	 */
	public StatisticCapturer(StatisticsStorage csvStats, WebLogicMBeanConnection conn, ObjectName serverRuntime, String serverName, int queryIntervalMillis, List<String> componentBlacklist, List<String> metricTypeSet,String wlsVersionNumber,String jvmVersion) {
		this.csvStats = csvStats;
		this.conn = conn;
		this.serverRuntime = serverRuntime;
		this.serverName = serverName;
		this.queryIntervalMillis = queryIntervalMillis;
		this.componentBlacklist = componentBlacklist;
		this.wlsVersionNumber = wlsVersionNumber;
		this.metricTypeSet = metricTypeSet;
		this.jvmVersion	= jvmVersion;
		/*TODO array de strings con cabeceras de cada tipo preparadas.., para no construirlas cada vez, SON ESTATICAS!!*/

	}

	/**
	 * "Template Method" based pattern. Main controlling method which calls 
	 * implementation class methods for obtaining each category of statistic (
	 * eg. JDBC, JMS).
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	public final void captureAndLogServerStats() throws DataRetrievalException, IOException {
		try {
		AppLog.getLogger().debug(getClass() + " initiated to collect stats for server:" + serverName);
		//TODO: execute reflected methods ?
		for (String type : metricTypeSet) {
 		   if(type.equalsIgnoreCase("jvm")){
			//Only for JVM 1.5 /1.6 and above 
			if(Integer.parseInt(jvmVersion)>=5)	logJvmStats();
 		   } else if(type.equalsIgnoreCase("core")){
			logCoreStats();
    		   } else if (type.equalsIgnoreCase("datasource")) {
			logDataSourcesStats();	
		   } else if (type.equalsIgnoreCase("jmsdestination")) {
			logDestinationsStats();
		   } else if (type.equalsIgnoreCase("webapp")) {
			logWebAppStats();
		   } else if (type.equalsIgnoreCase("ejb")) {
			logEJBStats();
		   } else if(type.equalsIgnoreCase("hostmachine")) {
			logHostMachineStats();
		   } else if(type.equalsIgnoreCase("extended")) {
			logExtendedStats();
		   }
			
		}
		} catch ( DataRetrievalException  e) { 
		/*this avoid exceptions on the main application server systemout.log as we want only log in our log4j logger*/
		} 
	}

	public final void setHost(String hostName)  {
		if( (hostName == null) || (hostName.length()==0))  AppLog.getLogger().info(getClass() + " initiated to collect stats for machine server: <HOSTNAME_NOT_SET_IN_CONFIG>");
		else AppLog.getLogger().info(getClass() + " initiated to collect stats for machine server:" + hostName);
		this.hostName=hostName;
	}

	/**
	 * Abstract method for capturing and persisting JVM server statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
//	protected abstract void logJvmStats() throws DataRetrievalException;
//	protected abstract String getJvmStatsLine() throws WebLogicMBeanException;

       protected void logJvmStats() throws DataRetrievalException {
                try {
                        String headerLine = getJvmStatsHeaderLine();
                        String contentLine = getJvmStatsLine();
                        getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), JVM_RESOURCE_TYPE, CORE_RSC_DEFAULT_NAME, headerLine, contentLine,getHostName()); } catch (Exception e) {
                        throw new DataRetrievalException("Problem logging " + CORE_RESOURCE_TYPE + " resources for server " + getServerName(), e);
                }
        }

        protected String getJvmStatsLine() throws WebLogicMBeanException {
                StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);

                //cl
                long j_current_loaded_class_count=0;
                long j_total_loaded_class_count=0;
                long j_total_unloaded_class_count=0;
                //comp
                long j_total_compilation_time_class=0;
                //gc
                long j_old_collection_count=0;
                long j_old_collection_time=0;
                long j_young_collection_count=0;
                long j_young_collection_time=0;
                //mem (MB)
                double j_heap_committed=0;
                double j_heap_init=0;
                double j_heap_max=0;
                double j_heap_used=0;

                double j_not_heap_committed=0;
                double j_not_heap_init=0;
                double j_not_heap_max=0;
                double j_not_heap_used=0;
                //memp (MB)
                double j_mempool_cm_committed=0;
                double j_mempool_cm_init=0;
                double j_mempool_cm_max=0;
                double j_mempool_cm_used=0;

                double j_mempool_cb_committed=0;
                double j_mempool_cb_init=0;
                double j_mempool_cb_max=0;
                double j_mempool_cb_used=0;

                double j_mempool_nursery_committed=0;
                double j_mempool_nursery_init=0;
                double j_mempool_nursery_max=0;
                double j_mempool_nursery_used=0;

                double j_mempool_old_committed=0;
                double j_mempool_old_init=0;
                double j_mempool_old_max=0;
                double j_mempool_old_used=0;
                //thread
                long j_cur_daemon_thread_count=0;
                long j_cur_non_daemon_thread_count=0;
                long j_cur_total_thread_count=0;
                long j_total_started_thread_count=0;


                // Date-time
                line.append(formatSeconsdDateTime(new Date()) + SEPARATOR);

                // Server attributes (not looping because state attr is not a num unlike all other attrs)

                String curServer=getServerName();
		AppLog.getLogger().info("Begin JVM data gather process for server: "+curServer);

                try  {

                        String clName = String.format("java.lang:Location=%s,type=ClassLoading", curServer);
                        ObjectName clMBean = new ObjectName(clName);
			AppLog.getLogger().info("ClassLoading Objectname : "+clMBean.toString());


                        if(clMBean != null ) {
                                j_current_loaded_class_count=(long)getConn().getNumberAttr(clMBean,"LoadedClassCount");
                                j_total_loaded_class_count=(long)getConn().getNumberAttr(clMBean,"TotalLoadedClassCount");
                                j_total_unloaded_class_count=(long) getConn().getNumberAttr(clMBean,"UnloadedClassCount");
                                 AppLog.getLogger().debug("Class Loaded:"+j_current_loaded_class_count +" Total:"+ j_total_loaded_class_count+ " Unloaded:" +j_total_unloaded_class_count );
                        }



                        String compName = String.format("java.lang:Location=%s,type=Compilation", curServer);
                        ObjectName compMBean = new ObjectName(compName);
                        if(compMBean != null ) {
                                j_total_compilation_time_class=(long)getConn().getNumberAttr(compMBean,"TotalCompilationTime");
                                 AppLog.getLogger().debug("COMPILED TIME:"+j_total_compilation_time_class);
                        }



                        //String gcName = String.format("java.lang:Location=%s,type=GarbageCollector,name=*", curServer);
			String gcName = String.format("java.lang:Location=%s,type=GarbageCollector,*", curServer);
                        Set<ObjectName> gcSet = getConn().queryNames(new ObjectName(gcName));

                        AppLog.getLogger().debug("Query Garbage Collector size:"+gcSet.size());

                        for (ObjectName objName : gcSet ) {
                                String name = objName.getKeyProperty("name");
                                AppLog.getLogger().debug("GC Query NAme :"+name+ "Canonical Name: "+ objName.getCanonicalName());
                                long cc=(long)getConn().getNumberAttr(objName,"CollectionCount"); //#collections count (-1 if undefinded)
                                long ct=(long)getConn().getNumberAttr(objName,"CollectionTime");
                                 AppLog.getLogger().debug("Found GC"+name+ "COUNT: "+cc+ " TIME: "+ct);
                                if(name.matches("(?i).*Old.*")) {
                                        j_old_collection_count=cc;
                                        j_old_collection_time=ct;
                                         AppLog.getLogger().debug("Found GC OLD:"+name+ "COUNT: "+cc+ " TIME: "+ct);

                                } else if (name.matches("(?i).*Young.*")) {
                                        j_young_collection_count=cc;
                                        j_young_collection_time=ct;
                                         AppLog.getLogger().debug("Found GC Young:"+name+ "COUNT: "+cc+ " TIME: "+ct);
                                }
                        }
                        String memName = String.format("java.lang:Location=%s,type=Memory", curServer);
                        ObjectName memMBean = new ObjectName(memName);
                        long finalize_pending   =(long)getConn().getNumberAttr(memMBean,"ObjectPendingFinalizationCount");
                        CompositeData heap      =(CompositeData)getConn().getObjectAttr(memMBean,"HeapMemoryUsage");
                        CompositeData non_heap  =(CompositeData)getConn().getObjectAttr(memMBean,"NonHeapMemoryUsage");

                        MemoryUsage mh  =MemoryUsage.from(heap);
                        j_heap_committed=((double) mh.getCommitted() /BYTES_IN_MEGABYTE);
                        j_heap_init     =((double) mh.getInit() /BYTES_IN_MEGABYTE);
                        j_heap_max      =((double) mh.getMax() /BYTES_IN_MEGABYTE);
                        j_heap_used     =((double) mh.getUsed() /BYTES_IN_MEGABYTE);
                        AppLog.getLogger().debug("HEAP init:"+j_heap_init+ " max: "+j_heap_max+ " used: "+j_heap_used+ " committed: "+j_heap_committed);

                        MemoryUsage mn  =MemoryUsage.from(non_heap);
                        j_not_heap_committed	=((double)mn.getCommitted() /BYTES_IN_MEGABYTE);
                        j_not_heap_init 	=((double)mn.getInit()/BYTES_IN_MEGABYTE);
                        j_not_heap_max  	=((double)mn.getMax()/BYTES_IN_MEGABYTE);
                        j_not_heap_used 	=((double)mn.getUsed()/BYTES_IN_MEGABYTE);
                        AppLog.getLogger().debug("NON HEAP init:"+j_not_heap_init+ " max: "+j_not_heap_max+ " used: "+j_not_heap_used+ " committed: "+j_not_heap_committed);

                        //String mpName = String.format("java.lang:Location=%s,type=MemoryPool,name=*", curServer);
			String mpName = String.format("java.lang:Location=%s,type=MemoryPool,*", curServer);
                        Set<ObjectName> mpSet = getConn().queryNames(new ObjectName(mpName));
                        AppLog.getLogger().debug("Query Memory Pool size:"+mpSet.size());

                        for (ObjectName objName : mpSet ) {
                                String name = objName.getKeyProperty("name");
                                AppLog.getLogger().debug("Memory Pool Name :"+name+ "Canonical Name: "+ objName.getCanonicalName());

                                CompositeData usage     =(CompositeData) getConn().getObjectAttr(objName,"Usage");
                                MemoryUsage mu  = MemoryUsage.from(usage);
                                long usage_committed    =mu.getCommitted();
                                long usage_init         =mu.getInit();
                                long usage_max          =mu.getMax();
                                long usage_used         =mu.getUsed();

                                 AppLog.getLogger().debug("Found MEMPOOL:"+name+ "INIT: "+usage_init+" USED: "+usage_used+" Committed: "+ usage_committed + "MAX  "+usage_max);
                                if(name.matches("(?i).*Class Memory.*")) {
                                        j_mempool_cm_committed  =((double)usage_committed/BYTES_IN_MEGABYTE);
                                        j_mempool_cm_init       =((double)usage_init/BYTES_IN_MEGABYTE);
                                        j_mempool_cm_max        =((double)usage_max/BYTES_IN_MEGABYTE);
                                        j_mempool_cm_used       =((double)usage_used/BYTES_IN_MEGABYTE);

                                } else if (name.matches("(?i).*ClassBlock Memory.*")) {
                                        j_mempool_cb_committed  =((double)usage_committed/BYTES_IN_MEGABYTE);
                                        j_mempool_cb_init       =((double)usage_init/BYTES_IN_MEGABYTE);
                                        j_mempool_cb_max        =((double)usage_max/BYTES_IN_MEGABYTE);
                                        j_mempool_cb_used       =((double)usage_used/BYTES_IN_MEGABYTE);
                                } else if (name.matches("(?i).*Nursery.*")) {
                                        j_mempool_nursery_committed     =((double)usage_committed/BYTES_IN_MEGABYTE);
                                        j_mempool_nursery_init          =((double)usage_init/BYTES_IN_MEGABYTE);
                                        j_mempool_nursery_max           =((double)usage_max/BYTES_IN_MEGABYTE);
                                        j_mempool_nursery_used          =((double)usage_used/BYTES_IN_MEGABYTE);

                                }else if (name.matches("(?i).*Old.*")) {
                                        j_mempool_old_committed         =((double)usage_committed/BYTES_IN_MEGABYTE);
                                        j_mempool_old_init              =((double)usage_init/BYTES_IN_MEGABYTE);
                                        j_mempool_old_max               =((double)usage_max/BYTES_IN_MEGABYTE);
                                        j_mempool_old_used              =((double)usage_used/BYTES_IN_MEGABYTE);

                                }

                        }
                        String thrName = String.format("java.lang:Location=%s,type=Threading", curServer);
                        ObjectName thrMBean = new ObjectName(thrName);

                        j_cur_daemon_thread_count       =(long) getConn().getNumberAttr(thrMBean,"DaemonThreadCount");
                        j_cur_total_thread_count        =(long) getConn().getNumberAttr(thrMBean,"ThreadCount");
                        j_cur_non_daemon_thread_count   =j_cur_total_thread_count-j_cur_daemon_thread_count;
                        j_total_started_thread_count    =(long) getConn().getNumberAttr(thrMBean,"TotalStartedThreadCount");

                         AppLog.getLogger().debug(" Thread  count: TOTAL: "+j_cur_total_thread_count+" DAEMON: "+j_cur_daemon_thread_count+ "STARTED: "+j_total_started_thread_count);




                } catch (Exception e) {
			 AppLog.getLogger().error("ERROR on get getJvmStatsLine data in server :"+curServer);
                         AppLog.getLogger().error(e.toString(),e);
                }

                //class loader
                line.append(j_current_loaded_class_count + SEPARATOR); //J_CURRENT_LOADED_CLASS_COUNT
                line.append(j_total_loaded_class_count + SEPARATOR); //J_TOTAL_LOADED_CLASS_COUNT
                line.append(j_total_unloaded_class_count + SEPARATOR); //J_TOTAL_UNLOADED_CLASS_COUNT
                //comp
                line.append(j_total_compilation_time_class + SEPARATOR); //J_TOTAL_COMPILATION_TIME_CLASS
                //GC
                line.append(j_old_collection_count + SEPARATOR); //J_OLD_COLLECTION_COUNT
                line.append(j_old_collection_time + SEPARATOR); //J_OLD_COLLECTION_TIME
                line.append(j_young_collection_count + SEPARATOR); //J_YOUNG_COLLECTION_COUNT
                line.append(j_young_collection_time + SEPARATOR); //J_YOUNG_COLLECTION_TIME
                //Mem
                line.append(j_heap_committed + SEPARATOR); //J_HEAP_COMMITTED
                line.append(j_heap_init + SEPARATOR); //J_HEAP_INIT
                line.append(j_heap_max + SEPARATOR); //J_HEAP_MAX
                line.append(j_heap_used + SEPARATOR); //J_HEAP_USED

                line.append(j_not_heap_committed + SEPARATOR); //J_NOT_HEAP_COMMITTED
                line.append(j_not_heap_init + SEPARATOR); //J_NOT_HEAP_INIT
                line.append(j_not_heap_max + SEPARATOR); //J_NOT_HEAP_MAX
                line.append(j_not_heap_used + SEPARATOR); //J_NOT_HEAP_USED
                //MemPool
                line.append(j_mempool_cm_committed + SEPARATOR); //J_MEMPOOL_CM_COMMITTED
                line.append(j_mempool_cm_init + SEPARATOR); //J_MEMPOOL_CM_INIT
                line.append(j_mempool_cm_max + SEPARATOR); //J_MEMPOOL_CM_MAX
                line.append(j_mempool_cm_used + SEPARATOR); //J_MEMPOOL_CM_USED

                line.append(j_mempool_cb_committed + SEPARATOR); //J_MEMPOOL_CB_COMMITTED
                line.append(j_mempool_cb_init + SEPARATOR); //J_MEMPOOL_CB_INIT
                line.append(j_mempool_cb_max + SEPARATOR); //J_MEMPOOL_CB_MAX
                line.append(j_mempool_cb_used + SEPARATOR); //J_MEMPOOL_CB_USED

                line.append(j_mempool_nursery_committed + SEPARATOR); //J_MEMPOOL_NURSERY_COMMITTED
                line.append(j_mempool_nursery_init + SEPARATOR); //J_MEMPOOL_NURSERY_INIT
                line.append(j_mempool_nursery_max + SEPARATOR); //J_MEMPOOL_NURSERY_MAX
                line.append(j_mempool_nursery_used + SEPARATOR); //J_MEMPOOL_NURSERY_USED

                line.append(j_mempool_old_committed + SEPARATOR); //J_MEMPOOL_OLD_COMMITTED
                line.append(j_mempool_old_init + SEPARATOR); //J_MEMPOOL_OLD_INIT
                line.append(j_mempool_old_max + SEPARATOR); //J_MEMPOOL_OLD_MAX
                line.append(j_mempool_old_used + SEPARATOR); //J_MEMPOOL_OLD_USED

                //thread
                line.append(j_cur_daemon_thread_count + SEPARATOR); //J_CUR_DAEMON_THREAD_COUNT
                line.append(j_cur_non_daemon_thread_count + SEPARATOR); //J_CUR_NON_DAEMON_THREAD_COUNT
                line.append(j_cur_total_thread_count + SEPARATOR); //J_CUR_TOTAL_THREAD_COUNT
                line.append(j_total_started_thread_count + SEPARATOR); //J_TOTAL_STARTED_THREAD_COUNT

                return line.toString();


        }



	/**
	 * Abstract method for capturing and persisting core server statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logCoreStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting JDBC data source 
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logDataSourcesStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting JMS destination
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logDestinationsStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting Web Application 
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logWebAppStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting EJB statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logEJBStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting WLHostMachine optional mbean statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logHostMachineStats() throws DataRetrievalException;


	/**
	 * Abstract method for capturing and persisting other types of server 
	 * statistics which are specific to the particular implementation of the 
	 * statistics capturer.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logExtendedStats() throws DataRetrievalException, IOException;

	/**
	 * Returns a text line containing the comma separated statistic field 
	 * headers for core server statistics.
	 * 
	 * @return The CVS field name header
	 */
	/*TODO: poner un string constante para cada tipo desde la creacion de la clase*/

	protected String getJvmStatsHeaderLine() {
		StringBuilder headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
		headerLine.append(DATE_TIME + SEPARATOR);	

		for (String attr : J_CLASSLOADING_MBEAN_ATTR_LIST ) {
			headerLine.append(attr + SEPARATOR);
		}			

		// Got to do these separately because adding calculated filed for heap size current
                for (String attr : J_COMPILATION_MBEAN_ATTR_LIST) {
                        headerLine.append(attr + SEPARATOR);
                }
		
		for (String attr : J_GARBAGECOLLECTOR_MBEAN_ATTR_LIST ) {
			headerLine.append(attr + SEPARATOR);
		}			

		for (String attr : J_MEMORY_MBEAN_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		for (String attr : J_MEMORYPOOL_MBEAN_ATTR_LIST ) {
			headerLine.append(attr + SEPARATOR);
		}			

		for (String attr : J_TREAD_MBEAN_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		return headerLine.toString();
	}


	/**
	 * Returns a text line containing the comma separated statistic field 
	 * headers for core server statistics.
	 * 
	 * @return The CVS field name header
	 */

	/*TODO: poner un string constante para cada tipo desde la creacion de la clase*/

	protected String getCoreStatsHeaderLine() {
		StringBuilder headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
		headerLine.append(DATE_TIME + SEPARATOR);	

		for (String attr : SERVER_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		// Got to do these separately because adding calculated filed for heap size current
		headerLine.append(HEAP_SIZE_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_FREE_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_USED_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_FREE_PERCENT + SEPARATOR);
		
                for (String attr : JROCKIT_MBEAN_MONITOR_ATTR_LIST) {
                        headerLine.append(attr + SEPARATOR);
                }
		
		
		for (String attr : THREADPOOL_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		for (String attr : JTA_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		return headerLine.toString();
	}

	/**
	 * Construct the single header line to go in a CSV file, from a list of 
	 * attribute names.
	 * 
	 * @param attrList List of attributes
	 * @param estLength Approximate lenght of line
	 * @return The new header text line
	 */
	protected String constructHeaderLine(String[] attrList) {
		StringBuilder headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
		headerLine.append(DATE_TIME + SEPARATOR);		

		for (String attr : attrList) {
			headerLine.append(attr + SEPARATOR);
		}			

		return headerLine.toString();
	}

	/**
	 * Returns the Meta-data about the server statistics CSV file being generated
	 * 
	 * @return Meta-data about the server statistics CSV file being generated
	 */
	protected StatisticsStorage getCSVStats() {
		return csvStats;
	}

	/**
	 * Returns the connection to the server's MBean tree
	 * 
	 * @return Connection to the server's MBean tree
	 */
	protected WebLogicMBeanConnection getConn() {
		return conn;
	}

	/**
	 * Returns the handle on the server's main runtime MBean
	 * 
	 * @return Handle on the server's main runtime MBean
	 */
	protected ObjectName getServerRuntime() {
		return serverRuntime;
	}

	/**
	 * Returns the name of the server to retrieve statistics for
	 * 
	 * @return Name of the server to retrieve statistics for
	 */
	protected String getServerName() {
		return serverName;
	}
	/**
	 * Returns the name of the host to retrieve statistics for
	 * 
	 * @return Name of the machine to retrieve statistics for
	 */
	protected String getHostName() {
		return hostName;
	}


	/**
	 * Returns the statistic poll/query interval in milliseconds 
	 * 
	 * @return The query interval in milliseconds
	 */
	protected int getQueryIntervalMillis() {
		return queryIntervalMillis;
	}

	/**
	 * Returns the list of component names to be ignored (the blacklist) 
	 * 
	 * @return The blacklist of component names
	 */
	protected List<String> getComponentBlacklist() {
		return componentBlacklist;
	}

	/**
	 * Return the current host WebLogic Domain's version 
	 * 
	 * @return The version text (e.g. 10.3.5)
	 */
	protected String getWlsVersionNumber() {
		return wlsVersionNumber;
	}
	
	/**
	 * Single-threaded utility method to generate a data-time string from a 
	 * given date (including seconds in format)
	 * 
	 * @return Text representation of given date-time including seconds
	 */
	protected String formatSeconsdDateTime(Date dateTime) {
		return secondDateFormat.format(dateTime);
	}

	/**
	 * Single-threaded utility method to generate a data-time string from a 
	 * given milli-seconds version of a date (including seconds in format)
	 * 
	 * @return Text representation of given date-time including seconds
	 */
	protected String formatSecondsDateTime(long dateTimeMillis) {
		return secondDateFormat.format(new Date(dateTimeMillis));
	}

	/**
	 * Returns an MBean handle onto the default work manager for the server 
	 * 
	 * @return The default Work Manager MBean
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the statistics
	 */
	protected ObjectName getDefaultWorkManager() throws WebLogicMBeanException {
		for (ObjectName wkMgr : getConn().getChildren(getServerRuntime(), WORK_MANAGER_RUNTIMES)) {
			if (getConn().getTextAttr(wkMgr, NAME).equals(DEFAULT_WKMGR_NAME)) {
				return wkMgr;
			}
		}
		
		return null;
	}
	
	// Constants
        protected static final int DEFAULT_CONTENT_LINE_LEN = 100;

	private static final int DEFAULT_HEADER_LINE_LEN = 100;
	protected static final long BYTES_IN_MEGABYTE = 1024 * 1024;
	
	// Members
	private final StatisticsStorage csvStats;
	private final WebLogicMBeanConnection conn;
	private final ObjectName serverRuntime;
	private final String serverName;
	private String hostName;
	private final int queryIntervalMillis;
	private final List<String> componentBlacklist;
	private final List<String> metricTypeSet;
	private final String wlsVersionNumber;
	private final String jvmVersion;
	private final DateFormat secondDateFormat = new SimpleDateFormat(DateUtil.DISPLAY_DATETIME_FORMAT);

	public final static Map<String, HeaderLine> headerList= new HashMap<String, HeaderLine>();

	static {
		headerList.put("JVM",new HeaderLine(J_MBEAN_ALL));
		headerList.put("CORE_WITH_JVM",new HeaderLine(J_CORE_WITH_JVM));
		headerList.put("CORE_WITHOUT_JVM", new HeaderLine(J_CORE_WITHOUT_JVM)); 
		headerList.put("JDBC",new HeaderLine(JDBC_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("JMS",new HeaderLine(JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("WEBAPP",new HeaderLine(WEBAPP_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("EJB",new HeaderLine(EJB_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("HOST",new HeaderLine(HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("WKMGR",new HeaderLine(WKMGR_MBEAN_MONITOR_ATTR_LIST));
		headerList.put("SRVCHN",new HeaderLine(SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST));
	}
}


