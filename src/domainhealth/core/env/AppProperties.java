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
package domainhealth.core.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

/**
 * Provides access to application level properties which may be populated from 
 * a variety of places, including -D JVM parameters, web.xml context-params
 * and props files bundled as resources in the WAR archive. Additionally, the 
 * same property key-value pairs are stored as servlet context 
 * (application-scope) attributes for easy use from Tab Libraries in view 
 * jsps, if/where required.
 * 
 * Note: Property key syntax uses '_' (underscore) rather than '-' (slash)
 * because some of the keys are used in JSP Expression Language (EL) clauses
 * where the '-' symbol is interpreted as 'subtract'. 
 */
public class AppProperties extends Properties {
	/**
	 * The keys for each property (calling toString() for these will give you the property name)
	 */
	public enum PropKey {
		/**
		 * The always use jmxpoll" property name ("dh_always_use_jmxpoll")
		 */
		ALWAYS_USE_JMXPOLL_PROP { public String toString() { return "dh_always_use_jmxpoll"; } },
		
		/**
		 * The root statistics directory path property name ("dh_stats_output_path")
		 */
		STATS_OUTPUT_PATH_PROP { public String toString() { return "dh_stats_output_path"; } },

		/**
		 * The OUTPUT_LOG_PATH property name ("dh_always_use_jmxpoll")
		 */
		OUTPUT_LOG_PATH_PROP { public String toString() { return "dh_output_log_path"; } },
		
		/**
		 * The root statistics directory path property name ("dh_stats_output_path")
		 */
		OUTPUT_LOG_LEVEL_PROP { public String toString() { return "dh_output_log_level"; } },

		/**
		 * The the number of seconds between statistics collection queries property name ("dh_query_interval_secs")
		 */
		QUERY_INTERVAL_SECS_PROP { public String toString() { return "dh_query_interval_secs"; } },

		/**
		 * The the number of seconds between statistics collection queries property name ("dh_component_blacklist")
		 */
		COMPONENT_BLACKLIST_PROP { public String toString() { return "dh_component_blacklist"; } },

		/**
		 * The metric type to get values among (core,datasource,jmsdestination,webapp,ejb,hostmachine,extended) 
		 */
		METRIC_TYPE_SET_PROP { public String toString() { return "dh_metric_type_set"; } },

		/**
		 * The metric deep set to get basic,extended,full 
		 */
		METRIC_DEEP_SET_PROP { public String toString() { return "dh_metric_deep_set"; } },

		/**
		 * The number of days of CSV files to retain. Use 0 or -1 to disable CSV cleanup ("dh_component_blacklist")
		 */
		CSV_RETAIN_NUM_DAYS { public String toString() { return "dh_csv_retain_num_days"; } },
		/**
		 * The backend to use to send data graphite/both/csvfile property name ("dh_backend_output")
		 */
		BACKEND_OUTPUT_PROP { public String toString() { return "dh_backend_output"; } },
	
		/**
		 * The domain health version number property name ("dh_version_number")
		 */
		VERSION_NUMBER_PROP { public String toString() { return "dh_version_number"; } },

		/**
		 * The domain health version date property name ("dh_version_date")
		 */
		VERSION_DATE_PROP { public String toString() { return "dh_version_date"; } },
		
		/**
		 * The domain health compilation time property name ("dh_compilation_time")
		 */
		COMPILATION_TIME_PROP { public String toString() { return "dh_compilation_time"; } },
		
