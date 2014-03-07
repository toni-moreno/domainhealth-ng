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

//import weblogic.logging.NonCatalogLogger;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;

/**
 * Wrapper around a logging implementation to enable application debug, info
 * notice, warning error, and critical logging messages to be directed to the 
 * appropriate logging sub-system. This implementation currently just provides
 * a simple wrapper around the WebLogic NonCatalogLogger logging API.  
 */
public class AppLog {

	public void setConfig(String filename,String level) {
		FileAppender fa = new FileAppender();
		fa.setName(DH_APP_NAME);
		fa.setFile(filename);
  		fa.setLayout(new PatternLayout("%d [%-5p][%r] %t | %x [%c] %m%n"));
		fa.setThreshold(Level.toLevel(level));
  		fa.setAppend(true);
  		fa.activateOptions();
		//removing all previous loggers.
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(fa);
		log = Logger.getLogger(DH_APP_NAME);
	}

	/**
	 * Creates a  Log4j logger identified by the application 
	 * name (DomainHealth)
	 */
	protected AppLog() {
		//TODO: redirect by default to Standar Output.
	 	ConsoleAppender console = new ConsoleAppender(); //create appender
  	//configure the appender
		console.setName("default");
  		console.setLayout(new PatternLayout("%d [%p|%c|%C{1}] %m%n")); 
  		console.setThreshold(Level.INFO);
  		console.activateOptions();
  	//add appender to any Logger (here is root)
  		Logger.getRootLogger().addAppender(console);
		this.log = Logger.getLogger("default");
	}
	
	/**
	 * Implements singleton pattern returning the shared logger instance
	 * 
	 * @return The instance of the logger
	 */
	public static AppLog getLogger() {
		return instance;
	}

	/**
	 * Logs a critical message
	 * 
	 * @param msg The message to be logged
	 */
	public void critical(String msg) {
		if (log.isEnabledFor(Level.FATAL)) {
			log.fatal(msg);
		}
	}

	/**
	 * Logs an error message
	 * 
	 * @param msg The message to be logged
	 */
	public void error(String msg) {
		if (log.isEnabledFor(Level.ERROR)) {
			log.error(msg);
		}
	}

	/**
	 * Logs an error messageand cause exception
	 * 
	 * @param msg The message to be logged
	 * @param t The root cause throwable instance
	 */
	public void error(String msg, Throwable t) {
		if (log.isEnabledFor(Level.ERROR)) {
			log.error(msg, t);
		}
		//log.error(msg, t);
	}

	/**
	 * Logs a warning message
	 * 
	 * @param msg The message to be logged
	 */
	public void warning(String msg) {
		if (log.isEnabledFor(Level.WARN)) {
			log.warn(msg);
		}
	}

	/**
	 * Logs a notice message
	 * 
	 * @param msg The message to be logged
	 */	
	public void notice(String msg) {
		if (log.isInfoEnabled()) {
			log.info(msg);
		}
	}

	/**
	 * Logs an info message
	 * 
	 * @param msg The message to be logged
	 */
	public void info(String msg) {
		if (log.isInfoEnabled()) {
			log.info(msg);
		}
	}

	/**
	 * Logs a debug message
	 * 
	 * @param msg The message to be logged
	 */
	public void debug(String msg) {
		if (debugToStandardOut) {
			System.out.println(DH_APP_NAME + ": DEBUG - " + msg);
		} else {
			if (log.isDebugEnabled()) {
				log.debug(msg);
			}
		}
	}

	/**
	 * Logs a debug message and cause exception
	 * 
	 * @param msg The message to be logged
	 * @param t The root cause throwable instance
	 */
	public void debug(String msg, Throwable t) {
		if (debugToStandardOut) {
			System.out.println(DH_APP_NAME + ": DEBUG - " + msg);
			t.printStackTrace();
		} else {
			if (log.isDebugEnabled()) {
				log.debug(msg,t);
			}
		}		
	}

	// Members
	
	static Logger log ;
	private static final AppLog instance = new AppLog();

	// Constants
	private static final String DH_APP_NAME = "DomainHealth-NG";
	private static final boolean debugToStandardOut = false;
}
