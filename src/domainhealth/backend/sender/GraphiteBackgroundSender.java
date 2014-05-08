//Copyright (C) 2013-2013 Graphite Sender . All rights reserved.
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
package domainhealth.backend.sender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static domainhealth.core.env.AppProperties.*;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.ContextAwareWork;

//netty imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.bootstrap.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.jboss.netty.buffer.ChannelBuffers; 




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
public class GraphiteBackgroundSender {
	// Members

	// Constants

	private String carbon_host; //localhost
	private int carbon_port; //port
	private String metric_default_host;
	private String metric_host_prefix; //pro.site0.bbdd.
	private String metric_host_suffix; //wl
	private String metric_domain_name; //
	private boolean useDomainName; 
	private boolean metric_use_host;
	private static boolean is_shutting_down; 
	private int reconnect_timeout;
	private int send_buffer_size;

	//server status 
	private boolean map_server_stats;
	static final Map<String,Integer> serverStatusMap = new HashMap<String , Integer>() {{
    		put("SHUTDOWN",new Integer(0));
    		put("STARTING",new Integer(1));
    		put("STANDBY",new Integer(2));
    		put("ADMIN",new Integer(3));
    		put("RESUMING",new Integer(4));
    		put("RUNNING",new Integer(5));
    		put("SUSPENDING",new Integer(6));
    		put("SHUTTING_DOWN",new Integer(7));
    		put("FORCE_SUSPENDING",new Integer(8));
	}};

	//counter MAP
	private Map<String,Integer> counterMap;

	//netty objects
	private Executor bossPool;
	private Executor workerPool;
	public ChannelFactory channelFactory;
	public ClientBootstrap bootstrap;
	public ChannelFuture cf;
	//public Channel channel;
	public GraphiteClientPipelineFactory gpf;
	
	/**
	 * Create new service to send data to a graphite  the root path to write CSV file to
	 *  
	 * @param appProps The system/application key/value pairs
	 */
	public GraphiteBackgroundSender(AppProperties appProps) {
		AppLog.getLogger().info("initializing beggining Graphite sender ");
		
		is_shutting_down = false;

		this.carbon_host=appProps.getProperty(PropKey.GRAPHITE_CARBON_HOST_PROP);
		if(this.carbon_host == null ) this.carbon_host="localhost"; 
		 AppLog.getLogger().info("Graphite send host to:"+this.carbon_host);

		this.carbon_port=appProps.getIntProperty(PropKey.GRAPHITE_CARBON_PORT_PROP);
		if(this.carbon_port <=0  ) this.carbon_port=2003; 
		AppLog.getLogger().info("Graphite send port to:"+this.carbon_port);

		this.reconnect_timeout=appProps.getIntProperty(PropKey.GRAPHITE_RECONNECT_TIMEOUT_PROP);
		if(this.reconnect_timeout <=0  ) this.reconnect_timeout=60;
		AppLog.getLogger().info("Graphite reconect timeout set to:"+this.reconnect_timeout);

		this.send_buffer_size=appProps.getIntProperty(PropKey.GRAPHITE_SEND_BUFFER_SIZE_PROP);
		if(this.send_buffer_size <=0  ) this.send_buffer_size=1048576;
		AppLog.getLogger().info("Graphite send buffer size set to :"+this.send_buffer_size);

		this.map_server_stats=appProps.getBoolProperty(PropKey.GRAPHITE_MAP_SERVER_STATS_PROP,true);
		AppLog.getLogger().info("Graphite map Server stats  set to:"+ new Boolean(this.map_server_stats).toString());

		this.metric_use_host=appProps.getBoolProperty(PropKey.GRAPHITE_METRIC_USE_HOST_PROP,true);
		AppLog.getLogger().info("Graphite use host  set to:"+ new Boolean(this.metric_use_host).toString());


		this.metric_host_prefix=appProps.getProperty(PropKey.GRAPHITE_METRIC_HOST_PREFIX_PROP);
		if(this.metric_host_prefix == null ) this.metric_host_prefix="pro.bbdd";
		 AppLog.getLogger().info("Graphite host prefix to:"+this.metric_host_prefix);

		this.metric_host_suffix=appProps.getProperty(PropKey.GRAPHITE_METRIC_HOST_SUFFIX_PROP);
		if(this.metric_host_suffix == null ) this.metric_host_suffix="wls";
		AppLog.getLogger().info("Graphite host suffix to:"+this.metric_host_suffix);

		this.metric_default_host=appProps.getProperty(PropKey.GRAPHITE_METRIC_DEFAULT_HOST_PROP);
		if(this.metric_default_host == null ) this.metric_default_host="default_host";
		AppLog.getLogger().info("Graphite metric default host:"+this.metric_default_host);

		this.useDomainName=true;
		this.metric_domain_name=appProps.getProperty(PropKey.GRAPHITE_METRIC_FORCE_DOMAIN_NAME_PROP);
		if(this.metric_domain_name != null ) {
			AppLog.getLogger().info("Graphite forced  domain name:"+this.metric_domain_name);
			this.useDomainName=false;
		}

		//initializing hashMap
		this.counterMap=new HashMap<String,Integer>();

	}

