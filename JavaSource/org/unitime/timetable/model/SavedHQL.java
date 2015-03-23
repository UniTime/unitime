/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.model.base.BaseSavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class SavedHQL extends BaseSavedHQL {
	private static final long serialVersionUID = 2532519378106863655L;

	public SavedHQL() {
		super();
	}
	
	public static enum Flag {
		APPEARANCE_COURSES("Appearance: Courses", "courses"),
		APPEARANCE_EXAMS("Appearance: Examinations", "exams"),
		APPEARANCE_SECTIONING("Appearance: Student Sectioning", "sectioning"),
		APPEARANCE_EVENTS("Appearance: Events", "events"),
		APPEARANCE_ADMINISTRATION("Appearance: Administration", "administration"),
		ADMIN_ONLY("Restrictions: Administrator Only")
		;
		private String iDescription;
		private String iAppearance;
		Flag(String desc, String appearance) { iDescription = desc; iAppearance = appearance; }
		Flag(String desc) { this(desc, null); }
		public int flag() { return 1 << ordinal(); }
		public boolean isSet(int type) { return (type & flag()) != 0; }
		public String description() { return iDescription; }
		public String getAppearance() { return iAppearance; }
	}
	
	private static interface OptionImplementation {
		public Map<Long, String> getValues(UserContext user);
	}

	private static class RefTableOptions implements OptionImplementation {
		private Class<? extends RefTableEntry> iReference;
		RefTableOptions(Class<? extends RefTableEntry> reference) { iReference = reference; }
		public Map<Long, String> getValues(UserContext user) {
			Map<Long, String> ret = new Hashtable<Long, String>();
			for (RefTableEntry ref: (List<RefTableEntry>)SessionDAO.getInstance().getSession().createCriteria(iReference).list())
				ret.put(ref.getUniqueId(), ref.getLabel());
			return ret;
		}
	}

	public static enum Option {
		SESSION("Academic Session", false, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				ret.put(session.getUniqueId(), session.getLabel());
				return ret;
			}
		}),
		DEPARTMENT("Department", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Department d: Department.getUserDepartments(user))
					ret.put(d.getUniqueId(), d.htmlLabel());
				return ret;
			}
		}),
		DEPARTMENTS("Departments", true, true, DEPARTMENT.iImplementation),
		SUBJECT("Subject Area", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				try {
					for (SubjectArea s: SubjectArea.getUserSubjectAreas(user)) {
						ret.put(s.getUniqueId(), s.getSubjectAreaAbbreviation());
					}
				} catch (Exception e) { return null; }
				return ret;
			}
		}),
		SUBJECTS("Subject Areas", true, true, SUBJECT.iImplementation),
		BUILDING("Building", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Building b: (List<Building>)Building.findAll(session.getUniqueId()))
					ret.put(b.getUniqueId(), b.getAbbrName());
				return ret;
			}
		}),
		BUILDINGS("Buildings", true, true, BUILDING.iImplementation),
		ROOM("Room", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Room r: (List<Room>)Room.findAllRooms(session.getUniqueId())){
					ret.put(r.getUniqueId(), r.getLabel());
				}
				return ret;
			}
		}),
		ROOMS("Rooms", true, true, ROOM.iImplementation),
		
		DistributionType(DistributionType.class, false),
		DistributionTypes(DistributionType.class, true),
		DemandOfferingType(DemandOfferingType.class, false),
		DemandOfferingTypes(DemandOfferingType.class, true),
		OfferingConsentType(OfferingConsentType.class, false),
		OfferingConsentTypes(OfferingConsentType.class, true),
		CourseCreditFormat(CourseCreditFormat.class, false),
		CourseCreditFormats(CourseCreditFormat.class, true),
		CourseCreditType(CourseCreditType.class, false),
		CourseCreditTypes(CourseCreditType.class, true),
		CourseCreditUnitType(CourseCreditUnitType.class, false),
		CourseCreditUnitTypes(CourseCreditUnitType.class, true),
		PositionType(PositionType.class, false),
		PositionTypes(PositionType.class, true),
		DepartmentStatusType(DepartmentStatusType.class, false),
		DepartmentStatusTypes(DepartmentStatusType.class, true),
		RoomType(RoomType.class, false),
		RoomTypes(RoomType.class, true),
		StudentSectioningStatus(StudentSectioningStatus.class, false),
		StudentSectioningStatuses(StudentSectioningStatus.class, true),
		ExamType(ExamType.class, false),
		ExamTypes(ExamType.class, true),
		RoomFeatureType(RoomFeatureType.class, false),
		RoomFeatureTypes(RoomFeatureType.class, true),
		CourseType(CourseType.class, false),
		CourseTypes(CourseType.class, true),
		;
		
		String iName;
		OptionImplementation iImplementation;
		boolean iAllowSelection, iMultiSelect;
		Option(String name, boolean allowSelection, boolean multiSelect ,OptionImplementation impl) {
			iName = name;
			iAllowSelection = allowSelection; iMultiSelect = multiSelect;
			iImplementation = impl;
		}
		Option(Class<? extends RefTableEntry> reference, boolean multiSelect) {
			iName = name().replaceAll("(?<=[^A-Z])([A-Z])"," $1");
			iAllowSelection = true; iMultiSelect = multiSelect;
			iImplementation = new RefTableOptions(reference);
		}
		
		public String text() { return iName; }
		public boolean allowSingleSelection() { return iAllowSelection; }
		public boolean allowMultiSelection() { return iAllowSelection && iMultiSelect; }
		public Map<Long, String> values(UserContext user) { return iImplementation.getValues(user); }
	}
	
	public static void main(String args[]) {
		for (Flag f: Flag.values()) {
			System.out.println(f.name() + ": " + f.flag() + " (" + f.isSet(0xFF) + ")");
		}
	}
	
	public boolean isSet(Flag f) {
		return getType() != null && f.isSet(getType());
	}
	
	public void set(Flag f) {
		if (!isSet(f))
			setType((getType() == null ? 0 : getType()) + f.flag());
	}
	
	public void clear(Flag f) {
		if (isSet(f)) setType(getType() - f.flag());
	}
	
	public static List<SavedHQL> listAll(org.hibernate.Session hibSession, Flag appearance, boolean admin) {
		if (admin) {
			return (List<SavedHQL>)(hibSession == null ? SavedHQLDAO.getInstance().getSession() : hibSession).createQuery(
					"from SavedHQL q where bit_and(q.type, :flag) > 0 order by q.name")
					.setInteger("flag", appearance.flag())
					.setCacheable(true).list();
		} else {
			return (List<SavedHQL>)(hibSession == null ? SavedHQLDAO.getInstance().getSession() : hibSession).createQuery(
					"from SavedHQL q where bit_and(q.type, :flag) > 0 and bit_and(q.type, :admin) = 0 order by q.name")
					.setInteger("flag", appearance.flag())
					.setInteger("admin", Flag.ADMIN_ONLY.flag())
					.setCacheable(true).list();
		}
	}
	
	public static boolean hasQueries(Flag appearance, boolean admin) {
		if (admin) {
			return ((Number)SavedHQLDAO.getInstance().getSession().createQuery(
					"select count(q) from SavedHQL q where bit_and(q.type, :flag) > 0")
					.setInteger("flag", appearance.flag())
					.setCacheable(true).uniqueResult()).intValue() > 0;
		} else {
			return ((Number)SavedHQLDAO.getInstance().getSession().createQuery(
					"select count(q) from SavedHQL q where bit_and(q.type, :flag) > 0 and bit_and(q.type, :admin) = 0")
					.setInteger("flag", appearance.flag())
					.setInteger("admin", Flag.ADMIN_ONLY.flag())
					.setCacheable(true).uniqueResult()).intValue() > 0;
		}
	}

}
