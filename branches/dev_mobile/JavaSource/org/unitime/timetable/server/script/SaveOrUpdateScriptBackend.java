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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.SaveOrUpdateScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SaveOrUpdateScriptRpcRequest.class)
public class SaveOrUpdateScriptBackend implements GwtRpcImplementation<SaveOrUpdateScriptRpcRequest, ScriptInterface> {

	@Override
	@PreAuthorize("checkPermission('ScriptEdit')")
	public ScriptInterface execute(SaveOrUpdateScriptRpcRequest request, SessionContext context) {
		org.hibernate.Session hibSession = ScriptDAO.getInstance().getSession();
		
		Long scriptId = request.getScript().getId();
		Script script = null;
		if (request.getScript().getId() == null) {
			script = new Script();
			script.setParameters(new HashSet<ScriptParameter>());
		} else {
			script = ScriptDAO.getInstance().get(scriptId, hibSession);
		}
		
		script.setName(request.getScript().getName());
		script.setDescription(request.getScript().getDescription());
		script.setPermission(request.getScript().getPermission());
		script.setEngine(request.getScript().getEngine());
		script.setScript(request.getScript().getScript());
		
		if (request.getScript().hasParameters()) {
			Map<String, ScriptParameter> params = new HashMap<String, ScriptParameter>();
			for (ScriptParameter parameter: script.getParameters())
				params.put(parameter.getName(), parameter);
			
			for (ScriptParameterInterface p: request.getScript().getParameters()) {
				ScriptParameter parameter = params.remove(p.getName());
				if (parameter == null) {
					parameter = new ScriptParameter();
					parameter.setName(p.getName());
					parameter.setScript(script);
					script.getParameters().add(parameter);
				}
				parameter.setType(p.getType());
				parameter.setLabel(p.getLabel());
				parameter.setDefaultValue(p.getDefaultValue());
			}
			
			for (ScriptParameter parameter: params.values()) {
				hibSession.delete(parameter);
				script.getParameters().remove(parameter);
			}
		} else {
			for (Iterator<ScriptParameter> i = script.getParameters().iterator(); i.hasNext(); ) {
				hibSession.delete(i.next());
				i.remove();
			}
		}
		
		hibSession.saveOrUpdate(script);
		hibSession.flush();
		
		return LoadAllScriptsBackend.load(script, context);
	}

}
