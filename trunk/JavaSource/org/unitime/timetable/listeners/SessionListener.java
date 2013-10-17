/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.listeners;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.struts.Globals;
import org.unitime.commons.Debug;
import org.unitime.timetable.util.MessageResources;
import org.unitime.timetable.util.MessageResourcesFactory;


/**
 * Utility class to determine start and end of http sessions
 * @author Heston Fernandes
 */

public class SessionListener implements HttpSessionListener {

    private static int activeSessions = 0;
    private static HashMap sessions = new HashMap();
    
    /**
     * Listener Event when session is created
     */
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        sessions.put(session.getId(), session);
        activeSessions++;        

        Debug.info("TT Session started ... " + session.getId() + " " +  new Date());
    }

    /**
     * Listener Event when session is destroyed
     */
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
		sessions.remove(session.getId());
        if(activeSessions > 0) {
			activeSessions--;
		}

        Debug.info("TT Session ended ... " + session.getId() + " " +  new Date());
        Debug.info("    - TT Session time ... " +  
                ( (new Date().getTime() - session.getCreationTime())/(1000*60) ) + " minutes" );
        
        session.invalidate();
    }
    
    /**
     * Count of Active Sessions
     * @return Count of Active Sessions
     */
	public static int getActiveSessions() {
		return activeSessions;
	}

	/**
	 * Map of active sessions
	 * @return Map of active sessions
	 */
	public static HashMap getSessions() {
	    return sessions;
	}
	
	public static void reloadMessageResources(String resourceFile) {
		if (sessions!=null && sessions.size()>0) {
			for(Iterator i= sessions.keySet().iterator(); i.hasNext(); ) {
				HttpSession session = (HttpSession) sessions.get(i.next());
				if (session!=null) {
					org.apache.struts.util.MessageResourcesFactory mrf = MessageResourcesFactory.createFactory();
					MessageResources mr = new MessageResources((MessageResourcesFactory) mrf, resourceFile);
					session.getServletContext().setAttribute(Globals.MESSAGES_KEY, mr);
					break;
				}
			}
		}
	}
}
