/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.script;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.DeleteScriptRpcRequest;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(DeleteScriptRpcRequest.class)
public class DeleteScriptBackend implements GwtRpcImplementation<DeleteScriptRpcRequest, ScriptInterface> {

	@Override
	@PreAuthorize("checkPermission('ScriptEdit')")
	public ScriptInterface execute(DeleteScriptRpcRequest request, SessionContext context) {
		org.hibernate.Session hibSession = ScriptDAO.getInstance().getSession();
		
		Script script = ScriptDAO.getInstance().get(request.getScriptId(), hibSession);
		hibSession.delete(script);
		
		hibSession.flush();
		
		return null;
	}

}
