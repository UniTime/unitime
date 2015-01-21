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

import java.util.HashSet;
import java.util.Hashtable;
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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentAccomodationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=accommodations]")
public class StudentAccommodations implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStudentAccommodation(), MESSAGES.pageStudentAccommodations());
	}

	@Override
	@PreAuthorize("checkPermission('StudentAccommodations')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 80, 10, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 50, Flag.UNIQUE),
				new Field(MESSAGES.fieldStudents(), FieldType.students, 200));
		data.setSortBy(1,2);
		for (StudentAccomodation accomodation: StudentAccomodationDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(accomodation.getUniqueId());
			r.setField(0, accomodation.getExternalUniqueId());
			r.setField(1, accomodation.getAbbreviation());
			r.setField(2, accomodation.getName());
			String students = "";
			for (Student student: new TreeSet<Student>(accomodation.getStudents())) {
				if (!students.isEmpty()) students += "\n";
				students += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
			}
			r.setField(3, students, accomodation.getExternalUniqueId() == null);
			r.setDeletable(accomodation.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.StudentAccommodationEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StudentAccommodationEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		for (StudentAccomodation accomodation: StudentAccomodationDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(accomodation.getUniqueId());
			if (r == null)
				delete(accomodation, context, hibSession, studentIds);
			else 
				update(accomodation, r, context, hibSession, studentIds);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}
	
	protected void save(Record record, SessionContext context, Session hibSession, Set<Long> studentIds) {
		StudentAccomodation accomodation = new StudentAccomodation();
		accomodation.setExternalUniqueId(record.getField(0));
		accomodation.setAbbreviation(record.getField(1));
		accomodation.setName(record.getField(2));
		accomodation.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		accomodation.setStudents(new HashSet<Student>());
		if (record.getField(3) != null) {
			String students = "";
			for (String s: record.getField(3).split("\\n")) {
				if (s.indexOf(' ') >= 0) s = s.substring(0, s.indexOf(' '));
				if (s.trim().isEmpty()) continue;
				Student student = Student.findByExternalId(context.getUser().getCurrentAcademicSessionId(), s.trim());
				if (student != null) {
					accomodation.getStudents().add(student);
					student.getAccomodations().add(accomodation);
					if (!students.isEmpty()) students += "\n";
					students += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
					studentIds.add(student.getUniqueId());
				}
			}
			record.setField(3, students, true);
		}
		record.setUniqueId((Long)hibSession.save(accomodation));
		ChangeLog.addChange(hibSession,
				context,
				accomodation,
				accomodation.getAbbreviation() + " " + accomodation.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('StudentAccommodationEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		save(record, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}
	
	protected void update(StudentAccomodation accomodation, Record record, SessionContext context, Session hibSession, Set<Long> studentIds) {
		if (accomodation == null) return;
		boolean changed = 
				!ToolBox.equals(accomodation.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(accomodation.getAbbreviation(), record.getField(1)) ||
				!ToolBox.equals(accomodation.getName(), record.getField(2));
			accomodation.setExternalUniqueId(record.getField(0));
			accomodation.setAbbreviation(record.getField(1));
			accomodation.setName(record.getField(2));
			if (accomodation.getExternalUniqueId() == null && record.getField(3) != null) {
				Hashtable<String, Student> students = new Hashtable<String, Student>();
				for (Student s: accomodation.getStudents())
					students.put(s.getExternalUniqueId(), s);
				for (String line: record.getField(3).split("\\n")) {
					String extId = (line.indexOf(' ') >= 0 ? line.substring(0, line.indexOf(' ')) : line).trim();
					if (extId.isEmpty() || students.remove(extId) != null) continue;
					Student student = Student.findByExternalId(context.getUser().getCurrentAcademicSessionId(), extId);
					if (student != null) {
						accomodation.getStudents().add(student);
						student.getAccomodations().add(accomodation);
						changed = true;
						studentIds.add(student.getUniqueId());
					}
				}
				if (!students.isEmpty()) {
					for (Student student: students.values()) {
						studentIds.add(student.getUniqueId());
						student.getAccomodations().remove(accomodation);
					}
					accomodation.getStudents().removeAll(students.values());
					changed = true;
				}
				String newStudents = "";
				for (Student student: new TreeSet<Student>(accomodation.getStudents())) {
					if (!newStudents.isEmpty()) newStudents += "\n";
					newStudents += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
				}
				record.setField(3, newStudents, accomodation.getExternalUniqueId() == null);
			}
			hibSession.saveOrUpdate(accomodation);
			if (changed)
				ChangeLog.addChange(hibSession,
						context,
						accomodation,
						accomodation.getAbbreviation() + " " + accomodation.getName(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}

	@Override
	@PreAuthorize("checkPermission('StudentAccommodationEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		update(StudentAccomodationDAO.getInstance().get(record.getUniqueId()), record, context, hibSession, studentIds);
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}

	protected void delete(StudentAccomodation accomodation, SessionContext context, Session hibSession, Set<Long> studentIds) {
		if (accomodation == null) return;
		if (accomodation.getStudents() != null)
			for (Student student: accomodation.getStudents()) {
				studentIds.add(student.getUniqueId());
				student.getAccomodations().remove(accomodation);
			}
		ChangeLog.addChange(hibSession,
				context,
				accomodation,
				accomodation.getAbbreviation() + " " + accomodation.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(accomodation);
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentAccommodationEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		Set<Long> studentIds = new HashSet<Long>();
		delete(StudentAccomodationDAO.getInstance().get(record.getUniqueId()), context, hibSession, studentIds);		
		if (!studentIds.isEmpty())
			StudentSectioningQueue.studentChanged(hibSession, context.getUser(), context.getUser().getCurrentAcademicSessionId(), studentIds);
	}
}
