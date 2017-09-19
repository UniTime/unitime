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
package org.unitime.timetable.server.solver;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.MakeAssignmentRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(MakeAssignmentRequest.class)
public class MakeAssignmentBackend implements GwtRpcImplementation<MakeAssignmentRequest, GwtRpcResponseNull> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public GwtRpcResponseNull execute(MakeAssignmentRequest request, SessionContext context) {
		context.checkPermission(Right.Suggestions);
		
		SuggestionsContext cx = new SuggestionsContext();
		String instructorFormat = context.getUser().getProperty(UserProperty.NameFormat);
    	if (instructorFormat != null)
    		cx.setInstructorNameFormat(instructorFormat);
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver == null)
			throw new GwtRpcException(MESSAGES.warnSolverNotLoaded());
		if (solver.isWorking())
			throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
		
		solver.assignSelectedAssignments(request.getAssignments());
		
		return null;
	}

}
