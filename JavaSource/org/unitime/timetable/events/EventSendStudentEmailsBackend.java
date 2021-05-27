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
package org.unitime.timetable.events;

import org.unitime.commons.Email;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.SendStudentEmailsRpcRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SendStudentEmailsRpcRequest.class)
public class EventSendStudentEmailsBackend implements GwtRpcImplementation<SendStudentEmailsRpcRequest, GwtRpcResponseNull> {

	@Override
	public GwtRpcResponseNull execute(SendStudentEmailsRpcRequest request, SessionContext context) {
		context.checkPermission(Right.EventDetailEmailStudents);
		context.checkPermission(request.getEventId(), "Event", Right.EventDetail);
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());

		try {
			Email email = Email.createEmail();
			if (request.hasCC())
				email.addRecipientCC(request.getCC(), null);
			email.setSubject(request.getSubject());
			email.setText(request.getMessage());
			
			for (Long studentId: request.getStudentIds()) {
				Student student = StudentDAO.getInstance().get(studentId);
				if (student != null && student.getEmail() != null && !student.getEmail().isEmpty())
					email.addRecipientBCC(student.getEmail(), student.getName(nameFormat));
			}

			if (context != null && context.isAuthenticated() && context.getUser().getEmail() != null)
				email.setReplyTo(context.getUser().getEmail(), context.getUser().getName());
			else if (request.hasCC())
				email.setReplyTo(request.getCC(), null);
			
			email.send();
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
		return null;
	}
	

}
