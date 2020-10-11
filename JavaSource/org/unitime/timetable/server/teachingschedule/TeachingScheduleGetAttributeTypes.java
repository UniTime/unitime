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
package org.unitime.timetable.server.teachingschedule;

import java.util.List;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.AttributeType;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetAttributeTypes;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.SessionContext;

@GwtRpcImplements(TeachingScheduleAPI.GetAttributeTypes.class)
public class TeachingScheduleGetAttributeTypes implements GwtRpcImplementation<TeachingScheduleAPI.GetAttributeTypes, GwtRpcResponseList<AttributeType>> {

	@Override
	public GwtRpcResponseList<AttributeType> execute(GetAttributeTypes request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		
		GwtRpcResponseList<AttributeType> ret = new GwtRpcResponseList<AttributeType>();
		for (InstructorAttributeType t: (List<InstructorAttributeType>)hibSession.createQuery("from InstructorAttributeType order by label").list()) {
			AttributeType at = new AttributeType();
			at.setId(t.getUniqueId());
			at.setReference(t.getReference());
			at.setLabel(t.getLabel());
			ret.add(at);
		}
		
		return ret;
	}
}
