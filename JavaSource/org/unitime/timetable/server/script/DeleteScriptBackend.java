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
