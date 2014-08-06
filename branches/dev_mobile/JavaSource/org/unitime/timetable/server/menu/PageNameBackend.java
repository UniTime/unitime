/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.menu;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameRpcRequest;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PageNameRpcRequest.class)
public class PageNameBackend implements GwtRpcImplementation<PageNameRpcRequest, PageNameInterface> {
	private static PageNames sPageNames = Localization.create(PageNames.class);
	
	@Override
	public PageNameInterface execute(PageNameRpcRequest request, SessionContext context) {
		String name = request.getName().trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_');
		PageNameInterface ret = new PageNameInterface();
		if (ApplicationProperty.PageHelpEnabled.isTrue() && ApplicationProperty.PageHelpUrl.value() != null)
			ret.setHelpUrl(ApplicationProperty.PageHelpUrl.value() + name);
		ret.setName(sPageNames.translateMessage(name, request.getName()));
		return ret;
	}

}
