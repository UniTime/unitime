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
package org.unitime.timetable.server.courses;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(DistributionsRequest.class)
public class DistributionsBackend implements GwtRpcImplementation<DistributionsRequest, DistributionsResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);

	@Override
	public DistributionsResponse execute(DistributionsRequest request, SessionContext context) {
		context.checkPermission(Right.DistributionPreferences);
		
		String subjectArea = request.getFilter().getParameterValue("subjectArea");
		if (subjectArea == null || subjectArea.isEmpty())
			throw new GwtRpcException(MESSAGES.errorSubjectRequired());
		
		DistributionsTableBuilder builder = new DistributionsTableBuilder(
				context, request.getBackType(), request.getBackId());
		
		for (FilterParameterInterface p: request.getFilter().getParameters()) {
			if ("subjectArea".equals(p.getName())) {
				context.setAttribute(SessionAttribute.OfferingsSubjectArea, p.getValue() != null ? p.getValue() : p.getDefaultValue());
			} else if ("subjectArea".equals(p.getName())) {
				context.setAttribute(SessionAttribute.OfferingsCourseNumber, p.getValue() != null ? p.getValue() : p.getDefaultValue());
			} else if (p.getValue() != null) {
				context.getUser().setProperty("Distributions." + p.getName(), p.getValue());
			}
		}
		DistributionsResponse response = new DistributionsResponse();
		
		for (String subjectAreaId: subjectArea.split(",")) {
			if (subjectAreaId.isEmpty()) continue;
			SubjectArea sa = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
			if (sa != null) {
				TableInterface table = builder.getDistPrefsTableForFilter(request.getFilter(), sa.getUniqueId());
				table.setId(sa.getUniqueId().toString());
				table.setName(sa.getLabel());
				response.addTable(table);
			}
		}
		
		BackTracker.markForBack(
				context, 
				"distributions",
				MESSAGES.backDistributionPreferences(), 
				true, true);


		return response;
	}

}
