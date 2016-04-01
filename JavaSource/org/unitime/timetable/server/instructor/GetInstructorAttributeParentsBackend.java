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
package org.unitime.timetable.server.instructor;

import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.GetInstructorAttributeParentsRequest;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetInstructorAttributeParentsRequest.class)
public class GetInstructorAttributeParentsBackend implements GwtRpcImplementation<GetInstructorAttributeParentsRequest, GwtRpcResponseList<AttributeInterface>>{

	@Override
	public GwtRpcResponseList<AttributeInterface> execute(GetInstructorAttributeParentsRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorAttributes);
		
		InstructorAttribute current = (request.getAttributeId() == null ? null : InstructorAttributeDAO.getInstance().get(request.getAttributeId()));
		
		GwtRpcResponseList<AttributeInterface> response = new GwtRpcResponseList<AttributeInterface>();
		for (InstructorAttribute attribute: (List<InstructorAttribute>)InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute a where a.session.uniqueId = :sessionId and (a.department is null or a.department.uniqueId = :departmentId) and a.type.uniqueId = :typeId"
				).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setLong("departmentId", request.getDepartmentId() == null ? -1l : request.getDepartmentId())
				.setLong("typeId", request.getTypeId()).setCacheable(true).list()) {
			if (current == null || (!current.equals(attribute) && !current.isParentOf(attribute))) {
				AttributeInterface a = new AttributeInterface();
				a.setId(attribute.getUniqueId());
				a.setName(attribute.getName());
				response.add(a);
			}
		}
		return response;
	}

}
