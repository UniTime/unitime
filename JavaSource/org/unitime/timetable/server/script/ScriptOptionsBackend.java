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

/**
 * @author Tomas Muller
 */
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
		options.setEmail(context.getUser().getEmail());
		
		return options;
	}

}
