/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Type;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CourseCreditFormatDAO;
import org.unitime.timetable.model.dao.CourseCreditTypeDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;
import org.unitime.timetable.model.dao.PositionTypeDAO;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("simpleEdit.gwt")
public class SimpleEditServlet implements SimpleEditService {
	private static Logger sLog = Logger.getLogger(SimpleEditServlet.class);

	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	
	@Override
	public SimpleEditInterface load(Type type) throws SimpleEditException, PageAccessException {
		getSessionContext().checkPermission(type2right(type));
		org.hibernate.Session hibSession = null;
		try {
			hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			SimpleEditInterface data = null;
			switch (type) {
			case area:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40, Flag.READ_ONLY),
						new Field("Abbreviation", FieldType.text, 80, 10, Flag.UNIQUE),
						new Field("Short Title", FieldType.text, 200, 50, Flag.UNIQUE),
						new Field("Long Title", FieldType.text, 500, 100)
						);
				data.setSortBy(1,2,3);
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(area.getUniqueId());
					r.setField(0, area.getExternalUniqueId());
					r.setField(1, area.getAcademicAreaAbbreviation());
					r.setField(2, area.getShortTitle());
					r.setField(3, area.getLongTitle());
					r.setDeletable(area.getExternalUniqueId() == null);
				}
				break;
			case classification:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40, Flag.READ_ONLY),
						new Field("Code", FieldType.text, 80, 10, Flag.UNIQUE),
						new Field("Name", FieldType.text, 500, 50, Flag.UNIQUE));
				data.setSortBy(1,2);
				for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(clasf.getUniqueId());
					r.setField(0, clasf.getExternalUniqueId());
					r.setField(1, clasf.getCode());
					r.setField(2, clasf.getName());
					r.setDeletable(clasf.getExternalUniqueId() == null);
				}
				break;
			case major:
				List<ListItem> areas = new ArrayList<ListItem>();
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + (area.getLongTitle() == null ? area.getShortTitle() : area.getLongTitle())));
				}
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40, Flag.READ_ONLY),
						new Field("Code", FieldType.text, 80, 10, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 50, Flag.UNIQUE),
						new Field("Academic Area", FieldType.list, 300, areas));
				data.setSortBy(3,1,2);
				for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(major.getUniqueId());
					r.setField(0, major.getExternalUniqueId());
					r.setField(1, major.getCode());
					r.setField(2, major.getName());
					r.setDeletable(major.getExternalUniqueId() == null);
					for (AcademicArea area: major.getAcademicAreas())
						r.setField(3, area.getUniqueId().toString());
				}
				break;
			case minor:
				areas = new ArrayList<ListItem>();
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + (area.getLongTitle() == null ? area.getShortTitle() : area.getLongTitle())));
				}
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40, Flag.READ_ONLY),
						new Field("Code", FieldType.text, 80, 10, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 50, Flag.UNIQUE),
						new Field("Academic Area", FieldType.list, 300, areas));
				data.setSortBy(3,1,2);
				for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(minor.getUniqueId());
					r.setField(0, minor.getExternalUniqueId());
					r.setField(1, minor.getCode());
					r.setField(2, minor.getName());
					for (AcademicArea area: minor.getAcademicAreas())
						r.setField(3, area.getUniqueId().toString());
					r.setDeletable(minor.getExternalUniqueId() == null);
				}
				break;
			case group:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40, Flag.READ_ONLY),
						new Field("Code", FieldType.text, 80, 10, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 50, Flag.UNIQUE),
						new Field("Students", FieldType.students, 200));
				data.setSortBy(1,2);
				for (StudentGroup group: StudentGroupDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(group.getUniqueId());
					r.setField(0, group.getExternalUniqueId());
					r.setField(1, group.getGroupAbbreviation());
					r.setField(2, group.getGroupName());
					String students = "";
					for (Student student: new TreeSet<Student>(group.getStudents())) {
						if (!students.isEmpty()) students += "\n";
						students += student.getExternalUniqueId() + " " + student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
					}
					r.setField(3, students, group.getExternalUniqueId() == null);
					r.setDeletable(group.getExternalUniqueId() == null);
				}
				break;
			case consent:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
						new Field("Abbreviation", FieldType.text, 160, 20, Flag.UNIQUE));
				data.setSortBy(0, 1);
				data.setAddable(false);
				for (OfferingConsentType consent: OfferingConsentTypeDAO.getInstance().findAll()) {
					Record r = data.addRecord(consent.getUniqueId(), false);
					r.setField(0, consent.getReference(), false);
					r.setField(1, consent.getLabel(), true);
					r.setField(2, consent.getAbbv(), true);
				}
				break;
			case creditFormat:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Abbreviation", FieldType.text, 80, 10));
				data.setSortBy(0, 1, 2);
				data.setAddable(false);
				for (CourseCreditFormat credit: CourseCreditFormatDAO.getInstance().findAll()) {
					Record r = data.addRecord(credit.getUniqueId(), false);
					r.setField(0, credit.getReference(), false);
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case creditType:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
						new Field("Abbreviation", FieldType.text, 80, 10, Flag.UNIQUE));
				data.setSortBy(0, 1, 2);
				for (CourseCreditType credit: CourseCreditTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(c) from CourseCreditUnitConfig c where c.creditType.uniqueId = :uniqueId")
								.setLong("uniqueId", credit.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(credit.getUniqueId(), used == 0);
					r.setField(0, credit.getReference());
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case creditUnit:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
						new Field("Abbreviation", FieldType.text, 80, 10, Flag.UNIQUE));
				data.setSortBy(0, 1, 2);
				for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(c) from CourseCreditUnitConfig c where c.creditUnitType.uniqueId = :uniqueId")
								.setLong("uniqueId", credit.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(credit.getUniqueId(), used == 0);
					r.setField(0, credit.getReference());
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case position:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
						new Field("Sort Order", FieldType.number, 80, 10, Flag.UNIQUE)
						);
				data.setSortBy(2, 0, 1);
				DecimalFormat df = new DecimalFormat("0000");
				for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(f) from Staff f where f.positionType.uniqueId = :uniqueId")
								.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue() +
						((Number)hibSession.createQuery(
								"select count(f) from DepartmentalInstructor f where f.positionType.uniqueId = :uniqueId")
								.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(position.getUniqueId(), used == 0);
					r.setField(0, position.getReference());
					r.setField(1, position.getLabel());
					r.setField(2, df.format(position.getSortOrder()));
				}
				break;
			case sectioning:
				data = new SimpleEditInterface(type,
						new Field("Abbreviation", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Access", FieldType.toggle, 40),
						new Field("Advisor", FieldType.toggle, 40),
						new Field("Email", FieldType.toggle, 40),
						new Field("Message", FieldType.text, 400, 200)
						);
				data.setSortBy(0, 1);
				for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll()) {
					Record r = data.addRecord(status.getUniqueId());
					r.setField(0, status.getReference());
					r.setField(1, status.getLabel());
					r.setField(2, status.hasOption(StudentSectioningStatus.Option.enabled) ? "true" : "false");
					r.setField(3, status.hasOption(StudentSectioningStatus.Option.advisor) ? "true" : "false");
					r.setField(4, status.hasOption(StudentSectioningStatus.Option.email) ? "true" : "false");
					r.setField(5, status.getMessage());
				}
				break;	
			case roles:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 250, 40, Flag.UNIQUE),
						new Field("Instructor", FieldType.toggle, 40),
						new Field("Enabled", FieldType.toggle, 40),
						new Field("Sort Order", FieldType.text, 80, 10, Flag.READ_ONLY, Flag.HIDDEN)
						);
				data.setSortBy(4);
				int idx = 0;
				for (Roles role: Roles.findAll(false)) {
					Record r = data.addRecord(role.getRoleId(), (role.isManager() || role.isInstructor()) && !role.isUsed());
					r.setField(0, role.getReference(), role.isManager() || role.isInstructor());
					r.setField(1, role.getAbbv());
					r.setField(2, role.isInstructor() ? "true" : "false");
					r.setField(3, role.isEnabled() ? "true" : "false");
					r.setField(4, String.valueOf(idx++));
				}
				break;
			case permissions:
				List<Roles> roles = new ArrayList<Roles>(Roles.findAll(false));
				Field[] fields = new Field[2 + roles.size()];
				fields[0] = new Field("Name", FieldType.text, 160, 200, Flag.READ_ONLY);
				fields[1] = new Field("Level", FieldType.text, 160, 200, Flag.READ_ONLY);
				for (int i = 0; i < roles.size(); i++) {
					fields[2 + i] = new Field(roles.get(i).getReference(), FieldType.toggle, 40, roles.get(i).isEnabled() ? null : Flag.HIDDEN);
				}
				data = new SimpleEditInterface(type, fields);
				data.setSortBy(-1);
				data.setAddable(false);
				data.setSaveOrder(false);
				for (Right right: Right.values()) {
					Record r = data.addRecord((long)right.ordinal(), false);
					r.setField(0, right.toString(), false);
					r.setField(1, right.hasType() ? right.type().getSimpleName().replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2").replace("_", " ") : "Global", false);
					for (int i = 0; i < roles.size(); i++)
						r.setField(2 + i, roles.get(i).getRights().contains(right.name()) ? "true" : "false");
				}
				break;
			case examType:
				List<ListItem> types = new ArrayList<ListItem>();
				types.add(new ListItem(String.valueOf(ExamType.sExamTypeFinal), "Final Examinations"));
				types.add(new ListItem(String.valueOf(ExamType.sExamTypeMidterm), "Midterm Examinations"));
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Type", FieldType.list, 300, types)
						);
				data.setSortBy(2, 1);
				for (ExamType xtype: ExamTypeDAO.getInstance().findAll()) {
					Record r = data.addRecord(xtype.getUniqueId());
					r.setField(0, xtype.getReference(), !xtype.getReference().equals("final") && !xtype.getReference().equals("midterm"));
					r.setField(1, xtype.getLabel());
					r.setField(2, xtype.getType().toString(), !xtype.getReference().equals("final") && !xtype.getReference().equals("midterm"));
					r.setDeletable(!xtype.isUsed(null) && !xtype.getReference().equals("final") && !xtype.getReference().equals("midterm"));
				}
				break;
			case eventRoomType:
				List<ListItem> states = new ArrayList<ListItem>();
				for (RoomTypeOption.Status state: RoomTypeOption.Status.values()) {
					states.add(new ListItem(String.valueOf(state.ordinal()), state.toString()));
				}

				data = new SimpleEditInterface(type,
						new Field("Department", FieldType.text, 160, Flag.READ_ONLY),
						new Field("Room Type", FieldType.text, 100, Flag.READ_ONLY),
						new Field("Status", FieldType.list, 300, states, Flag.NOT_EMPTY),
						new Field("Message", FieldType.text, 500, 200),
						new Field("Break Time", FieldType.text, 50, 10),
						new Field("Sort Order", FieldType.text, 80, 10, Flag.READ_ONLY, Flag.HIDDEN)
						);
				data.setSortBy(0, 5);
				data.setAddable(false);
				long id = 0;
				for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
					if (!department.isAllowEvents()) continue;
					for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
							"select distinct r.roomType from Room r where r.eventDepartment.uniqueId = :departmentId order by r.roomType.ord, r.roomType.label")
							.setLong("departmentId", department.getUniqueId()).list()) {
						RoomTypeOption option = roomType.getOption(department);
						Record r = data.addRecord(id++, false);
						r.setField(0, department.getLabel(), false);
						r.setField(1, roomType.getLabel(), false);
						r.setField(2, String.valueOf(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus()));
						r.setField(3, option.getMessage() == null ? "" : option.getMessage());
						r.setField(4, option.getBreakTime() == null ? "0" : option.getBreakTime().toString());
						r.setField(5, roomType.getOrd().toString());
					}
					for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
							"select distinct r.roomType from NonUniversityLocation r where r.eventDepartment.uniqueId = :departmentId order by r.roomType.ord, r.roomType.label")
							.setLong("departmentId", department.getUniqueId()).list()) {
						RoomTypeOption option = roomType.getOption(department);
						Record r = data.addRecord(id++, false);
						r.setField(0, department.getLabel(), false);
						r.setField(1, roomType.getLabel(), false);
						r.setField(2, String.valueOf(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus()));
						r.setField(3, option.getMessage() == null ? "" : option.getMessage());
						r.setField(4, option.getBreakTime() == null ? "0" : option.getBreakTime().toString());
						r.setField(5, roomType.getOrd().toString());
					}
				}
				break;
			case featureType:
				data = new SimpleEditInterface(type,
						new Field("Abbreviation", FieldType.text, 160, 20, Flag.UNIQUE),
						new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
						new Field("Event Management", FieldType.toggle, 40)
						);
				data.setSortBy(2, 1);
				for (RoomFeatureType ftype: RoomFeatureTypeDAO.getInstance().findAll()) {
					Record r = data.addRecord(ftype.getUniqueId());
					r.setField(0, ftype.getReference());
					r.setField(1, ftype.getLabel());
					r.setField(2, ftype.isShowInEventManagement() ? "true" : "false");
					int used =
							((Number)hibSession.createQuery(
									"select count(f) from RoomFeature f where f.featureType.uniqueId = :uniqueId")
									.setLong("uniqueId", ftype.getUniqueId()).uniqueResult()).intValue();
					r.setDeletable(used == 0);
				}
				break;
			case instructorRole:
				List<ListItem> departments = new ArrayList<ListItem>();
				List<ListItem> instructorRoles = new ArrayList<ListItem>();
				instructorRoles.add(new ListItem("", ""));
				for (Roles role: Roles.findAllInstructorRoles()) {
					instructorRoles.add(new ListItem(role.getUniqueId().toString(), role.getAbbv()));
				}
				data = new SimpleEditInterface(type,
						new Field("Department", FieldType.list, 160, departments),
						new Field("Instructor", FieldType.person, 300),
						new Field("Role", FieldType.list, 300, instructorRoles)
						);
				data.setSortBy(0, 1);
				
				boolean deptIndep = sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);

				for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
					if (!department.isAllowEvents()) continue;
					departments.add(new ListItem(department.getUniqueId().toString(), department.getLabel()));
					for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery(
							"from DepartmentalInstructor i where i.department.uniqueId = :departmentId and i.externalUniqueId is not null order by i.lastName, i.firstName")
							.setLong("departmentId", department.getUniqueId()).list()) {
						if (deptIndep && instructor.getRole() == null) continue;
						Record r = data.addRecord(instructor.getUniqueId(), false);
						r.setField(0, instructor.getDepartment().getLabel(), false);
						r.setField(1, null, false);
						r.addToField(1, instructor.getLastName() == null ? "" : instructor.getLastName());
						r.addToField(1, instructor.getFirstName() == null ? "" : instructor.getFirstName());
						r.addToField(1, instructor.getMiddleName() == null ? "" : instructor.getMiddleName());
						r.addToField(1, instructor.getExternalUniqueId());
						r.addToField(1, instructor.getEmail() == null ? "" : instructor.getEmail());
						r.setField(2, instructor.getRole() == null ? "" : instructor.getRole().getUniqueId().toString());
						r.setDeletable(deptIndep);
					}
				}
				break;
			}
			data.setEditable(getSessionContext().hasPermission(type2editRight(type)));
			return data;
		} catch (PageAccessException e) {
			throw e;
		} catch (SimpleEditException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	@Override
	public SimpleEditInterface save(SimpleEditInterface data) throws SimpleEditException, PageAccessException {
		getSessionContext().checkPermission(type2editRight(data.getType()));
		org.hibernate.Session hibSession = null;
		try {
			hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				switch (data.getType()) {
				case area:
					for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(area.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									area,
									area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(area);
						} else {
							boolean changed = 
								!ToolBox.equals(area.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(area.getAcademicAreaAbbreviation(), r.getField(1)) ||
								!ToolBox.equals(area.getShortTitle(), r.getField(2)) ||
								!ToolBox.equals(area.getLongTitle(), r.getField(3));
							area.setExternalUniqueId(r.getField(0));
							area.setAcademicAreaAbbreviation(r.getField(1));
							area.setShortTitle(r.getField(2));
							area.setLongTitle(r.getField(3));
							hibSession.saveOrUpdate(area);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										area,
										area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						AcademicArea area = new AcademicArea();
						area.setExternalUniqueId(r.getField(0));
						area.setAcademicAreaAbbreviation(r.getField(1));
						area.setShortTitle(r.getField(2));
						area.setLongTitle(r.getField(3));
						area.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(area));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								area,
								area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case classification:
					for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(clasf.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									clasf,
									clasf.getCode() + " " + clasf.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(clasf);
						} else {
							boolean changed = 
								!ToolBox.equals(clasf.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(clasf.getCode(), r.getField(1)) ||
								!ToolBox.equals(clasf.getName(), r.getField(2));
							clasf.setExternalUniqueId(r.getField(0));
							clasf.setCode(r.getField(1));
							clasf.setName(r.getField(2));
							hibSession.saveOrUpdate(clasf);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										clasf,
										clasf.getCode() + " " + clasf.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						AcademicClassification clasf = new AcademicClassification();
						clasf.setExternalUniqueId(r.getField(0));
						clasf.setCode(r.getField(1));
						clasf.setName(r.getField(2));
						clasf.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(clasf));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								clasf,
								clasf.getCode() + " " + clasf.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case major:
					for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(major.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									major,
									major.getCode() + " " + major.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(major);
						} else {
							boolean changed =
								!ToolBox.equals(major.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(major.getCode(), r.getField(1)) ||
								!ToolBox.equals(major.getName(), r.getField(2));
							major.setExternalUniqueId(r.getField(0));
							major.setCode(r.getField(1));
							major.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(major.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									major.getAcademicAreas().add(area);
									area.getPosMajors().add(major);
									changed = true;
								}
							}
							for (AcademicArea area: delete) {
								major.getAcademicAreas().remove(area);
								area.getPosMajors().remove(major);
								changed = true;
							}
							hibSession.saveOrUpdate(major);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										major,
										major.getCode() + " " + major.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						PosMajor major = new PosMajor();
						major.setExternalUniqueId(r.getField(0));
						major.setCode(r.getField(1));
						major.setName(r.getField(2));
						major.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						major.setAcademicAreas(new HashSet<AcademicArea>());
						for (String areaId: r.getValues(3)) {
							AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
							major.getAcademicAreas().add(area);
							area.getPosMajors().add(major);
						}
						r.setUniqueId((Long)hibSession.save(major));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								major,
								major.getCode() + " " + major.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}
					break;
				case minor:
					for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(minor.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									minor,
									minor.getCode() + " " + minor.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(minor);
						} else {
							boolean changed =
								!ToolBox.equals(minor.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(minor.getCode(), r.getField(1)) ||
								!ToolBox.equals(minor.getName(), r.getField(2));
							minor.setExternalUniqueId(r.getField(0));
							minor.setCode(r.getField(1));
							minor.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(minor.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									minor.getAcademicAreas().add(area);
									area.getPosMinors().add(minor);
									changed = true;
								}
							}
							for (AcademicArea area: delete) {
								minor.getAcademicAreas().remove(area);
								area.getPosMinors().remove(minor);
								changed = true;
							}
							hibSession.saveOrUpdate(minor);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										minor,
										minor.getCode() + " " + minor.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						PosMinor minor = new PosMinor();
						minor.setExternalUniqueId(r.getField(0));
						minor.setCode(r.getField(1));
						minor.setName(r.getField(2));
						minor.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						minor.setAcademicAreas(new HashSet<AcademicArea>());
						for (String areaId: r.getValues(3)) {
							AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
							minor.getAcademicAreas().add(area);
							area.getPosMinors().add(minor);
						}
						r.setUniqueId((Long)hibSession.save(minor));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								minor,
								minor.getCode() + " " + minor.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}
					break;
				case group:
					for (StudentGroup group: StudentGroupDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(group.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									group,
									group.getGroupAbbreviation() + " " + group.getGroupName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(group);
						} else {
							boolean changed = 
								!ToolBox.equals(group.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(group.getGroupAbbreviation(), r.getField(1)) ||
								!ToolBox.equals(group.getGroupName(), r.getField(2));
							group.setExternalUniqueId(r.getField(0));
							group.setGroupAbbreviation(r.getField(1));
							group.setGroupName(r.getField(2));
							if (group.getExternalUniqueId() == null && r.getField(3) != null) {
								Hashtable<String, Student> students = new Hashtable<String, Student>();
								for (Student s: group.getStudents())
									students.put(s.getExternalUniqueId(), s);
								for (String line: r.getField(3).split("\\n")) {
									String extId = (line.indexOf(' ') >= 0 ? line.substring(0, line.indexOf(' ')) : line).trim();
									if (extId.isEmpty() || students.remove(extId) != null) continue;
									Student student = Student.findByExternalId(sessionId, extId);
									if (student != null) {
										group.getStudents().add(student);
										changed = true;
									}
								}
								if (!students.isEmpty()) {
									group.getStudents().removeAll(students.values());
									changed = true;
								}
							}
							hibSession.saveOrUpdate(group);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										group,
										group.getGroupAbbreviation() + " " + group.getGroupName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						StudentGroup group = new StudentGroup();
						group.setExternalUniqueId(r.getField(0));
						group.setGroupAbbreviation(r.getField(1));
						group.setGroupName(r.getField(2));
						group.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						group.setStudents(new HashSet<Student>());
						if (r.getField(3) != null) {
							for (String s: r.getField(3).split("\\n")) {
								if (s.indexOf(' ') >= 0) s = s.substring(0, s.indexOf(' '));
								if (s.trim().isEmpty()) continue;
								Student student = Student.findByExternalId(sessionId, s.trim());
								if (student != null)
									group.getStudents().add(student);
							}
						}
						r.setUniqueId((Long)hibSession.save(group));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								group,
								group.getGroupAbbreviation() + " " + group.getGroupName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case consent:
					for (OfferingConsentType consent: OfferingConsentTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(consent.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									consent,
									consent.getReference() + " " + consent.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(consent);
						} else {
							boolean changed = 
								!ToolBox.equals(consent.getReference(), r.getField(0)) ||
								!ToolBox.equals(consent.getLabel(), r.getField(1)) ||
								!ToolBox.equals(consent.getAbbv(), r.getField(2));
							consent.setReference(r.getField(0));
							consent.setLabel(r.getField(1));
							consent.setAbbv(r.getField(2));
							hibSession.saveOrUpdate(consent);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										consent,
										consent.getReference() + " " + consent.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						OfferingConsentType consent = new OfferingConsentType();
						consent.setReference(r.getField(0));
						consent.setLabel(r.getField(1));
						consent.setAbbv(r.getField(2));
						r.setUniqueId((Long)hibSession.save(consent));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								consent,
								consent.getReference() + " " + consent.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditFormat:
					for (CourseCreditFormat credit: CourseCreditFormatDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditFormat credit = new CourseCreditFormat();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditType:
					for (CourseCreditType credit: CourseCreditTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditType credit = new CourseCreditType();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditUnit:
					for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditUnitType credit = new CourseCreditUnitType();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case position:
					for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(position.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									position,
									position.getReference() + " " + position.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(position);
						} else {
							boolean changed = 
								!ToolBox.equals(position.getReference(), r.getField(0)) ||
								!ToolBox.equals(position.getLabel(), r.getField(1)) ||
								!ToolBox.equals(position.getSortOrder().toString(), r.getField(2));
							position.setReference(r.getField(0));
							position.setLabel(r.getField(1));
							position.setSortOrder(Integer.valueOf(r.getField(2)));
							hibSession.saveOrUpdate(position);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										position,
										position.getReference() + " " + position.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						PositionType position = new PositionType();
						position.setReference(r.getField(0));
						position.setLabel(r.getField(1));
						position.setSortOrder(Integer.valueOf(r.getField(2)));
						r.setUniqueId((Long)hibSession.save(position));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								position,
								position.getReference() + " " + position.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case sectioning:
					for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll()) {
						Record r = data.getRecord(status.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									status,
									status.getReference() + " " + status.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(status);
						} else {
							int value = 0;
							if ("true".equals(r.getField(2))) value += StudentSectioningStatus.Option.enabled.toggle();
							if ("true".equals(r.getField(3))) value += StudentSectioningStatus.Option.advisor.toggle();
							if ("true".equals(r.getField(4))) value += StudentSectioningStatus.Option.email.toggle();
							boolean changed = 
								!ToolBox.equals(status.getReference(), r.getField(0)) ||
								!ToolBox.equals(status.getLabel(), r.getField(1)) ||
								!ToolBox.equals(status.getStatus(), value) ||
								!ToolBox.equals(status.getMessage(), r.getField(5));
							status.setReference(r.getField(0));
							status.setLabel(r.getField(1));
							status.setStatus(value);
							status.setMessage(r.getField(5));
							hibSession.saveOrUpdate(status);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										status,
										status.getReference() + " " + status.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						StudentSectioningStatus status = new StudentSectioningStatus();
						int value = 0;
						if ("true".equals(r.getField(2))) value += StudentSectioningStatus.Option.enabled.toggle();
						if ("true".equals(r.getField(3))) value += StudentSectioningStatus.Option.advisor.toggle();
						if ("true".equals(r.getField(4))) value += StudentSectioningStatus.Option.email.toggle();
						status.setReference(r.getField(0));
						status.setLabel(r.getField(1));
						status.setStatus(value);
						status.setMessage(r.getField(5));
						r.setUniqueId((Long)hibSession.save(status));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								status,
								status.getReference() + " " + status.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case roles:
					for (Roles role: RolesDAO.getInstance().findAll()) {
						Record r = data.getRecord(role.getRoleId());
						if (r == null) {
							if (!role.isManager())
								throw new PageAccessException("Role "  + role.getAbbv() + " cannot be deleted.");
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									role,
									role.getAbbv() + " role",
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(role);
						} else {
							boolean changed = 
								!ToolBox.equals(role.getReference(), r.getField(0)) ||
								!ToolBox.equals(role.getAbbv(), r.getField(1)) ||
								!ToolBox.equals(role.isInstructor(), "true".equals(r.getField(2))) ||
								!ToolBox.equals(role.isEnabled(), "true".equals(r.getField(3)));
							role.setReference(r.getField(0));
							role.setAbbv(r.getField(1));
							role.setInstructor("true".equals(r.getField(2)));
							role.setEnabled("true".equals(r.getField(3)));
							hibSession.saveOrUpdate(role);
							if (changed)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										role,
										role.getAbbv() + " role",
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						Roles role = new Roles();
						role.setReference(r.getField(0));
						role.setAbbv(r.getField(1));
						role.setInstructor("true".equals(r.getField(2)));
						role.setEnabled("true".equals(r.getField(3)));
						role.setManager(true);
						r.setUniqueId((Long)hibSession.save(role));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								role,
								role.getAbbv() + " role",
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case permissions:
					List<Roles> roles = new ArrayList<Roles>(Roles.findAll(false));
					Set<Roles> changed = new HashSet<Roles>();
					for (Record r: data.getRecords()) {
						Right right = Right.values()[(int)r.getUniqueId().longValue()];
						for (int i = 0; i < roles.size(); i++) {
							boolean newValue = "true".equals(r.getField(2 + i));
							boolean oldValue = roles.get(i).getRights().contains(right.name());
							if (newValue != oldValue) {
								changed.add(roles.get(i));
								if (newValue) roles.get(i).getRights().add(right.name());
								else roles.get(i).getRights().remove(right.name());
							}
						}
					}
					for (Roles role: changed) {
						hibSession.saveOrUpdate(role);
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								role,
								role.getAbbv() + " permissions",
								Source.SIMPLE_EDIT, 
								Operation.UPDATE,
								null,
								null);
					}
					break;
				case examType:
					for (ExamType type: ExamTypeDAO.getInstance().findAll(hibSession)) {
						Record r = data.getRecord(type.getUniqueId());
						if (r == null) {
							if (type.isUsed(null))
								throw new SimpleEditException("Attempted to delete an examination type " + type.getReference() + " that is being used.");
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									type,
									type.getReference(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(type);
						} else {
							boolean typeChanged = 
								!ToolBox.equals(type.getReference(), r.getField(0)) ||
								!ToolBox.equals(type.getLabel(), r.getField(1)) ||
								!ToolBox.equals(type.getType(), Integer.valueOf(r.getField(2)));
							type.setReference(r.getField(0));
							type.setLabel(r.getField(1));
							type.setType(Integer.valueOf(r.getField(2)));
							hibSession.saveOrUpdate(type);
							if (typeChanged)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										type,
										type.getReference(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						ExamType type = new ExamType();
						type.setReference(r.getField(0));
						type.setLabel(r.getField(1));
						type.setType(Integer.valueOf(r.getField(2)));
						r.setUniqueId((Long)hibSession.save(type));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								type,
								type.getReference(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case eventRoomType:
					for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
						if (!department.isAllowEvents()) continue;
						for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
								"select distinct r.roomType from Room r where r.eventDepartment.uniqueId = :departmentId order by r.roomType.ord, r.roomType.label")
								.setLong("departmentId", department.getUniqueId()).list()) {
							RoomTypeOption option = roomType.getOption(department);
							for (Record r: data.getRecords()) {
								if (r.getField(0).equals(department.getLabel()) && r.getField(1).equals(roomType.getLabel())) {
									boolean optionChanged = 
											!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(r.getField(2))) ||
											!ToolBox.equals(option.getMessage(), r.getField(3)) ||
											!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), r.getField(4));
									option.setStatus(Integer.parseInt(r.getField(2)));
									option.setMessage(r.getField(3));
									try {
										option.setBreakTime(Integer.parseInt(r.getField(4)));
									} catch (NumberFormatException e) {
										option.setBreakTime(0);
									}
									hibSession.saveOrUpdate(option);
									if (optionChanged)
										ChangeLog.addChange(hibSession,
												getSessionContext(),
												option.getRoomType(),
												option.getRoomType().getLabel(),
												Source.SIMPLE_EDIT, 
												Operation.UPDATE,
												null,
												option.getDepartment());
								}
							}
						}
						for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
								"select distinct r.roomType from NonUniversityLocation r where r.eventDepartment.uniqueId = :departmentId order by r.roomType.ord, r.roomType.label")
								.setLong("departmentId", department.getUniqueId()).list()) {
							RoomTypeOption option = roomType.getOption(department);
							for (Record r: data.getRecords()) {
								if (r.getField(0).equals(department.getLabel()) && r.getField(1).equals(roomType.getLabel())) {
									boolean optionChanged = 
											!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(r.getField(2))) ||
											!ToolBox.equals(option.getMessage(), r.getField(3)) ||
											!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), r.getField(4));
									option.setStatus(Integer.parseInt(r.getField(2)));
									option.setMessage(r.getField(3));
									try {
										option.setBreakTime(Integer.parseInt(r.getField(4)));
									} catch (NumberFormatException e) {
										option.setBreakTime(0);
									}
									hibSession.saveOrUpdate(option);
									if (optionChanged)
										ChangeLog.addChange(hibSession,
												getSessionContext(),
												option.getRoomType(),
												option.getRoomType().getLabel(),
												Source.SIMPLE_EDIT, 
												Operation.UPDATE,
												null,
												option.getDepartment());
								}
							}
						}
					}
					break;					
				case featureType:
					for (RoomFeatureType type: RoomFeatureTypeDAO.getInstance().findAll(hibSession)) {
						Record r = data.getRecord(type.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									type,
									type.getReference(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(type);
						} else {
							boolean typeChanged = 
								!ToolBox.equals(type.getReference(), r.getField(0)) ||
								!ToolBox.equals(type.getLabel(), r.getField(1)) ||
								!ToolBox.equals(type.getShowInEventManagement(), "true".equals(r.getField(2)));
							type.setReference(r.getField(0));
							type.setLabel(r.getField(1));
							type.setShowInEventManagement("true".equals(r.getField(2)));
							hibSession.saveOrUpdate(type);
							if (typeChanged)
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										type,
										type.getReference(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						RoomFeatureType type = new RoomFeatureType();
						type.setReference(r.getField(0));
						type.setLabel(r.getField(1));
						type.setShowInEventManagement("true".equals(r.getField(2)));
						r.setUniqueId((Long)hibSession.save(type));
						ChangeLog.addChange(hibSession,
								getSessionContext(),
								type,
								type.getReference(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case instructorRole:
					for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
						if (!department.isAllowEvents()) continue;
						List<DepartmentalInstructor> instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
								"from DepartmentalInstructor i where i.department.uniqueId = :departmentId and i.externalUniqueId is not null order by i.lastName, i.firstName")
								.setLong("departmentId", department.getUniqueId()).list();
						for (DepartmentalInstructor instructor: instructors) {
							Record r = data.getRecord(instructor.getUniqueId());
							if (r == null) {
								if (instructor.getRole() == null) continue;
								
								instructor.setRole(null);
								
								hibSession.update(instructor);
								
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										instructor,
										instructor.getName(DepartmentalInstructor.sNameFormatLastInitial) + ": No Role",
										Source.SIMPLE_EDIT, 
										Operation.DELETE,
										null,
										instructor.getDepartment());
							} else {
								if (ToolBox.equals(instructor.getRole() == null ? "" : instructor.getRole().getUniqueId().toString(), r.getField(2))) continue;
								
								instructor.setRole(r.getField(2) == null || r.getField(2).isEmpty() ? null : RolesDAO.getInstance().get(Long.valueOf(r.getField(2))));
								
								hibSession.update(instructor);
								
								ChangeLog.addChange(hibSession,
										getSessionContext(),
										instructor,
										instructor.getName(DepartmentalInstructor.sNameFormatLastInitial) + ": " + (instructor.getRole() == null ? "No Role" : instructor.getRole().getAbbv()),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										instructor.getDepartment());								
							}
						}
					
						for (Record r: data.getNewRecords()) {
							if (!department.getUniqueId().toString().equals(r.getField(0))) continue;
							
							if (r.getField(1) == null || r.getField(1).isEmpty()) continue;
							
							String[] name = r.getValues(1);

							DepartmentalInstructor instructor = null;
							boolean add = true;
							
							for (DepartmentalInstructor i: instructors)
								if (name[3].equals(i.getExternalUniqueId())) {
									instructor = i;
									add = false;
									break;
								}
							
							if (instructor == null) {
								instructor = new DepartmentalInstructor();
								instructor.setExternalUniqueId(name[3]);
								instructor.setLastName(name[0]);
								instructor.setFirstName(name[1]);
								instructor.setMiddleName(name[2].isEmpty() ? null : name[2]);
								instructor.setEmail(name.length <=4 || name[4].isEmpty() ? null : name[4]);
								instructor.setIgnoreToFar(false);
								instructor.setDepartment(department);

								instructor.setRole(r.getField(2) == null || r.getField(2).isEmpty() ? null : RolesDAO.getInstance().get(Long.valueOf(r.getField(2))));

								r.setUniqueId((Long)hibSession.save(instructor));
							} else {
								r.setUniqueId(instructor.getUniqueId());
								instructor.setRole(r.getField(2) == null || r.getField(2).isEmpty() ? null : RolesDAO.getInstance().get(Long.valueOf(r.getField(2))));

								hibSession.update(instructor);
							}
							
							r.setDeletable(false);
							r.setField(0, r.getField(0), false);
							r.setField(1, r.getField(1), false);
							
							ChangeLog.addChange(hibSession,
									getSessionContext(),
									instructor,
									instructor.getName(DepartmentalInstructor.sNameFormatLastInitial) + ": " + (instructor.getRole() == null ? "No Role" : instructor.getRole().getAbbv()),
									Source.SIMPLE_EDIT, 
									(add ? Operation.CREATE : Operation.UPDATE),
									null,
									instructor.getDepartment());
						}
					}
					break;
				}
				hibSession.flush();
				tx.commit(); tx = null;
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
			for (Iterator<Record> i = data.getRecords().iterator(); i.hasNext(); )
				if (i.next().getUniqueId() == null) i.remove();
			return data;
		} catch (PageAccessException e) {
			throw e;
		} catch (SimpleEditException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	private Long getAcademicSessionId() {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentAuthority() == null) throw new PageAccessException("Insufficient user privileges.");
		Long sessionId = (Long) user.getCurrentAcademicSessionId();
		if (sessionId == null) throw new PageAccessException("Insufficient user privileges.");
		return sessionId;
	}
	
	private Right type2right(Type type) {
		switch (type) {
		case area:
			return Right.AcademicAreas;
		case classification:
			return Right.AcademicClassifications;
		case major:
			return Right.Majors;
		case minor:
			return Right.Minors;
		case group:
			return Right.StudentGroups;
		case consent:
			return Right.OfferingConsentTypes;
		case creditFormat:
			return Right.CourseCreditFormats;
		case creditType:
			return Right.CourseCreditTypes;
		case creditUnit:
			return Right.CourseCreditUnits;
		case position:
			return Right.PositionTypes;
		case sectioning:
			return Right.StudentSchedulingStatusTypes;
		case roles:
			return Right.Roles;
		case permissions:
			return Right.Permissions;
		case examType:
			return Right.ExamTypes;
		case eventRoomType:
			return Right.EventRoomTypes;
		case featureType:
			return Right.RoomFeatures;
		case instructorRole:
			return Right.InstructorRoles;
		default:
			return Right.IsAdmin;
		}
	}
	
	private Right type2editRight(Type type) {
		switch (type) {
		case area:
			return Right.AcademicAreaEdit;
		case classification:
			return Right.AcademicClassificationEdit;
		case major:
			return Right.MajorEdit;
		case minor:
			return Right.MinorEdit;
		case group:
			return Right.StudentGroupEdit;
		case consent:
			return Right.OfferingConsentTypeEdit;
		case creditFormat:
			return Right.CourseCreditFormatEdit;
		case creditType:
			return Right.CourseCreditTypeEdit;
		case creditUnit:
			return Right.CourseCreditUnitEdit;
		case position:
			return Right.PositionTypeEdit;
		case sectioning:
			return Right.StudentSchedulingStatusTypeEdit;
		case roles:
			return Right.RoleEdit;
		case permissions:
			return Right.PermissionEdit;
		case examType:
			return Right.ExamTypeEdit;
		case eventRoomType:
			return Right.EventRoomTypeEdit;
		case featureType:
			return Right.RoomFeatureTypeEdit;
		case instructorRole:
			return Right.InstructorRoleEdit;
		default:
			return Right.IsAdmin;
		}
	}
	
}