	/*
	public void setChannel(Channel channel) {
		this.channel=channel;
	}*/

	public void setDomainName(String dom_name){
		if(this.useDomainName) {
		if(this.metric_domain_name == null || this.metric_domain_name.length() > 0)
			this.metric_domain_name=dom_name;
	 }
	} 
	
	/**
	 * Start the continuously repeating sleep-gather-schedule background 
	 * daemon thread.
	 */
	public void startup() {

		try { 
			Timer timer = new HashedWheelTimer();
			AppLog.getLogger().info("Graphite sender Background starting up");
			AppLog.getLogger().debug("Created background Java daemon thread to drive data retrieval process");
			// begin channel

			final InetSocketAddress addressToConnectTo = new InetSocketAddress(carbon_host, carbon_port);


			bossPool = Executors.newCachedThreadPool();
			workerPool = Executors.newCachedThreadPool();
			channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);


			bootstrap = new ClientBootstrap(channelFactory);
			
			gpf=new GraphiteClientPipelineFactory(bootstrap,timer);
			//gpf.setSender(this);
			gpf.setReconnectTimeout(this.reconnect_timeout);

			bootstrap.setPipelineFactory(gpf);
			bootstrap.setOption("tcpNoDelay" , true);
			bootstrap.setOption("keepAlive", true);
			bootstrap.setOption("remoteAddress",addressToConnectTo);
			bootstrap.setOption("sendBufferSize",this.send_buffer_size);

			cf=bootstrap.connect();
			//channel = cf.awaitUninterruptibly().getChannel();

		} catch (Exception e) {
			AppLog.getLogger().critical("Statistics Retriever Background Service has been disabled. Reason: " + e.toString());
			//throw new RuntimeException(e);
		}
	}

	/**
	 * Send signal to the continuously repeating sleep-gather-schedule 
	 * background process should terminate as soon as possible.
	 */
	public void shutdown() {
		AppLog.getLogger().info("Statistics Retriever Background Service shutting down");
		try {
			is_shutting_down = true; 
			Channel channel=gpf.getCurrentPipeline().getChannel(); 
			//cf.getChannel().getCloseFuture().awaitUninterruptibly(); 
			//cf.getChannel().close().awaitUninterruptibly(); 
			cf.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			AppLog.getLogger().info("Close channel"); 
			channelFactory.releaseExternalResources();
			AppLog.getLogger().info("Release factory resources"); 
			bootstrap.releaseExternalResources();
			AppLog.getLogger().info("Release client resources"); 
			
		} catch (Exception e) {
	        	AppLog.getLogger().error("error on channel retrieval: " + e.toString(), e);
			//throw new RuntimeException(e);

		}

	}

	public void counterInc(String serverName)
	{
		Integer val=counterMap.get(serverName);
		if(val !=null ) {
			
			counterMap.put(serverName,new Integer(val.intValue()+1));
		}
	}

	public void counterAdd(String serverName,int plus)
	{
		Integer val=counterMap.get(serverName);
		if(val !=null ) {
			counterMap.put(serverName,new Integer(val.intValue()+plus));
		}
	}


	public void resetCounter(String serverName)
	{
		counterMap.put(serverName,new Integer(0));
	}

	private String getMetricPathBase(String hostName,String serverName,String resourceType,String resourceName)
	{
		// Graphite Format
		// <item_1>.<item_2>.<item_3>.<item_4>
		// Each item can not contain "."
		// This is the item 
		// HOST BASE APROACH
		// ------------------
		//<HOST_PREFIX>.<HOST>.<HOST_SUFFIX>.<DOMAIN_NAME>.<SERVER>.<RESOURCE_TYPE>.<RESOURCE_NAME>.<METRIC_NAME>
		//
		// DOMAIN BASE APROACH
		//-------------------
		//<DOMAIN_NAME>.<SERVER>.<RESOURCE_TYPE>.<RESOURCE_NAME>.<METRIC_NAME>

		String final_host_name;
		String metric_path_base;

		if(metric_use_host) {
			//tree ordered in a host base aproach 

			if(hostName != null && (hostName.length()>0)) 	final_host_name=hostName;
			else 						final_host_name=metric_default_host;	
	
		
			 metric_path_base=metric_host_prefix+"."+
					final_host_name+"."+
					metric_host_suffix+"."+
					metric_domain_name+"."+
					serverName+"."+
					resourceType.replace('.','_');
		} else {
			// tree ordered only in a domain aproach
			 metric_path_base=metric_domain_name+"."+
					serverName+"."+
					resourceType.replace('.','_');

		}

		if(resourceName.length()>0) {	
				//on "core" resource Type no resource Name are set
				metric_path_base+="."+resourceName.replace('.','_');
		}
		return metric_path_base;

	}

	public void sendDHData(String resourceType,String resourceName,String metricName,String data)
	{
	try{ 
		Channel channel=gpf.getCurrentPipeline().getChannel();
		if(!channel.isConnected()) {
		//if(!channel.isWritable()) {
			 AppLog.getLogger().error("channel is disconnected ...send data skipped");
			return;
		}
		int counter=counterMap.get(resourceName).intValue()+2;

		String metric_path_base=getMetricPathBase("","dh_stats",resourceType,resourceName);	
		//String metric_path_1=metric_path_base+"."+metricName.replace('.','_');
		//Metric Name is set by us if needed we can place "." to organize tree
		String metric_path_1=metric_path_base+"."+metricName;
		String metric_path_2=metric_path_base+".number_metrics";
		long timestamp=System.currentTimeMillis()/1000;
		channel.write(metric_path_1+" "+data+" "+timestamp+"\n");
		channel.write(metric_path_2+" "+counter+" "+timestamp+"\n");
		
	} catch (Exception e) {
	        AppLog.getLogger().error("error on channel retrieval: " + e.toString(),e);
	}
	
	}


	public void sendData(Date dateTime, String serverName, String resourceType, String resourceName, String headerLine, String contentLine,String hostName)
	{

	try{ 
		Channel channel=gpf.getCurrentPipeline().getChannel();
		if(!channel.isWritable()) {
			 AppLog.getLogger().error("I can not write to this channel...");
			return;
		}


		String metric_path_base=getMetricPathBase(hostName,serverName,resourceType,resourceName);	
		String metric_path;
		String value;
		long timestamp;
		DateFormat formatter=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date;

		if (!contentLine.equals("")) 
		{
		List<String> metricItems = Arrays.asList(headerLine.split(","));
		List<String> contentItems = Arrays.asList(contentLine.split(","));
		try {
   			date = formatter.parse(contentItems.get(0));
			timestamp=date.getTime()/1000;
			int size=metricItems.size();
			//Core Resoutce Type Status
			if(resourceType.equals("core")) { 
				if(this.map_server_stats) {
					//Status set as first Metric
					int status=serverStatusMap.get(contentItems.get(1)).intValue();
					metric_path=metric_path_base+"."+metricItems.get(1); //State
					channel.write(metric_path+" "+Integer.toString(status)+" "+timestamp+"\n");
					counterInc(serverName);
				} 
				for(int i=2;i< size; i++) {
                			//Metric Name is set by us if needed we can place "." to organize graphite tree, so we prefer not to replace dots.
					//metric_path=metric_path_base+"."+metricItems.get(i).replace('.','_');
					metric_path=metric_path_base+"."+metricItems.get(i);
					value=contentItems.get(i);
					channel.write(metric_path+" "+value+" "+timestamp+"\n");
				}
				counterAdd(serverName,size-2);
				
			}  else {
			// other Resource Type
				for(int i=1;i< size; i++) {
					//Metric Name is set by us if needed we can place "." to organize graphite tree, so we prefer not to replace dots.
					//metric_path=metric_path_base+"."+metricItems.get(i).replace('.','_');
					metric_path=metric_path_base+"."+metricItems.get(i);
					value=contentItems.get(i);
					channel.write(metric_path+" "+value+" "+timestamp+"\n");
				}
				counterAdd(serverName,size-1); // not counting DateTime column
			}
	    
		} catch (Exception e) {
                        AppLog.getLogger().error("error on parse date: " + e.toString(),e);
		}
		}
	} catch (Exception e) {
	        AppLog.getLogger().error("error on channel retrieval: " + e.toString(),e);
	}
	
	}
	
	public static boolean isShuttingDown ()
	{
		return is_shutting_down; 
	}
	

}