		/// Graphite properties
		/**
		 * The Graphite  property name ("dh_graphite_carbon_host")
		 */
		GRAPHITE_CARBON_HOST_PROP { public String toString() { return "dh_graphite_carbon_host"; } },
		/**
		 * The Graphite  property name ("dh_graphite_carbon_port")
		 */
		GRAPHITE_CARBON_PORT_PROP { public String toString() { return "dh_graphite_carbon_port"; } }, 
		/**
		 * The Graphite  property name ("dh_graphite_reconect_timeout")
		 */
		GRAPHITE_RECONNECT_TIMEOUT_PROP { public String toString() { return "dh_graphite_reconnect_timeout"; } }, 
		/**
		 * The Graphite  property name ("dh_graphite_reconect_timeout")
		 */
		GRAPHITE_FORCE_RECONNECT_TIMEOUT_PROP { public String toString() { return "dh_graphite_force_reconnect_timeout"; } }, 
		/**
		 * The Graphite  property name ("dh_graphite_send_buffer_size")
		 */
		GRAPHITE_SEND_BUFFER_SIZE_PROP { public String toString() { return "dh_graphite_send_buffer_size"; } }, 
		/**
		 * The Graphite  property name ("dh_graphite_report_dhstats")
		 */
		GRAPHITE_REPORT_DHSTATS_PROP { public String toString() { return "dh_graphite_report_dhstats"; } }, 
		/**
		 * The Graphite  property name ("dh_graphite_map_server_stats")
		 */
		//GRAPHITE_MAP_SERVER_STATS_PROP { public String toString() { return "dh_graphite_map_server_stats"; } }, 

		/**
		 * The Graphite  property name ("dh_graphite_metric_use_host")
		 */
		GRAPHITE_METRIC_USE_HOST_PROP { public String toString() { return "dh_graphite_metric_use_host"; } },

		/**
		 * The Graphite  property name ("dh_graphite_metric_prefix")
		 */
		GRAPHITE_METRIC_HOST_PREFIX_PROP { public String toString() { return "dh_graphite_metric_host_prefix"; } },
		/**
		 * The Graphite  property name ("dh_graphite_metric_suffix")
		 */
		GRAPHITE_METRIC_HOST_SUFFIX_PROP { public String toString() { return "dh_graphite_metric_host_suffix"; } },
		/**
		 * The Graphite  property name ("dh_graphite_default_host")
		 */
		GRAPHITE_METRIC_DEFAULT_HOST_PROP { public String toString() { return "dh_graphite_default_host"; } },
		/**
		 * The Graphite  property name ("dh_graphite_metric_force_domain_name")
		 */
		GRAPHITE_METRIC_FORCE_DOMAIN_NAME_PROP { public String toString() { return "dh_graphite_metric_force_domain_name"; } } 




	};

