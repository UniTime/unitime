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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.List;

import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomDegreePlansHolder;
import org.unitime.timetable.onlinesectioning.custom.DegreePlansProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class GetDegreePlans implements OnlineSectioningAction<List<DegreePlanInterface>> {
	private static final long serialVersionUID = 1L;
	
	public GetDegreePlans() {}
	
	protected Long iStudentId;
	
	public GetDegreePlans forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}

	@Override
	public List<DegreePlanInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		DegreePlansProvider provider = CustomDegreePlansHolder.getProvider();
		if (provider == null) return null;
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		
		if (iStudentId != null)
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
		
		XStudent student = (iStudentId == null ? null : server.getStudent(iStudentId));
		if (student == null)
			return provider.getDegreePlans(server, helper, new XStudent(null, helper.getStudentExternalId(), helper.getUser().getName()));
		
		action.getStudentBuilder().setExternalId(student.getExternalId());
		action.getStudentBuilder().setName(student.getName());
		
		return provider.getDegreePlans(server, helper, student);
	}

	@Override
	public String name() {
		return "degree-plans";
	}

}
