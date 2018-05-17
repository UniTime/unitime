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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.DateSelectorBackend;
import org.unitime.timetable.events.EventAction.HasPastOrOutside;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskOptionsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetTaskOptionsRpcRequest.class)
public class TaskOptionsBackend implements GwtRpcImplementation<GetTaskOptionsRpcRequest, TaskOptionsInterface>{

	@Override
	public TaskOptionsInterface execute(GetTaskOptionsRpcRequest request, SessionContext context) {
		context.checkPermission(Right.Tasks);
		
		TaskOptionsInterface options = new TaskOptionsInterface();
		
		options.setCanAdd(context.hasPermission(Right.TaskEdit));
		
		for (Script s: ScriptDAO.getInstance().findAll()) {
			ScriptInterface script = LoadAllScriptsBackend.load(s, context);
			if (script != null)
				options.addScript(script);
		}
		Collections.sort(options.getScripts());
		
		TimetableManager manager = TimetableManager.findByExternalId(context.getUser().getExternalUserId());
		if (manager != null) {
			String nameFormat = context.getUser().getProperty(UserProperty.NameFormat);
			ContactInterface contact = new ContactInterface();
			contact.setAcademicTitle(manager.getAcademicTitle());
			contact.setEmail(manager.getEmailAddress());
			contact.setExternalId(manager.getExternalUniqueId());
			contact.setFirstName(manager.getFirstName());
			contact.setMiddleName(manager.getMiddleName());
			contact.setLastName(manager.getLastName());
			contact.setFormattedName(manager.getName(nameFormat));
			options.setManager(contact);
		} else {
			options.setCanAdd(false);
		}
		
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		if (session != null) {
			options.setSession(new AcademicSessionInfo(session.getUniqueId(), session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(), session.getLabel()));
			options.setSessionMonth(DateSelectorBackend.listMonths(session, context.hasPermission(Right.EventDateMappings), new SimplePastOrOutside(session),
					ApplicationProperty.DatePatternNrExessMonth.intValue(), false));
		} else {
			options.setCanAdd(false);
		}
		
		return options;
	}

	static class SimplePastOrOutside implements HasPastOrOutside {
		private Date iToday;
		
		SimplePastOrOutside(Session session) {
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			iToday = cal.getTime();
		}
		
		@Override
		public boolean isOutside(Date date) {
			return false;
		}
		
		@Override
		public boolean isPast(Date date) {
			return date == null || date.before(iToday);
		}
		
		@Override
		public boolean isPastOrOutside(Date date) {
			return isPast(date) || isOutside(date);
		}
	}
}
