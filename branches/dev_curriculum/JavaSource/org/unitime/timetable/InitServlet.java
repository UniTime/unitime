/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.queue.QueueProcessor;


/**
 * Application Initialization Servlet
 * @version 1.0
 * @author Heston Fernandes
 */

public class InitServlet extends HttpServlet implements Servlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3258415014804142137L;
	 
	/**
	* Initializes the application
	*/
	public void init() throws ServletException {

		logMessage("******* Initializing Timetabling Application : START *******");

		super.init();
        
		try {
            
            logMessage(" - Initializing Debugger ... ");
            Debug.init(ApplicationProperties.getProperties());
            
			logMessage(" - Initializing Hibernate ... ");							
			_RootDAO.initialize();
			
			logMessage(" - Initializing Solver Register ... ");							
			SolverRegisterService.startService();
			SolverRegisterService.addShutdownHook();
			
			if (RoomAvailability.getInstance()!=null) {
			    logMessage(" - Initializing Room Availability Service ... ");
			    RoomAvailability.getInstance().startService();
			}
			
			logMessage("******* Timetabling Application : Initializing DONE *******");
		} 
		catch (Exception e) {
			logError("Servlet Initialization Failed : " + e.getMessage());
            e.printStackTrace();
		}
	}

	/**
	* Terminates the application
	*/
	public void destroy() {
		try {
		
			logMessage("******* Shutting down Timetabling Application *******");
		
			super.destroy();
		
			logMessage(" - Stopping Solver Register ... ");							
			SolverRegisterService.stopService();
			try {
				SolverRegisterService.removeShutdownHook();
			} catch (IllegalStateException e) {}
			
			SolverInfo.stopInfoCacheCleanup();
		
			ApplicationProperties.stopListener();
			
	         if (RoomAvailability.getInstance()!=null) {
	             logMessage(" - Stopping Room Availability Service ... ");
	             RoomAvailability.getInstance().stopService();
	         }
	         
	         QueueProcessor.stopProcessor();
			
			logMessage("******* Timetabling Application : Shut down DONE *******");
		} catch (Exception e) {
			Debug.error(e);
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException("Shut down failed", e);
		}
	}

	/**
	 * Gets servlet information
	 * @return String containing servlet info 
	 */
	public String getServletInfo() {

		return "Timetabling Initialization Servlet";

	}

	/*
	 * Writes message to log
	 */
	private static void logMessage(String message) {
		Debug.info(message);
	}

	/*
	 * Write error to log
	 */
	private static void logError(String message) {
		Debug.error(message);
		System.err.println(message);
	}
}
