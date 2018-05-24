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
package org.unitime.timetable.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.model.base.BaseSavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class SavedHQL extends BaseSavedHQL {
	private static final long serialVersionUID = 2532519378106863655L;

	public SavedHQL() {
		super();
	}
	
	public static enum Flag {
		APPEARANCE_COURSES("Appearance: Courses", Right.HQLReportsCourses, "courses"),
		APPEARANCE_EXAMS("Appearance: Examinations", Right.HQLReportsExaminations, "exams"),
		APPEARANCE_SECTIONING("Appearance: Student Sectioning", Right.HQLReportsStudents, "sectioning"),
		APPEARANCE_EVENTS("Appearance: Events", Right.HQLReportsEvents, "events"),
		APPEARANCE_ADMINISTRATION("Appearance: Administration", Right.HQLReportsAdministration, "administration"),
		ADMIN_ONLY("Restrictions: Administrator Only", Right.HQLReportsAdminOnly)
		;
		private String iDescription;
		private String iAppearance;
		private Right iRight;
		Flag(String desc, Right right, String appearance) { iDescription = desc; iRight = right; iAppearance = appearance; }
		Flag(String desc, Right right) { this(desc, right, null); }
		public int flag() { return 1 << ordinal(); }
		public boolean isSet(int type) { return (type & flag()) != 0; }
		public String description() { return iDescription; }
		public String getAppearance() { return iAppearance; }
		public Right getPermission() { return iRight; }
	}
	
	private static interface OptionImplementation {
		public Map<Long, String> getValues(UserContext user);
		public Long lookupValue(UserContext user, String value);
		public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId);
	}

	private static class RefTableOptions implements OptionImplementation {
		private Class<? extends RefTableEntry> iReference;
		RefTableOptions(Class<? extends RefTableEntry> reference) { iReference = reference; }
		public Map<Long, String> getValues(UserContext user) {
			Map<Long, String> ret = new Hashtable<Long, String>();
			for (RefTableEntry ref: (List<RefTableEntry>)SessionDAO.getInstance().getSession().createCriteria(iReference).setCacheable(true).list())
				ret.put(ref.getUniqueId(), ref.getLabel());
			return ret;
		}
		@Override
		public Long lookupValue(UserContext user, String value) {
			for (RefTableEntry ref: (List<RefTableEntry>)SessionDAO.getInstance().getSession().createCriteria(iReference).setCacheable(true).list())
				if (value.equalsIgnoreCase(ref.getReference())) return ref.getUniqueId();
			return null;
		}
		@Override
		public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
			return originalId;
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

			@Override
			public Long lookupValue(UserContext user, String value) {
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", value).setMaxResults(1).uniqueResult();
			}

			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return newSessionId;
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

			@Override
			public Long lookupValue(UserContext user, String value) {
				for (Department d: Department.getUserDepartments(user))
					if (value.equalsIgnoreCase(d.getDeptCode())) return d.getUniqueId();
				return null;
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from Department d1, Department d2 where d1.uniqueId = :id and d1.deptCode = d2.deptCode and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
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
			
			@Override
			public Long lookupValue(UserContext user, String value) {
				for (SubjectArea s: SubjectArea.getUserSubjectAreas(user))
					if (value.equalsIgnoreCase(s.getSubjectAreaAbbreviation())) return s.getUniqueId();
				return null;
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from SubjectArea d1, SubjectArea d2 where d1.uniqueId = :id and d1.subjectAreaAbbreviation = d2.subjectAreaAbbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
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
			
			@Override
			public Long lookupValue(UserContext user, String value) {
				Map<Long, String> values = getValues(user);
				if (values != null)
					for (Map.Entry<Long, String> e: values.entrySet())
						if (value.equalsIgnoreCase(e.getValue())) return e.getKey();
				return null;
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from Building d1, Building d2 where d1.uniqueId = :id and d1.abbreviation = d2.abbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
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
			
			@Override
			public Long lookupValue(UserContext user, String value) {
				Map<Long, String> values = getValues(user);
				if (values != null)
					for (Map.Entry<Long, String> e: values.entrySet())
						if (value.equalsIgnoreCase(e.getValue())) return e.getKey();
				return null;
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from Room d1, Room d2 where d1.uniqueId = :id and d1.permanentId = d2.permanentId and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		ROOMS("Rooms", true, true, ROOM.iImplementation),
		LOCATION("Location", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Location r: (List<Location>)Location.findAllLocations(session.getUniqueId())){
					ret.put(r.getUniqueId(), r.getLabel());
				}
				return ret;
			}
			
			@Override
			public Long lookupValue(UserContext user, String value) {
				Map<Long, String> values = getValues(user);
				if (values != null)
					for (Map.Entry<Long, String> e: values.entrySet())
						if (value.equalsIgnoreCase(e.getValue())) return e.getKey();
				return null;
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from Location d1, Location d2 where d1.uniqueId = :id and d1.permanentId = d2.permanentId and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		LOCATIONS("Locations", true, true, LOCATION.iImplementation),
		PITD("Point In Time Data", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				List<PointInTimeData> pitdList = PointInTimeData.findAllSavedSuccessfullyForSession(sessionId); 
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (PointInTimeData pitd : pitdList){
					ret.put(pitd.getUniqueId(), pitd.getName());
				}
				return(ret);
			}
			@Override
			public Long lookupValue(UserContext user, String value) {
				Map<Long, String> values = getValues(user);
				if (values != null)
					for (Map.Entry<Long, String> e: values.entrySet())
						if (value.equalsIgnoreCase(e.getValue())) return e.getKey();
				return null;
			}
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return null;
			}
		}),
		SESSIONS("Academic Sessions", true, true, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (UserAuthority a: user.getAuthorities(user.getCurrentAuthority().getRole())) {
					UserQualifier session = a.getAcademicSession();
					if (session != null)
						ret.put((Long)session.getQualifierId(), session.getQualifierLabel());
				}
				return ret;
			}

			@Override
			public Long lookupValue(UserContext user, String value) {
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", value).setMaxResults(1).uniqueResult();
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return originalId;
			}
		}),
		STUDENT_GROUP("Student Group", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (StudentGroup g: (List<StudentGroup>)SessionDAO.getInstance().getSession().createQuery(
						"from StudentGroup where session.uniqueId = :sessionId").
						setLong("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list()) {
					ret.put(g.getUniqueId(), g.getGroupAbbreviation() + " - " + g.getGroupName());
				}
				return ret;
			}
			@Override
			public Long lookupValue(UserContext user, String value) {
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select uniqueId from StudentGroup where session.uniqueId = :sessionId and groupAbbreviation = :value"
						).setLong("sessionId", user.getCurrentAcademicSessionId())
						.setString("value", value).setCacheable(true).setMaxResults(1).uniqueResult();
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from StudentGroup d1, StudentGroup d2 where d1.uniqueId = :id and d1.groupAbbreviation = d2.groupAbbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		STUDENT_GROUPS("Student Groups", true, true, STUDENT_GROUP.iImplementation),
		ACADEMIC_AREA("Academic Area", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (AcademicArea a: (List<AcademicArea>)SessionDAO.getInstance().getSession().createQuery(
						"from AcademicArea where session.uniqueId = :sessionId").
						setLong("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list()) {
					ret.put(a.getUniqueId(), a.getAcademicAreaAbbreviation() + " - " + a.getTitle());
				}
				return ret;
			}
			@Override
			public Long lookupValue(UserContext user, String value) {
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select uniqueId from AcademicArea where session.uniqueId = :sessionId and academicAreaAbbreviation = :value"
						).setLong("sessionId", user.getCurrentAcademicSessionId())
						.setString("value", value).setCacheable(true).setMaxResults(1).uniqueResult();
			}
			
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from AcademicArea d1, AcademicArea d2 where d1.uniqueId = :id and d1.academicAreaAbbreviation = d2.academicAreaAbbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		ACADEMIC_AREAS("Academic Areas", true, true, ACADEMIC_AREA.iImplementation),
		POS_MAJOR("Major", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (PosMajor m: (List<PosMajor>)SessionDAO.getInstance().getSession().createQuery(
						"from PosMajor where session.uniqueId = :sessionId").
						setLong("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list()) {
					for (AcademicArea a: m.getAcademicAreas())
						ret.put(m.getUniqueId(), a.getAcademicAreaAbbreviation() + " " + m.getCode() + " - " + m.getName());
				}
				return ret;
			}
			@Override
			public Long lookupValue(UserContext user, String value) {
				Long id = (Long)SessionDAO.getInstance().getSession().createQuery(
						"select m.uniqueId from PosMajor m inner join m.academicAreas a where m.session.uniqueId = :sessionId and (a.academicAreaAbbreviation || ' ' || m.code) = :value"
						).setLong("sessionId", user.getCurrentAcademicSessionId())
						.setString("value", value).setCacheable(true).setMaxResults(1).uniqueResult();
				if (id != null) return id;
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select m.uniqueId from PosMajor m where m.session.uniqueId = :sessionId and m.code = :value"
						).setLong("sessionId", user.getCurrentAcademicSessionId())
						.setString("value", value).setCacheable(true).setMaxResults(1).uniqueResult();
			}
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from PosMajor d1 inner join d1.academicAreas a1, PosMajor d2 inner join d2.academicAreas a2 where d1.uniqueId = :id and d1.code = d2.code and a1.academicAreaAbbreviation = a2.academicAreaAbbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		POS_MAJORS("Majors", true, true, POS_MAJOR.iImplementation),
		ACCOMODATION("Student Accomodation", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (StudentAccomodation a: (List<StudentAccomodation>)SessionDAO.getInstance().getSession().createQuery(
						"from StudentAccomodation where session.uniqueId = :sessionId").
						setLong("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list()) {
					ret.put(a.getUniqueId(), a.getAbbreviation() + " - " + a.getName());
				}
				return ret;
			}
			@Override
			public Long lookupValue(UserContext user, String value) {
				return (Long)SessionDAO.getInstance().getSession().createQuery(
						"select uniqueId from StudentAccomodation where session.uniqueId = :sessionId and abbreviation = :value"
						).setLong("sessionId", user.getCurrentAcademicSessionId())
						.setString("value", value).setCacheable(true).setMaxResults(1).uniqueResult();
			}
			@Override
			public Long rollForward(org.hibernate.Session hibSession, Long originalId, Long sessionId, Long newSessionId) {
				return (Long)hibSession.createQuery("select d2.uniqueId from StudentAccomodation d1, StudentAccomodation d2 where d1.uniqueId = :id and d1.abbreviation = d2.abbreviation and d1.session = :s1 and d2.session = :s2")
						.setLong("id", originalId).setLong("s1", sessionId).setLong("s2", newSessionId).setMaxResults(1).uniqueResult();
			}
		}),
		ACCOMODATIONS("Student Accomodations", true, true, ACCOMODATION.iImplementation),

		
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
		Option(String name, boolean allowSelection, boolean multiSelect, OptionImplementation impl) {
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
		public Long lookupValue(UserContext user, String value) { return iImplementation.lookupValue(user, value); }
		public String rollForward(org.hibernate.Session hibSession, String value, Long sessionId, Long newSessionId) {
			if (value == null || value.isEmpty()) return value;
			if (iMultiSelect) {
				String ret = "";
				for (String id: value.split(",")) {
					try {
						Long converted = iImplementation.rollForward(hibSession, Long.valueOf(id), sessionId, newSessionId);
						if (converted != null)
							ret += (ret.isEmpty() ? "" : ",") + converted;
					} catch (Exception e) {
						ret += (ret.isEmpty() ? "" : ",") + id;
					}
				}
				return ret;
			} else {
				try {
					Long converted = iImplementation.rollForward(hibSession, Long.valueOf(value), sessionId, newSessionId);
					if (converted != null) return converted.toString();
					return null;
				} catch (Exception e) {
					return value;
				}
			}
		}
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
