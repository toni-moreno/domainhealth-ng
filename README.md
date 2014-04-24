DomainHeatlh-NG
==============

DomainHealth-NG is a fork from the DomainHealth Tool done by  Paul Done that you can find at 

http://sourceforge.net/projects/domainhealth

A tool designed to provide administrarors with a quick and easy way to monitor a set WebLogic servers effectively. Entineered to have a minimal performance Impact on the managed servers in the domain. DomainHealth supports all WebLogic version from 9.0 to 12.1.x.


The main goal of this fork is to add graphite as backend to store metric data and analize them.

This project will focus efforts on collecting and selecting  metrics from any weblogic server better than store or render.

Collected Metrics
-----------------

From each managed server DomainHealth try to garther following metrics.

* __jvm__:
   * Loaded/Unloaded Classes.
   * Compilation time.
   * Heap/NonHeap memory data.
   * Memory Pools informaction.
   * Garbage Collection metrics.
* __Core__: 
   * Server State and Open Sockets
   * JVM HEAP size,free,%used.
   * ThreadPool stats.
   * JTA  Transaction stats.
* __Datasource__:
   * For each Datasource you will get active conections, available, delay time, threads waiting for conection, etc.
* __JMS Destinations__:
   * For each JMS destination queue gives you message and consumer countsers.  
* __WebApps__:
   * For each webapp deployed will get current sessions.
* __EJB__:
   * For each EJB you will get  ejb pool stats  and transacction stats 
* __HostMachineStats__:
   * For getting host OS stats you need WLHostMachineStats, WLHostMachineStats is a small agent (JMX MBean) that runs in every WebLogic Server to collect O.S. statistics (ie. CPU/Memory/Network usage).
   http://sourceforge.net/projects/wlhostmchnstats/
   This is a good choice if working with a standalone DomainHealth instalation. If working on a multidomain environtment with graphite you will gather better and faster OS stats with other graphite colecting tools like collectd/hekad/etc.
* __ExtendedStats__: 
   * ( Only when gather stats with WLDF ) you will get workmanager and server channels stats



Building From Source
--------------------

This project includes an Ant buildfile in the root directory to enable the project to be completely re-built from source and modified and enhanced where necessary. The project also includes an Eclipse '.project' Project file, enabling developers to optionally use Eclipse to modify the source (just import DomainHealth as an existing project into Eclipse).

To re-build the project, first ensure the Java 1.5.x SDK and Ant 1.6+ is installed and their 'bin' directories are present in PATH environment variable, then check the values in the local.properties file in the project's
root directory to ensure this reflects your local WebLogic environment settings. 

Run the following commands to clean the project, compile the source code and build the WAR web-application:

```
 > ant clean
 > ant
```

OPTIONAL: To run the unit tests for the project, copy the JUnit archive ('junit.jar') from this project's 'lib' directory into 'ANT_HOME/lib'm and then run:

```
 > ant test
```

OPTIONAL: To automatically deploy the generated WAR web-application to a running WebLogic Server, first modify the 'local.properties' file in the root of the project, to reflect the required WebLogic settings and then run:

```
 > ant deploy
```

To undeploy the application, run:

```
 > ant undeploy
```

Install
-------

Once  you have the domainhealth-XXX.war package you can Install in two ways

1. with ant deploy task as the previous section said.
2. with the console in the AdminServer.


Configuration
-------------

DomainHealth-NG have maintained the old configuration system from Original DomainHealth with -D<parameter_key>=<value> style config but also introduces a new dh_global.properties file to centralize all config parameters and simplify changes and maintenance.

In this way you should configure all settings  configuring only one parameter dh_config_file as the only JAVA_OPTIONS to add or leaving a config file named "dh_global.properties" in the Domain root path.

 An default dh_global.properties is located at the root of the sources directory

a)  You can config by editing setDomainEnv.sh and add at the end.

```
case ${SERVER_NAME} in
        AdminServer)
        echo "Enabling DomainHealth NG Config"
        export JAVA_OPTIONS="$JAVA_OPTIONS -Ddh_config_file=/absolute_path/dh_global.properties"
       ;; 
esac
```

