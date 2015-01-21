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
