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
package org.unitime.timetable.server.admin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.cpsolver.ifs.util.ToolBox;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.StudentSectioningStatus.NotificationType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=sectioning]")
public class StudentSchedulingStatusTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStudentSchedulingStatusType(), MESSAGES.pageStudentSchedulingStatusTypes());
	}
	
	enum StatusOption {
		Access(MESSAGES.toggleAccess(), StudentSectioningStatus.Option.enabled),
		Enrollment(MESSAGES.toggleEnrollment(), StudentSectioningStatus.Option.enrollment),
		Advisor(MESSAGES.toggleAdvisor(), StudentSectioningStatus.Option.advisor),
		Admin(MESSAGES.toggleAdmin(), StudentSectioningStatus.Option.admin),
		RegAccess(MESSAGES.toggleRegAccess(), StudentSectioningStatus.Option.regenabled),
		Registration(MESSAGES.toggleRegistration(), StudentSectioningStatus.Option.registration),
		RegAdvisor(MESSAGES.toggleRegAdvisor(), StudentSectioningStatus.Option.regadvisor),
		RegAdmin(MESSAGES.toggleRegAdmin(), StudentSectioningStatus.Option.regadmin),
		Email(MESSAGES.toggleEmail(), StudentSectioningStatus.Option.email),
		ReSchedule(MESSAGES.toggleReSchedule(), StudentSectioningStatus.Option.reschedule),
		WaitListing(MESSAGES.toggleWaitList(), StudentSectioningStatus.Option.waitlist),
		NoSubs(MESSAGES.toggleNoSubs(), StudentSectioningStatus.Option.nosubs),
		NoBatch(MESSAGES.toggleNoBatch(), StudentSectioningStatus.Option.nobatch),
		AdvisorCanSet(MESSAGES.toggleAdvisorCanSetStatus(), StudentSectioningStatus.Option.advcanset),
		CReqValidation(MESSAGES.toggleCourseRequestValidation(), StudentSectioningStatus.Option.reqval),
		SpecReg(MESSAGES.toggleSpecialRequests(), StudentSectioningStatus.Option.specreg),
		CanReq(MESSAGES.toggleCanRequire(), StudentSectioningStatus.Option.canreq),
		NoSchedule(MESSAGES.toggleNoSchedule(), StudentSectioningStatus.Option.noschedule),
		;
		
		private StudentSectioningStatus.Option iOption;
		private String iLabel;
		
		StatusOption(String label, StudentSectioningStatus.Option option) {
			iLabel = label; iOption = option;  
		}
		
		public String getLabel() { return iLabel; }
		public StudentSectioningStatus.Option getOption() { return iOption; }		
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypes')")
	public SimpleEditInterface load(SessionContext context, org.hibernate.Session hibSession) {
		List<CourseType> courseTypes = CourseTypeDAO.getInstance().getSession().createQuery(
				"from CourseType order by reference", CourseType.class).setCacheable(true).list();
		SimpleEditInterface.Field[] fields = new SimpleEditInterface.Field[courseTypes.isEmpty() ? 10 + StatusOption.values().length : 11 + StatusOption.values().length];
		int idx = 0;
		fields[idx++] = new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE);
		fields[idx++] = new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE);
		for (StatusOption t: StatusOption.values())
			fields[idx++] = new Field(t.getLabel(), FieldType.toggle, 40);
		fields[idx++] = new Field(MESSAGES.fieldMessage(), FieldType.textarea, 40, 500);
		
		List<ListItem> notifications = new ArrayList<ListItem>();
		for (NotificationType t: NotificationType.values())
			notifications.add(new ListItem(t.name(), t.label()));
		fields[idx++] = new Field(MESSAGES.fieldNotifications(), FieldType.multi, 100, notifications); 

		if (!courseTypes.isEmpty()) {
			List<ListItem> courseTypeItems = new ArrayList<ListItem>();
			courseTypeItems.add(new ListItem("-", MESSAGES.toggleNoCourseType()));
			for (CourseType type: courseTypes)
				courseTypeItems.add(new ListItem(type.getUniqueId().toString(), type.getReference()));
			fields[idx++] = new Field(MESSAGES.fieldCourseTypes(), FieldType.multi, 100, courseTypeItems); 
		}

		List<ListItem> fallbacks = new ArrayList<ListItem>();
		List<StudentSectioningStatus> statuses = StudentSectioningStatus.findAll(context.getUser().getCurrentAcademicSessionId());
		fallbacks.add(new ListItem("", ""));
		for (StudentSectioningStatus status: statuses)
			fallbacks.add(new ListItem(status.getUniqueId().toString(), status.getLabel()));
		fields[idx++] = new Field(MESSAGES.fieldStudentStatusEffectiveStartDate(), FieldType.date, 80);
		fields[idx++] = new Field(MESSAGES.fieldStudentStatusEffectiveStartTime(), FieldType.time, 50);
		fields[idx++] = new Field(MESSAGES.fieldStudentStatusEffectiveEndDate(), FieldType.date, 80);
		fields[idx++] = new Field(MESSAGES.fieldStudentStatusEffectiveEndTime(), FieldType.time, 50);
		fields[idx++] = new Field(MESSAGES.fieldStudentStatusFallback(), FieldType.list, 100, fallbacks, Flag.NO_CYCLE);
		fields[idx++] = new Field(MESSAGES.fieldSession(), FieldType.toggle, 40);
		
		SimpleEditInterface data = new SimpleEditInterface(fields);
		data.setSortBy(0, 1);
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		for (StudentSectioningStatus status: statuses) {
			Record r = data.addRecord(status.getUniqueId());
			idx = 0;
			r.setField(idx++, status.getReference());
			r.setField(idx++, status.getLabel());
			for (StatusOption t: StatusOption.values())
				r.setField(idx++, status.hasOption(t.getOption()) ? "true" : "false");
			r.setField(idx++, status.getMessage());
			
			for (NotificationType t: NotificationType.values())
				if (status.hasNotification(t))
					r.addToField(idx, t.name());
			idx++;

			if (!courseTypes.isEmpty()) {
				for (CourseType type: courseTypes)
					if (status.getTypes().contains(type))
						r.addToField(idx, type.getUniqueId().toString());
				if (status.hasOption(StudentSectioningStatus.Option.notype))
					r.addToField(idx, "-");
				idx++;
			}

			r.setField(idx++, status.getEffectiveStartDate() == null ? "" : dateFormat.format(status.getEffectiveStartDate()));
			r.setField(idx++, status.getEffectiveStartPeriod() == null ? "" : status.getEffectiveStartPeriod().toString());
			r.setField(idx++, status.getEffectiveStopDate() == null ? "" : dateFormat.format(status.getEffectiveStopDate()));
			r.setField(idx++, status.getEffectiveStopPeriod() == null ? "" : status.getEffectiveStopPeriod().toString());
			r.setField(idx++, status.getFallBackStatus() == null ? "" : status.getFallBackStatus().getUniqueId().toString());
			r.setField(idx++, status.getSession() == null ? "false" : "true");
		}
		data.setEditable(context.hasPermission(Right.StudentSchedulingStatusTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		for (StudentSectioningStatus status: StudentSectioningStatus.findAll(context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(status.getUniqueId());
			if (r == null)
				delete(status, context, hibSession);
			else
				update(status, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void save(Record record, SessionContext context, org.hibernate.Session hibSession) {
		StudentSectioningStatus status = new StudentSectioningStatus();
		int value = 0;
		for (int i = 0; i < StatusOption.values().length; i++)
			if ("true".equals(record.getField(2 + i))) value += StatusOption.values()[i].getOption().toggle();
		status.setTypes(new HashSet<CourseType>());
		List<CourseType> courseTypes = CourseTypeDAO.getInstance().getSession().createQuery(
				"from CourseType order by reference", CourseType.class).setCacheable(true).list();
		status.setReference(record.getField(0));
		status.setLabel(record.getField(1));
		status.setStatus(value);
		status.setMessage(record.getField(2 + StatusOption.values().length));
		
		int notification = 0;
		for (String option: record.getValues(3 + StatusOption.values().length)) {
			notification += NotificationType.valueOf(option).toggle();
		}
		status.setNotifications(notification);
		
		int idx = 4 + StatusOption.values().length;
		status.setTypes(new HashSet<CourseType>());
		if (!courseTypes.isEmpty()) {
			for (String option: record.getValues(idx)) {
				if ("-".equals(option))
					value += StudentSectioningStatus.Option.notype.toggle();
				else
					status.getTypes().add(CourseTypeDAO.getInstance().get(Long.valueOf(option)));
			}
			idx ++;
		}
		
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		Date startDate = null;
		try {
			startDate = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : dateFormat.parse(record.getField(idx))); idx++;
		} catch (ParseException e) {}
		Integer startTime = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Integer.valueOf(record.getField(idx))); idx++;
		Date endDate = null;
		try {
			endDate = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : dateFormat.parse(record.getField(idx))); idx++;
		} catch (ParseException e) {}
		Integer endTime = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Integer.valueOf(record.getField(idx))); idx++;
		Long fallBackId = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Long.valueOf(record.getField(idx))); idx++;
		boolean session = "true".equals(record.getField(idx)); idx++;
		status.setEffectiveStartDate(startDate);
		status.setEffectiveStartPeriod(startTime);
		status.setEffectiveStopDate(endDate);
		status.setEffectiveStopPeriod(endTime);
		status.setFallBackStatus(fallBackId == null ? null : StudentSectioningStatusDAO.getInstance().get(fallBackId, hibSession));
		status.setSession(session ? SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()) : null);
		
		hibSession.persist(status);
		record.setUniqueId(status.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				status,
				status.getReference() + " " + status.getLabel() + (status.getSession() == null ? " (global)" : " (" + status.getSession().getLabel() + ")"),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(StudentSectioningStatus status, Record record, SessionContext context, org.hibernate.Session hibSession) {
		if (status == null) return;
		int value = 0;
		for (int i = 0; i < StatusOption.values().length; i++)
			if ("true".equals(record.getField(2 + i))) value += StatusOption.values()[i].getOption().toggle();
		Set<CourseType> types = new HashSet<CourseType>();
		List<CourseType> courseTypes = CourseTypeDAO.getInstance().getSession().createQuery(
				"from CourseType order by reference", CourseType.class).setCacheable(true).list();
		int notification = 0;
		for (String option: record.getValues(3 + StatusOption.values().length)) {
			notification += NotificationType.valueOf(option).toggle();
		}
		int idx = 4 + StatusOption.values().length;
		if (!courseTypes.isEmpty()) {
			for (String option: record.getValues(idx)) {
				if ("-".equals(option))
					value += StudentSectioningStatus.Option.notype.toggle();
				else
					types.add(CourseTypeDAO.getInstance().get(Long.valueOf(option)));
			}
			idx ++;
		}
		
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		Date startDate = null;
		try {
			startDate = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : dateFormat.parse(record.getField(idx))); idx++;
		} catch (ParseException e) {}
		Integer startTime = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Integer.valueOf(record.getField(idx))); idx++;
		Date endDate = null;
		try {
			endDate = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : dateFormat.parse(record.getField(idx))); idx++;
		} catch (ParseException e) {}
		Integer endTime = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Integer.valueOf(record.getField(idx))); idx++;
		Long fallBackId = (record.getField(idx) == null || record.getField(idx).isEmpty() ? null : Long.valueOf(record.getField(idx))); idx++;
		boolean session = "true".equals(record.getField(idx)); idx++;
		
		boolean changed = 
			!ToolBox.equals(status.getReference(), record.getField(0)) ||
			!ToolBox.equals(status.getLabel(), record.getField(1)) ||
			!ToolBox.equals(status.getStatus(), value) ||
			!ToolBox.equals(status.getTypes(), types) ||
			!ToolBox.equals(status.getMessage(), record.getField(2 + StatusOption.values().length)) ||
			!ToolBox.equals(status.getEffectiveStartDate(), startDate) || !ToolBox.equals(status.getEffectiveStartPeriod(), startTime) ||
			!ToolBox.equals(status.getEffectiveStopDate(), endDate) || !ToolBox.equals(status.getEffectiveStopPeriod(), endTime) ||
			!ToolBox.equals(status.getFallBackStatus() == null ? null : status.getFallBackStatus().getUniqueId(), fallBackId) ||
			!ToolBox.equals(status.getNotifications(), notification) ||
			(session && status.getSession() == null) || (!session && status.getSession() != null)
			;
		status.setReference(record.getField(0));
		status.setLabel(record.getField(1));
		status.setStatus(value);
		status.setTypes(types);
		status.setMessage(record.getField(2 + StatusOption.values().length));
		status.setEffectiveStartDate(startDate);
		status.setEffectiveStartPeriod(startTime);
		status.setEffectiveStopDate(endDate);
		status.setEffectiveStopPeriod(endTime);
		status.setFallBackStatus(fallBackId == null ? null : StudentSectioningStatusDAO.getInstance().get(fallBackId, hibSession));
		status.setSession(session ? SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()) : null);
		status.setNotifications(notification);
		hibSession.merge(status);
		if (changed)
			ChangeLog.addChange(hibSession,
					context,
					status,
					status.getReference() + " " + status.getLabel() + (status.getSession() == null ? " (global)" : " (" + status.getSession().getLabel() + ")"),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		
		if (session) {
			// session --> check other sessions with the same status, break it up if needed
			Map<Long, StudentSectioningStatus> others = new HashMap<Long, StudentSectioningStatus>();
			for (Session s: hibSession.createQuery("select distinct s.session from Student s where s.sectioningStatus.uniqueId = :uniqueId and s.session.uniqueId != :sessionId", Session.class
					).setParameter("uniqueId", status.getUniqueId()).setParameter("sessionId", status.getSession().getUniqueId()).list()) {
				StudentSectioningStatus other = hibSession.createQuery("from StudentSectioningStatus where session.uniqueId = :sessionId and reference = :reference", StudentSectioningStatus.class)
						.setParameter("sessionId", s.getUniqueId()).setParameter("reference", status.getReference()).uniqueResult();
				if (other == null) {
					System.out.println("Creating " + status.getReference() + " for " + s.getLabel());
					other = new StudentSectioningStatus();
					other.setSession(s);
					other.setReference(status.getReference());
					other.setLabel(status.getLabel());
					other.setMessage(status.getMessage());
					other.setStatus(status.getStatus());
					other.setEffectiveStartDate(status.getEffectiveStartDate());
					other.setEffectiveStartPeriod(status.getEffectiveStartPeriod());
					other.setEffectiveStopDate(status.getEffectiveStopDate());
					other.setEffectiveStopPeriod(status.getEffectiveStopPeriod());
					other.setFallBackStatus(status.getFallBackStatus() == null ? null : StudentSectioningStatus.getStatus(status.getFallBackStatus().getReference(), s.getUniqueId(), hibSession));
					other.setTypes(new HashSet<CourseType>(status.getTypes()));
					other.setNotifications(status.getNotifications());
					hibSession.persist(other);
					others.put(s.getUniqueId(), other);
					hibSession.flush();
				}
				hibSession.createMutationQuery("update Student set sectioningStatus.uniqueId = :newId where sectioningStatus.uniqueId = :oldId and session.uniqueId = :sessionId"
						).setParameter("newId", other.getUniqueId()).setParameter("oldId", status.getUniqueId()).setParameter("sessionId", s.getUniqueId()).executeUpdate();
			}
			for (Session s: hibSession.createQuery("from Session where defaultSectioningStatus.uniqueId = :uniqueId and uniqueId != :sessionId", Session.class
					).setParameter("uniqueId", status.getUniqueId()).setParameter("sessionId", status.getSession().getUniqueId()).list()) {
				StudentSectioningStatus other = others.get(s.getUniqueId());
				if (other == null)
					other = hibSession.createQuery("from StudentSectioningStatus where session.uniqueId = :sessionId and reference = :reference", StudentSectioningStatus.class)
						.setParameter("sessionId", s.getUniqueId()).setParameter("reference", status.getReference()).uniqueResult();
				if (other == null) {
					System.out.println("Creating " + status.getReference() + " for " + s.getLabel());
					other = new StudentSectioningStatus();
					other.setSession(s);
					other.setReference(status.getReference());
					other.setLabel(status.getLabel());
					other.setMessage(status.getMessage());
					other.setStatus(status.getStatus());
					other.setEffectiveStartDate(status.getEffectiveStartDate());
					other.setEffectiveStartPeriod(status.getEffectiveStartPeriod());
					other.setEffectiveStopDate(status.getEffectiveStopDate());
					other.setEffectiveStopPeriod(status.getEffectiveStopPeriod());
					other.setFallBackStatus(status.getFallBackStatus() == null ? null : StudentSectioningStatus.getStatus(status.getFallBackStatus().getReference(), s.getUniqueId(), hibSession));
					other.setTypes(new HashSet<CourseType>(status.getTypes()));
					other.setNotifications(status.getNotifications());
					hibSession.persist(other);
				}
				s.setDefaultSectioningStatus(other);
				hibSession.merge(s);
			}
		} else {
			// global -> there should be only one status with this reference (merge others if needed)
			for (StudentSectioningStatus other: hibSession.createQuery("from StudentSectioningStatus where uniqueId != :uniqueId and reference = :reference", StudentSectioningStatus.class
					).setParameter("uniqueId", status.getUniqueId()).setParameter("reference", status.getReference()).list()) {
				System.out.println("Removing " + other.getReference() + " from " + (other.getSession() == null ? "GLOBAL" : other.getSession().getLabel()));
				hibSession.createMutationQuery("update Student set sectioningStatus.uniqueId = :newId where sectioningStatus.uniqueId = :oldId"
						).setParameter("newId", status.getUniqueId()).setParameter("oldId", other.getUniqueId()).executeUpdate();
				hibSession.createMutationQuery("update Session set defaultSectioningStatus.uniqueId = :newId where defaultSectioningStatus.uniqueId = :oldId"
						).setParameter("newId", status.getUniqueId()).setParameter("oldId", other.getUniqueId()).executeUpdate();
				hibSession.remove(other);
			}
		}
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void update(Record record, SessionContext context, org.hibernate.Session hibSession) {
		update(StudentSectioningStatusDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(StudentSectioningStatus status, SessionContext context, org.hibernate.Session hibSession) {
		if (status == null) return;
		for (StudentSectioningStatus s: hibSession.createQuery(
				"from StudentSectioningStatus s where s.fallBackStatus.uniqueId = :statusId", StudentSectioningStatus.class).setParameter("statusId", status.getUniqueId()).list()) {
			s.setFallBackStatus(null); hibSession.merge(s);
		}
		ChangeLog.addChange(hibSession,
				context,
				status,
				status.getReference() + " " + status.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(status);
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void delete(Record record, SessionContext context, org.hibernate.Session hibSession) {
		delete(StudentSectioningStatusDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
