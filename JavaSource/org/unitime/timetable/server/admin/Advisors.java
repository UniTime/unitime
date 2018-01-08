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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
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
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.AdvisorDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=advisor]")
public class Advisors implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStudentAdvisor(), MESSAGES.pageStudentAdvisors());
	}

	@Override
	@PreAuthorize("checkPermission('StudentAdvisors')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> roles = new ArrayList<ListItem>();
		for (Roles r: Roles.findAll(true))
			if (r.hasRight(Right.StudentSchedulingAdvisor))
				roles.add(new ListItem(r.getReference(), r.getAbbv()));
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldFirstName(), FieldType.text, 200, 100),
				new Field(MESSAGES.fieldMiddleName(), FieldType.text, 200, 100),
				new Field(MESSAGES.fieldLastName(), FieldType.text, 200, 100),
				new Field(MESSAGES.fieldAcademicTitle(), FieldType.text, 100, 50),
				new Field(MESSAGES.fieldEmailAddress(), FieldType.text, 200, 200),
				new Field(MESSAGES.fieldRole(), FieldType.list, 100, roles, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldStudents(), FieldType.students, 200));
		data.setSortBy(4,2,3);
		for (Advisor advisor: AdvisorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(advisor.getUniqueId());
			r.setField(0, advisor.getExternalUniqueId());
			r.setField(1, advisor.getFirstName());
			r.setField(2, advisor.getMiddleName());
			r.setField(3, advisor.getLastName());
			r.setField(4, advisor.getAcademicTitle());
			r.setField(5, advisor.getEmail());
			r.setField(6, advisor.getRole().getReference());
			String students = "";
			for (Student student: new TreeSet<Student>(advisor.getStudents())) {
				if (!students.isEmpty()) students += "\n";
				students += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
			}
			r.setField(7, students);
		}
		data.setEditable(context.hasPermission(Right.StudentAdvisorEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StudentAdvisorEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		for (Advisor advisor: AdvisorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(advisor.getUniqueId());
			if (r == null)
				delete(advisor, context, hibSession, studentIds);
			else 
				update(advisor, r, context, hibSession, studentIds);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}

	protected void save(Record record, SessionContext context, Session hibSession, Set<Long> studentIds) {
		Advisor advisor = new Advisor();
		advisor.setExternalUniqueId(record.getField(0));
		advisor.setFirstName(record.getField(1));
		advisor.setMiddleName(record.getField(2));
		advisor.setLastName(record.getField(3));
		advisor.setAcademicTitle(record.getField(4));
		advisor.setEmail(record.getField(5));
		advisor.setRole(Roles.getRole(record.getField(6), hibSession));
		advisor.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		advisor.setStudents(new HashSet<Student>());
		if (record.getField(7) != null) {
			String students = "";
			for (String s: record.getField(7).split("\\n")) {
				if (s.indexOf(' ') >= 0) s = s.substring(0, s.indexOf(' '));
				if (s.trim().isEmpty()) continue;
				Student student = Student.findByExternalId(context.getUser().getCurrentAcademicSessionId(), s.trim());
				if (student != null) {
					advisor.getStudents().add(student);
					student.getAdvisors().add(advisor);
					if (!students.isEmpty()) students += "\n";
					students += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
					studentIds.add(student.getUniqueId());
				}
			}
			record.setField(7, students);
		}
		record.setUniqueId((Long)hibSession.save(advisor));
		ChangeLog.addChange(hibSession,
				context,
				advisor,
				advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentAdvisorEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		save(record, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}

	
	protected void update(Advisor advisor, Record record, SessionContext context, Session hibSession, Set<Long> studentIds) {
		if (advisor == null) return;
		boolean changed = 
				!ToolBox.equals(advisor.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(advisor.getFirstName(), record.getField(1)) ||
				!ToolBox.equals(advisor.getMiddleName(), record.getField(2)) ||
				!ToolBox.equals(advisor.getLastName(), record.getField(3)) ||
				!ToolBox.equals(advisor.getAcademicTitle(), record.getField(4)) ||
				!ToolBox.equals(advisor.getEmail(), record.getField(5)) ||
				!ToolBox.equals(advisor.getRole().getReference(), record.getField(6));
		advisor.setExternalUniqueId(record.getField(0));
		advisor.setFirstName(record.getField(1));
		advisor.setMiddleName(record.getField(2));
		advisor.setLastName(record.getField(3));
		advisor.setAcademicTitle(record.getField(4));
		advisor.setEmail(record.getField(5));
		advisor.setRole(Roles.getRole(record.getField(6), hibSession));
		Hashtable<String, Student> students = new Hashtable<String, Student>();
		for (Student s: advisor.getStudents())
			students.put(s.getExternalUniqueId(), s);
		for (String line: record.getField(7).split("\\n")) {
			String extId = (line.indexOf(' ') >= 0 ? line.substring(0, line.indexOf(' ')) : line).trim();
			if (extId.isEmpty() || students.remove(extId) != null) continue;
			Student student = Student.findByExternalId(context.getUser().getCurrentAcademicSessionId(), extId);
			if (student != null) {
				advisor.getStudents().add(student);
				student.getAdvisors().add(advisor);
				changed = true;
				studentIds.add(student.getUniqueId());
			}
		}
		if (!students.isEmpty()) {
			for (Student student: students.values()) {
				student.getAdvisors().remove(advisor);
				studentIds.add(student.getUniqueId());
			}
			advisor.getStudents().removeAll(students.values());
			changed = true;
		}
		String newStudents = "";
		for (Student student: new TreeSet<Student>(advisor.getStudents())) {
			if (!newStudents.isEmpty()) newStudents += "\n";
			newStudents += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
		}
		record.setField(7, newStudents);
		hibSession.saveOrUpdate(advisor);
		if (changed)
			ChangeLog.addChange(hibSession,
					context,
					advisor,
					advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
	}

	@Override
	@PreAuthorize("checkPermission('StudentAdvisorEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		update(AdvisorDAO.getInstance().get(record.getUniqueId()), record, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}

	protected void delete(Advisor advisor, SessionContext context, Session hibSession, Set<Long> studentIds) {
		if (advisor == null) return;
		if (advisor.getStudents() != null)
			for (Student student: advisor.getStudents()) {
				studentIds.add(student.getUniqueId());
				student.getAdvisors().remove(advisor);
			}
		ChangeLog.addChange(hibSession,
				context,
				advisor,
				advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(advisor);
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentAdvisorEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		delete(AdvisorDAO.getInstance().get(record.getUniqueId()), context, hibSession, studentIds);		
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}

}
