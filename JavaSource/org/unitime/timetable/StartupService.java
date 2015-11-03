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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.connection.DisposableConnectionProvider;
import org.unitime.timetable.events.EventExpirationService;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.base._BaseRootDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LogCleaner;
import org.unitime.timetable.util.MessageLogAppender;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.queue.QueueProcessor;

/**
 * @author Tomas Muller
 */
@Service("startupService")
public class StartupService implements InitializingBean, DisposableBean {
	private Exception iInitializationException = null;
	private MessageLogAppender iMessageLogAppender = null;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Debug.info("******* UniTime " + Constants.getVersion() +
				" build on " + Constants.getReleaseDate() + " is starting up *******");

		try {
			
			Debug.info(" - Initializing Logging ... ");
            Debug.init(ApplicationProperties.getProperties());
            
			Debug.info(" - Initializing Hibernate ... ");							
			_RootDAO.initialize();
			
			// Update logging according to the changes recorded in the application config
			ApplicationConfig.configureLogging();
			
	         Debug.info(" - Creating Message Log Appender ... ");
	         iMessageLogAppender = new MessageLogAppender();
	         Logger.getRootLogger().addAppender(iMessageLogAppender);
			
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
			iInitializationException = e;
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}		
	}
	
	public Exception getInitializationException() {
		return iInitializationException;
	}

	@Override
	public void destroy() throws Exception {
		try {
			
			Debug.info("******* UniTime " + Constants.getVersion() +
					" build on " + Constants.getReleaseDate() + " is going down *******");
		
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
	         Logger.getRootLogger().removeAppender(iMessageLogAppender);
	         iMessageLogAppender.close();
	         
	         Debug.info(" - Closing Hibernate ... ");
	         (new _BaseRootDAO() {
		    		void closeHibernate() {
		    			SessionFactory sf = sSessionFactory;
		    			if (sf != null) {
		    				sSessionFactory = null;
		    				if (sf instanceof SessionFactoryImpl) {
		    					ConnectionProvider cp = ((SessionFactoryImpl)sf).getConnectionProvider();
		    					if (cp instanceof DisposableConnectionProvider) {
		    						try {
		    							((DisposableConnectionProvider)cp).destroy();
		    						} catch (Exception e) {}
		    					}
		    				}
		    				sf.close();
		    			}
		    		}
		    		protected Class getReferenceClass() { return null; }
		    	}).closeHibernate();
	         // CacheManager.getInstance().shutdown();
	         
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
}
