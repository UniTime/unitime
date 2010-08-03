/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.remote.SolverRegisterService;
import org.unitime.timetable.util.Constants;
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

		Debug.info("******* UniTime " + (Constants.VERSION + "." + Constants.BLD_NUMBER).replace("@build.number@", "?") +
				" build on " + Constants.REL_DATE.replace("@build.date@", "?") + " is starting up *******");

		super.init();
        
		try {
			
			Debug.info(" - Initializing Logging ... ");
            Debug.init(ApplicationProperties.getProperties());
            
			Debug.info(" - Initializing Hibernate ... ");							
			_RootDAO.initialize();
			
			Debug.info(" - Initializing Solver Register ... ");							
			SolverRegisterService.startService();
			SolverRegisterService.addShutdownHook();
			
			if (RoomAvailability.getInstance()!=null) {
			    Debug.info(" - Initializing Room Availability Service ... ");
			    RoomAvailability.getInstance().startService();
			}
			
			Debug.info("******* UniTime " + (Constants.VERSION + "." + Constants.BLD_NUMBER).replace("@build.number@", "?") +
					" build on " + Constants.REL_DATE.replace("@build.date@", "?") + " initialized successfully *******");

		} catch (Exception e) {
			Debug.error("UniTime Initialization Failed : " + e.getMessage(), e);
			sInitializationException = e;
		}
	}

	/**
	* Terminates the application
	*/
	public void destroy() {
		try {
		
			Debug.info("******* UniTime " + (Constants.VERSION + "." + Constants.BLD_NUMBER).replace("@build.number@", "?") +
					" build on " + Constants.REL_DATE.replace("@build.date@", "?") + " is going down *******");
		
			super.destroy();
		
			Debug.info(" - Stopping Solver Register ... ");							
			SolverRegisterService.stopService();
			try {
				SolverRegisterService.removeShutdownHook();
			} catch (IllegalStateException e) {}
			
			SolverInfo.stopInfoCacheCleanup();
		
			ApplicationProperties.stopListener();
			
	         if (RoomAvailability.getInstance()!=null) {
	             Debug.info(" - Stopping Room Availability Service ... ");
	             RoomAvailability.getInstance().stopService();
	         }
	         
	         QueueProcessor.stopProcessor();
			
				Debug.info("******* UniTime " + (Constants.VERSION + "." + Constants.BLD_NUMBER).replace("@build.number@", "?") +
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
		return "UniTime " + (Constants.VERSION + "." + Constants.BLD_NUMBER).replace("@build.number@", "?") + " Initialization Servlet";
	}
	
	public static Exception getInitializationException() { return sInitializationException; }
}
