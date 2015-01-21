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
