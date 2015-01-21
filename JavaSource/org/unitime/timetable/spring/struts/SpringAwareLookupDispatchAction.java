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
package org.unitime.timetable.spring.struts;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.ModuleUtils;

/**
 * @author Tomas Muller
 */
public abstract class SpringAwareLookupDispatchAction extends LookupDispatchAction {
	
	@Override
	protected MessageResources getResources(HttpServletRequest request, String key) {
		// Identify the current module, the right way
		ServletContext context = request.getSession().getServletContext();
		ModuleConfig moduleConfig = ModuleUtils.getInstance().getModuleConfig(request, context);
		
		// Return the requested message resources instance
		return (MessageResources) context.getAttribute(key + moduleConfig.getPrefix());
	}

}
