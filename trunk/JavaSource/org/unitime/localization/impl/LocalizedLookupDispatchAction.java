/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
