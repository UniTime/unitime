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

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface.VersionInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.VersionInfoRpcRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(VersionInfoRpcRequest.class)
public class VersionInfoBackend implements GwtRpcImplementation<VersionInfoRpcRequest, VersionInfoInterface> {
	@Override
	public VersionInfoInterface execute(VersionInfoRpcRequest request, SessionContext context) {
		VersionInfoInterface ret = new VersionInfoInterface();
		ret.setVersion(Constants.getVersion());
		ret.setBuildNumber(Constants.getBuildNumber());
		ret.setReleaseDate(Constants.getReleaseDate());
		return ret;
	}

}