b) You can also config by placing the dh_global.properties file in the domain root path.

```
$DOMAIN_ROOT/dh_global.properties
```


After you can edit dh_global.properties


Configuration Parameters
------------------------

***Base Configuration Parameters***

 
* __dh_always_use_jmxpoll__: Forces DH to always use JMX polling to collect metrics rather than allowing DH to decide for itself what to use (which in WLS 10.3+ would otherwise default to using WLDF Harvesting).
* __dh_query_interval_secs__: The gap in seconds between consecutive statistic collections (making this too small could impact server performance).
* __dh_backend_output__:  Select with backend to use graphite,csvfile,both
* __dh_output_log_path__: The file where to log all DomainHealth-NG output
* __dh_output_log_level__: Set the threshold level. All log events with lower level than the threshold level are ignored by the appender. Default: INFO

***Metric Configuration Parameters***

* __dh_metric_type_set__: type of metric to gather among (jvm,core,datasource,jmsdestination,webapp,ejb,hostmachine,extended) Default: All
* __dh_metric_deep_set__: set of metrics to gather among (basic/full). Default: full
* __dh_component_blacklist__: The list of deployed application names which should not have statistics collected or displayed - usually used to prevent WebLogic internal applications from appearing in results



***CSV Configuration Parameters***

* __dh_stats_output_path__:  Defines the absolute or relative (to server start-up dir) path of the root directory where DH should store captured CSV statistic files. Default ./logs/statistics
* __dh_csv_retain_num_days__:  The number of days to retain captured CSV data log files for (older ones are automatically removed by DomainHealth to help limit file-system capacity consumption).


***Graphite Configuration Parameters***


* __dh_graphite_carbon_host__: Graphite carbon server carbon (Graphite) Default: localhost
* __dh_graphite_carbon_port__: carbon port ( Graphite) Default: 2003
* __dh_graphite_reconnect_timeout__: Reconnection attempt time after connection lost in seconds. Default: 60 seconds
* __dh_graphite_send_buffer_size__:  Graphite output buffer size on heavily loaded systems better big buffers. Default: 1Mb
* __dh_graphite_metric_use_host__: Enable a metric tree based on host better than a domain tree based approach. Default: True
* __dh_graphite_metric_host_prefix__: hostname prefix. Default: pro.bbdd
* __dh_graphite_metric_host_suffix__: hostname suffix. Default: wls
* __dh_graphite_default_host__:  “Machine” Name it uses in host based approach ( if not properly configured with console ) and the hostname in the AdminServer data. Default: default_host 
* __dh_graphite_metric_force_domain_name__: fix bug on read weblogic domain name on server startup in 9.2. Default: my_domain:  
* __dh_graphite_map_server_stats__:  Map server stats to numbers so the graphite backend will be able to store and render after.
 - SHUTDOWN(0)
 - STARTING(1)
 - STANDBY(2)
 - ADMIN(3)
 - RESUMING(4)
 - RUNNING(5)
 - SHUTTING_DOWN(7)
 - SUSPENDING(6)
 - FORCE_SUSPENDING(8)
* __dh_graphite_report_dhstats__:  Send data about Domainhealth data retrieval (Only reported over graphite backend). Default : True




Graphite Tree Model
-------------------

1.- Host Based ( by default)

```
<HOST_PREFIX>.<HOST>.<HOST_SUFFIX>.<DOMAIN_NAME>.<SERVER_INSTANCE_NAME>.<RESOURCE_TYPE>.<RESOURCE_NAME>.<METRIC_NAME>
```


2.- Domain Based.

```
<DOMAIN_NAME>.<SERVER_INSTANCE_NAME>.<RESOURCE_TYPE>.<RESOURCE_NAME>.<METRIC_NAME>
```

On both models you can get information on how many and how lond data is gathered on each managed server over de domain tree.


```
XXXXX.<DOMAIN_NAME>.dh_stats.servers.<SERVER_INSTANCE_NAME>.retrieve_time
XXXXX.<DOMAIN_NAME>.dh_stats.servers.<SERVER_INSTANCE_NAME>.number_metrics
```


