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
package org.unitime.localization.impl;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;

/**
 * @author Tomas Muller
 */
public abstract class LocalizedLookupDispatchAction extends SpringAwareLookupDispatchAction {
	
	@Override
	protected Map getKeyMethodMap() {
		return new Hashtable();
	}
	
	protected abstract Messages getMessages();
	
	@Override
	protected String getLookupMapName(HttpServletRequest request, String keyName, ActionMapping mapping) throws ServletException {
		try {
			synchronized (localeMap) {
				if (keyMethodMap == null || true)
					keyMethodMap = ((Localization.StrutsActionsRetriever)getMessages()).getStrutsActions((Class<LocalizedLookupDispatchAction>)this.getClass());
			}
			return (String)keyMethodMap.get(keyName);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}
}
