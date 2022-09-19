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
package org.unitime.timetable.action;

import org.apache.struts2.convention.annotation.Action;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.util.AccessDeniedException;
import org.unitime.timetable.webutil.BackTracker;

/**
 * 
 * @author Tomas Muller
 *
 */
@Action(value = "back")
public class BackAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 6990093910325408161L;

	public String execute() throws Exception {
        if(!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();
        
        BackTracker.doBack(request, response);
        
        return null;
	}

}
