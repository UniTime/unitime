/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.listeners;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;

import org.unitime.commons.Debug;


/**
 * @author Heston Fernandes
 */
public class SessionActivationListener 
	implements HttpSessionActivationListener, HttpSessionBindingListener {

    public void sessionWillPassivate(HttpSessionEvent evt) {
        Debug.info("TT Session will passivate ... " + evt.getSession().getId());
    }

    public void sessionDidActivate(HttpSessionEvent evt) {
        Debug.info("TT Session did activate ... " + evt.getSession().getId());
    }

    
    public void valueBound(HttpSessionBindingEvent evt) {
        Debug.info("TT Session value bound ... " + evt.getSession().getId());
    }
    
    public void valueUnbound(HttpSessionBindingEvent evt) {
        Debug.info("TT Session value unbound ... " + evt.getSession().getId());
    }

}
