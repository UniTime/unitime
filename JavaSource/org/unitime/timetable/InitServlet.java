/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.sf.ehcache.CacheManager;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.events.EventExpirationService;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LogCleaner;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.queue.QueueProcessor;


/**
 * Application Initialization Servlet
 * @version 1.0
 * @author Heston Fernandes, Tomas Muller
 */

public class InitServlet extends HttpServlet implements Servlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3258415014804142137L;

	private static Exception sInitializationException = null;
	
	/**
	* Initializes the application
	*/
	public void init() throws ServletException {

		Debug.info("******* UniTime " + Constants.getVersion() +
				" build on " + Constants.getReleaseDate() + " is starting up *******");

		super.init();
        
		try {
			
			Debug.info(" - Initializing Logging ... ");
            Debug.init(ApplicationProperties.getProperties());
            
			Debug.info(" - Initializing Hibernate ... ");							
			_RootDAO.initialize();
			
			// Update logging according to the changes recorded in the application config
			ApplicationConfig.configureLogging();
			
			if (RoomAvailability.getInstance()!=null) {
			    Debug.info(" - Initializing Room Availability Service ... ");
			    RoomAvailability.getInstance().startService();
			}
			
			Debug.info(" - Cleaning Logs ...");
			LogCleaner.cleanupLogs();
			
			Debug.info(" - Starting Event Expiration Service ...");
			EventExpirationService.getInstance().start();
			
			Debug.info("******* UniTime " + Constants.getVersion() +
					" build on " + Constants.getReleaseDate() + " initialized successfully *******");

		} catch (Exception e) {
			Debug.error("UniTime Initialization Failed : " + e.getMessage(), e);
			sInitializationException = e;
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}

	/**
	* Terminates the application
	*/
	public void destroy() {
		try {
		
			Debug.info("******* UniTime " + Constants.getVersion() +
					" build on " + Constants.getReleaseDate() + " is going down *******");
		
			super.destroy();
			
			Debug.info(" - Stopping Event Expiration Service ...");
			EventExpirationService.getInstance().interrupt();
			
			SolverInfo.stopInfoCacheCleanup();
		
			ApplicationProperties.stopListener();
			
	         if (RoomAvailability.getInstance()!=null) {
	             Debug.info(" - Stopping Room Availability Service ... ");
	             RoomAvailability.getInstance().stopService();
	         }
	         
	         QueueProcessor.stopProcessor();
	         
	         Debug.info(" - Removing Message Log Appender ... ");
	         Appender mlog = Logger.getRootLogger().getAppender("mlog");
	         if (mlog != null) {
	        	 Logger.getRootLogger().removeAppender("mlog");
	        	 mlog.close();
	         }
	         
	         Debug.info(" - Closing Hibernate ... ");
	         HibernateUtil.closeHibernate();
	         CacheManager.getInstance().shutdown();
	         
	         Debug.info("******* UniTime " + Constants.getVersion() +
						" shut down successfully *******");
		} catch (Exception e) {
			Debug.error("UniTime Shutdown Failed : " + e.getMessage(), e);
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException("UniTime Shutdown Failed : " + e.getMessage(), e);
		}
	}

	/**
	 * Gets servlet information
	 * @return String containing servlet info 
	 */
	public String getServletInfo() {
		return "UniTime " + Constants.getVersion() + " Initialization Servlet";
	}
	
	public static Exception getInitializationException() { return sInitializationException; }
}
