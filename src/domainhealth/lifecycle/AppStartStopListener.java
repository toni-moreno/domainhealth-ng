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
package domainhealth.lifecycle;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.AppProperties.PropKey;
import domainhealth.backend.retriever.RetrieverBackgroundService;
import domainhealth.backend.sender.GraphiteBackgroundSender;
import domainhealth.core.statistics.MonitorProperties;

/**
 * Application start/deploy and stop/undeploy event listener to initialise and
 * destroy the resources required by DomainHealth. Not using a class which 
 * implements 'ServletContextListener' because this class's methods need to be 
 * run as a privileged user, using a 'runas' entry in web.xml, which is only 
 * possible for servlets.  
 */
public class AppStartStopListener extends GenericServlet {
	/**
	 * NOT IMPLEMENTED. Always throws a Servlet Exception because this method should never be invoked
	 * 
	 * @param request The HTTP Servlet request
	 * @param response The HTTP Servlet response
	 * @throws ServletException Indicates a problem processing the HTTP servlet request
	 * @throws IOException Indicates a problem processing the HTTP servlet request
	 *
	 * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		throw new ServletException("NOT IMPLEMENTED");		
	}

	/**
	 * Initialises the start/stop listener, which starts the Statistics
	 * Retriever background daemon process which will run repeatedly.
	 * 
	 * @throws ServletException Indicates a problem initialising the servlet
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {		

		//Redirect logs to where configured

		AppProperties appProps = new AppProperties(getServletContext());
		String log_filename	=appProps.getProperty(PropKey.OUTPUT_LOG_PATH_PROP);
		String log_level	=appProps.getProperty(PropKey.OUTPUT_LOG_LEVEL_PROP);
		if((log_filename == null) || (log_filename.length() == 0 )) log_filename="./domainhealt.log";
		if((log_level == null) || ( log_level.length() == 0 )) log_level="INFO";

		AppLog.getLogger().setConfig(log_filename,log_level);


		AppLog.getLogger().notice("Starting DomainHealth application");

		String outputPath 	=appProps.getProperty(PropKey.STATS_OUTPUT_PATH_PROP);		
               	String metricDeepSet	=appProps.getProperty(PropKey.METRIC_DEEP_SET_PROP);
                AppLog.getLogger().notice("initialized Statistic DEEP TO : " + metricDeepSet.toString());

		//Setting Metric Set

                MonitorProperties.setMetricDeep(metricDeepSet);

		
		if (outputPath == null) {
			throw new ServletException("Neither a JVM start-up '-D parameter nor a web.xml context-param has been defined for parameter '" + PropKey.STATS_OUTPUT_PATH_PROP + "' to specify the root path of the CSV output path");
		}
	
		String backend_output=appProps.getProperty(PropKey.BACKEND_OUTPUT_PROP);
		AppLog.getLogger().notice("Seting DomainHealth output to : " + backend_output);

		if( ! (backend_output.equals("graphite") | backend_output.equals("csvfile") | backend_output.equals("graphite")) ) backend_output="both";
		use_graphite=(backend_output.equals("graphite") | backend_output.equals("both"));

		retrieverBackgroundService = new RetrieverBackgroundService(appProps);


		if(use_graphite) {
			AppLog.getLogger().notice("Initializing Graphite Output...");
			//initialicing background workers to send data
			graphiteBackgroundSender = new GraphiteBackgroundSender(appProps);
			graphiteBackgroundSender.setDomainName(retrieverBackgroundService.getWLSDomainName());
			graphiteBackgroundSender.startup();
			//setting needed data to the RetrieverBackgoundService
			retrieverBackgroundService.setSender(graphiteBackgroundSender);
		}
		//begin thread
		retrieverBackgroundService.startup();		


		
	}

	/**
	 * Destroys the start/stop listener, which signals to the Retriever 
	 * background daemon process to stop.
	 *
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
		AppLog.getLogger().notice("Stopping DomainHealth application");
		retrieverBackgroundService.shutdown();
		if(use_graphite)  graphiteBackgroundSender.shutdown();
	}
	
	// Members
	private RetrieverBackgroundService retrieverBackgroundService = null;
	private GraphiteBackgroundSender graphiteBackgroundSender=null;
	private boolean use_graphite;
	
	// Constants
	private static final long serialVersionUID = 1L;	
}
