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