	/**
	 * Populate the set of named application properties (and servlet context 
	 * attributes) from the following sources in order of decreasing 
	 * precedence:
	 * 
	 *   1. -D JVM parameters
	 *   2. web.xml context-params
	 *   3. Base properties provided ina props file in the WAR archive
	 *   
	 * @param sc The servlet context
	 */
	public AppProperties(ServletContext sc) {
		InputStream in = null;
		Properties baseProps = new Properties();
		Properties compProps = new Properties(); 
		Properties configProps = new Properties();

		// Loading base properties
		
		try {
			in = sc.getResourceAsStream(VERSION_PROPS_FILEPATH);
			baseProps.load(in);			
		} catch (IOException e) {
			AppLog.getLogger().warning("Unable to load base properties from WAR internal path: " + VERSION_PROPS_FILEPATH);
		} finally {
			try { in.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		
		// Loading compilation properties
	    try {
			in = sc.getResourceAsStream(COMPILATION_PROPS_FILEPATH);
			compProps.load(in);			
		} catch (IOException e) {
			AppLog.getLogger().warning("Unable to load compilation properties from WAR internal path: " + COMPILATION_PROPS_FILEPATH);
		} finally {
			try { in.close(); } catch (Exception e) { e.printStackTrace(); }
		}

		// Loading config.properies from -Ddh_config_file=<file>
		String filename=System.getProperty("dh_config_file");

                if ((filename != null) && (filename.length() > 0)) {
			try {
				//final Path path = Paths.get(filename);
				if( new File(filename).exists()) {
				//if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
					configProps.load(new FileInputStream(filename));
				} else { 
					configProps.load(getClass().getClassLoader().getResourceAsStream(filename));
				}
			} catch (IOException e) {
                        	AppLog.getLogger().warning("Unable to load global config  properties : " + filename);
	                }

			
                } else {
				
					try
					{
						InitialContext ctx = new InitialContext();
						MBeanServer server = (MBeanServer)ctx.lookup("java:comp/env/jmx/runtime");
						ObjectName service = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
						ObjectName domain = (ObjectName)server.getAttribute(service, "DomainConfiguration");
						String base_domain = server.getAttribute(domain, "RootDirectory").toString();
						configProps.load(new FileInputStream(base_domain + "/dh_global.properties"));
						//AppLog.getLogger().warning("Not Global config_file requested using system -D, and web.xml properties instead: ");
						AppLog.getLogger().warning("[DomainHealth-NG]: System -D config file param not found, using config file in base domain path instead: ");
					}
					catch (NamingException e)
					{
						AppLog.getLogger().warning("Unable to load context to find base domain path: ");
					}	
					catch (MalformedObjectNameException e)
					{
						AppLog.getLogger().warning("Unable to load domain configuration object name: ");
					}
					catch (MBeanException e)
					{
						AppLog.getLogger().warning("Unable to read RuntimeService MBean: ");
					}
					catch (AttributeNotFoundException e)
					{
						AppLog.getLogger().warning("Unable to read RootDirectory attribute in RuntimeService MBean: ");
					}
					catch (InstanceNotFoundException e)
					{
						AppLog.getLogger().warning("Unable to read RootDirectory attribute in RuntimeService MBean: ");
					}
					catch (ReflectionException e)
					{
						AppLog.getLogger().warning("Unable to read RootDirectory attribute in RuntimeService MBean: ");
					}
					catch (IOException e) 
					{
						AppLog.getLogger().warning("Unable to load global config  properties in base domain: ");
					}
		}

		loadProps(sc, baseProps,configProps, compProps);
	}

	/**
	 * Get property value
	 * 
	 * @param key The property key
	 * @return The property text value
	 */
    public String getProperty(PropKey key) {
    	return getProperty(key.toString());
    }
   
 	/**
	 * Get boolean version of property value, returning false if value can't 
	 * be coerced into a boolean.
	 * 
	 * @param key The property key to look up
	 * @return The boolean value of property
	 */
	public boolean getBoolProperty(PropKey key,boolean default_is_not_set) {
		String inkey=getProperty(key.toString());
		if(inkey == null ) return default_is_not_set;
		else {
			return Boolean.parseBoolean(inkey);		
		}
	}

	/**
	 * Get boolean version of property value, returning false if value can't 
	 * be coerced into a boolean.
	 * 
	 * @param key The property key to look up
	 * @return The boolean value of property
	 */
	public boolean getBoolProperty(PropKey key) {
		return Boolean.parseBoolean(getProperty(key.toString()));		
	}

	/**
	 * Get integer version of property value, returning -1 if value can't be
	 * coerced into an integer.
	 * 
	 * @param key The property key to look up
	 * @return The integer value of property
	 */
	public int getIntProperty(PropKey key) {
		int result = -1;
		String textVal = getProperty(key.toString());
		
		if ((textVal != null) && (textVal.length() > 0)) {
			try {
				result = Integer.parseInt(textVal);
			} catch (NumberFormatException e) {
				result = -1;
			}
		}
		
		return result;
	}

	/**
	 * Load the properties trying each of the 3 sources in turn for a matching
	 * property.
	 * 
	 * @param sc The servlet context
	 * @param baseProps The base properties to use if not overriden from another source
	 */
	private void loadProps(ServletContext sc, Properties baseProps,Properties globalProps, Properties compProps) {
		for (PropKey key : PropKey.values()) {
			// Global config properties always first 
			String value =  globalProps.getProperty(key.toString());
			
			// -D system property
			 if ((value == null) || (value.length() <= 0)) {
				value = System.getProperty(key.toString());
			}
			// web.xml context-param property
			if ((value == null) || (value.length() <= 0)) {
				value = sc.getInitParameter(key.toString());
			}
			
			// base properties file property
			if ((value == null) || (value.length() <= 0)) {
				value = baseProps.getProperty(key.toString());
			}
			
			// compilation properties
			if ((value == null) || (value.length() <= 0)) {
				value = compProps.getProperty(key.toString()); 
			}
			
			// finally save this property 
			if ((value != null) && (value.length() > 0)) {
				setProperty(key.toString(), value);
				sc.setAttribute(key.toString(), value);
			}
		}
	}
	
	// Constants
	private static final long serialVersionUID = 1L;
	private static final String VERSION_PROPS_FILEPATH = "/WEB-INF/version.props";
	private static final String COMPILATION_PROPS_FILEPATH = "/WEB-INF/compilation.props"; 
}
