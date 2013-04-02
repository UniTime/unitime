/*
 * UniTime 3.4 (University Timetabling Application)
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

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.GetScriptOptionsRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptOptionsInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(GetScriptOptionsRpcRequest.class)
public class ScriptOptionsBackend implements GwtRpcImplementation<GetScriptOptionsRpcRequest, ScriptOptionsInterface>{

	@Override
	@PreAuthorize("checkPermission('Scripts')")
	public ScriptOptionsInterface execute(GetScriptOptionsRpcRequest request, SessionContext context) {
		ScriptOptionsInterface options = new ScriptOptionsInterface();
		
		for (ScriptEngineFactory factory: new ScriptEngineManager().getEngineFactories())
			options.addEngine(factory.getLanguageName());
		
		for (Right right: Right.values()) {
			if (!right.hasType() || right.type().equals(Session.class) || right.type().equals(Department.class) || right.type().equals(SubjectArea.class))
				options.addPermission(right.toString());
		}
		
		options.setCanAdd(context.hasPermission(Right.ScriptEdit));
		
		return options;
	}

}
