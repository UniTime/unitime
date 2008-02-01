/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseSession;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.ReferenceList;


/**
 * @hibernate.class table="SESSIONS" schema = "TIMETABLE"
 */
public class Session extends BaseSession implements Comparable {

	public static int sNrExcessDays = 31;
	
	public static int sHolidayTypeNone = 0;

	public static int sHolidayTypeHoliday = 1;

	public static int sHolidayTypeBreak = 2;

	public static String[] sHolidayTypeNames = new String[] { "No Holiday",
			"Holiday", "(Spring/October/Thanksgiving) Break" };

	public static String[] sHolidayTypeColors = new String[] {
			"rgb(240,240,240)", "rgb(200,30,20)", "rgb(240,50,240)" };

	private static final long serialVersionUID = 3691040980400813366L;

	static String mappingTable = "timetable.ll_course_mapping";

	/*
	 * @return all sessions
	 */
	public static TreeSet getAllSessions() throws HibernateException {
		return new TreeSet((new SessionDAO()).findAll());
	}

	/**
	 * @param id
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSessionById(Long id) throws HibernateException {
		return (new SessionDAO()).get(id);
	}

	/**
	 * @param id
	 * @throws HibernateException
	 */
	public static void deleteSessionById(Long id) throws HibernateException {
		org.hibernate.Session hibSession = new SessionDAO().getSession();
		Transaction tx = null;
		try {
		    tx = hibSession.beginTransaction();
		    Session session = new SessionDAO().get(id, hibSession);
            hibSession.createQuery(
                    "delete StudentClassEnrollment e where e.student.uniqueId in " +
                    "(select s.uniqueId from Student s where s.session.uniqueId=:sessionId)").
                    setLong("sessionId", id).
                    executeUpdate();
		    hibSession.delete(session);
		    tx.commit();
		} catch (HibernateException e) {
		    try {
                if (tx!=null && tx.isActive()) tx.rollback();
            } catch (Exception e1) { }
            throw e;
		}
	}

	public void saveOrUpdate() throws HibernateException {
		(new SessionDAO()).saveOrUpdate(this);
	}

	public String getAcademicYearTerm() {
		return (getAcademicYear() + getAcademicTerm());
	}

	public boolean isDefault() throws HibernateException {
		Session defSessn = Session.defaultSession();
		return ((defSessn == null) ? false : this.getSessionId().equals(
				defSessn.getSessionId()));
	}

	public boolean getIsDefault() throws HibernateException {
		return isDefault();
	}
	
	public static Session defaultSession() throws HibernateException {
	    return defaultSession(getAllSessions());
	}
	
	public static Set availableSessions(ManagerRole role) {
	    if (Roles.ADMIN_ROLE.equals(role.getRole().getReference()))
	        return getAllSessions();
	    Set sessions = role.getTimetableManager().sessionsCanManage();
	    if (Roles.VIEW_ALL_ROLE.equals(role.getRole().getReference()) && sessions.isEmpty())
	        return getAllSessions();
	    return sessions;
	}

    public static Session defaultSession(ManagerRole role) throws HibernateException {
        return defaultSession(availableSessions(role));
    }

    public static Session defaultSession(Set sessions) throws HibernateException {
        if (sessions==null || sessions.isEmpty()) return null; // no session -> no default
        TreeSet orderedSession = (sessions instanceof TreeSet?(TreeSet)sessions:new TreeSet(sessions));
        
        //try to pick among active sessions first (check that all active sessions are of the same initiative)
        String initiative = null;
        Session lastActive = null;
        for (Iterator it = sessions.iterator();it.hasNext();) {
            Session session = (Session)it.next();
            if (session.getStatusType()==null || !session.getStatusType().isActive()) continue;
            if (initiative==null) 
                initiative = session.getAcademicInitiative();
            else if (!initiative.equals(session.getAcademicInitiative()))
                return null; // multiple initiatives -> no default
            lastActive = session;
        }
        if (lastActive!=null) return lastActive; //return the last (most recent) active session
        
        //pick among all sessions (check that all sessions are of the same initiative)
		for (Iterator it = sessions.iterator();it.hasNext();) {
		    Session session = (Session)it.next();
		    if (initiative==null) 
		        initiative = session.getAcademicInitiative();
		    else if (!initiative.equals(session.getAcademicInitiative())) 
		        return null; // multiple initiatives -> no default
		}
		return (Session)orderedSession.last(); // return the last one, i.e., the most recent one
	}

	/**
	 * Gets the current user session
	 * 
	 * @param user
	 *            User object
	 * @return Session object of found, throws Exception otherwise
	 * @throws HibernateException
	 */
	public static Session getCurrentAcadSession(User user) throws Exception {
		Object sessionId = user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId == null || sessionId.toString().trim().length() == 0)
			throw new Exception(
					"Current Academic Session cannot be determined for user");
		else {
			Session s = Session.getSessionById(new Long(sessionId.toString()));
			return s;
		}
	}

	/**
	 * @return Returns the term.
	 */
	public String getTermLabel() {
		return this.getAcademicTerm();
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return getAcademicTerm() + " " + getYear() + 
		    " ("+(getAcademicInitiative().length()>9?getAcademicInitiative().substring(0,9):getAcademicInitiative())+")";
	}

	public String toString() {
		return this.getLabel();
	}

	/**
	 * @return Returns the year.
	 */
	public int getYear() {
		return Integer.parseInt(this.getAcademicYear());
	}

	/**
	 * @return Returns the year.
	 */
	public static int getYear(String acadYrTerm) {
		return Integer.parseInt(acadYrTerm.substring(0, 4));
	}

	/**
	 * @param bldgUniqueId
	 * @return
	 */
	public Building building(String bldgUniqueId) {
		// TODO make faster
		for (Iterator it = this.getBuildings().iterator(); it.hasNext();) {
			Building bldg = (Building) it.next();
			if (bldg.getExternalUniqueId().equals(bldgUniqueId)) {
				return bldg;
			}
		}
		return null;
	}

	public String academicInitiativeDisplayString() {
		return this.getAcademicInitiative();
	}

	public String statusDisplayString() {
		return getStatusType().getLabel();
	}

	/**
	 * @return Returns the sessionStatusMap.
	 */
	public static ReferenceList getSessionStatusList() {
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForSession());
		return ref;
	}

	/**
	 * Fetch rooms efficiently, joining features and roomDepts
	 * 
	 * @return
	 */
	public java.util.Set getRoomsFast(String[] depts) {
		if (depts != null && depts.length > 0) {
			return new TreeSet((new RoomDAO()).getSession().createQuery(
					"select room from Location as room "
							+ "left outer join room.roomDepts as roomDept "
							+ "where room.session.uniqueId = :sessionId"
							+ " and roomDept.department.deptCode  in ( "
							+ Constants.arrayToStr(depts, "'", ", ") + " ) ")
					.setLong("sessionId", getSessionId().longValue()).list());
		} else {
			return new TreeSet(
					(new RoomDAO())
							.getSession()
							.createQuery(
									"select room from Location as room where room.session.uniqueId = :sessionId")
							.setLong("sessionId", getSessionId().longValue())
							.list());
		}
	}

	public java.util.Set getRoomsFast(User user) throws Exception {
		if (user.getRole().equals(Roles.ADMIN_ROLE))
			return getRoomsFast((String[]) null);

		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		String mgrId = (String) user
				.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = (new TimetableManagerDAO()).get(new Long(
				mgrId));

		Set departments = manager.departmentsForSession(sessionId);
		if (departments != null) {
			String[] depts = new String[departments.size()];
			int idx = 0;
			for (Iterator i = departments.iterator(); i.hasNext();) {
				depts[idx++] = ((Department) i.next()).getDeptCode();
			}
			return getRoomsFast(depts);
		}

		return new TreeSet();
	}

	/**
	 * 
	 * @param depts
	 * @return
	 */
	public java.util.Set getBldgsFast(String[] depts) {
		if (depts != null && depts.length > 0) {
			List rooms = (new RoomDAO()).getSession().createQuery(
					"select room from Room as room "
							+ "left outer join room.roomDepts as roomDept "
							+ "where room.session.uniqueId = :sessionId"
							+ " and roomDept.department.deptCode  in ( "
							+ Constants.arrayToStr(depts, "'", ", ") + " ) ")
					.setInteger("sessionId", this.getSessionId().intValue())
					.list();
			TreeSet bldgs = new TreeSet();
			for (Iterator i = rooms.iterator(); i.hasNext();) {
				Room room = (Room) i.next();
				bldgs.add(room.getBuilding());
			}
			return bldgs;
		} else {
			return new TreeSet(
					(new BuildingDAO())
							.getSession()
							.createQuery(
									"select building from Building as building where building.session.uniqueId = :sessionId")
							.setInteger("sessionId", getSessionId().intValue())
							.list());
		}
	}

	/**
	 * Build the msf schema name for the academic year term provided
	 * 
	 * @param acadYrTerm
	 *            Academic Year Term (format yyyyTtt - 2005Fal)
	 * @return Msf Schema Name
	 */
	public static String getMsfSchemaName(String acadYr, String acadTerm) {
		String schema = "msf1";

		if (acadYr.length() != 4)
			return "";
		if (acadTerm.length() != 3)
			return "";

		int year = Integer.parseInt(acadYr);

		schema = schema + getLegacyYearTermCode(year, acadTerm);

		return schema;
	}

	/**
	 * Build the last like msf schema name for the academic year term provided
	 * 
	 * @param acadYrTerm
	 *            Academic Year Term (format yyyyTtt - 2005Fal)
	 * @return Last like Msf Schema Name
	 */
	public static String getLLMsfSchemaName(String acadYr, String acadTerm) {
		String schema = "msf1";
		if (acadYr.length() != 4)
			return "";
		if (acadTerm.length() != 3)
			return "";
		int year = Integer.parseInt(acadYr) - 1;
		schema = schema + getLegacyYearTermCode(year, acadTerm);

		return schema;
	}

	private static String getLegacyYearTermCode(int year, String term) {
		String termCode = "";

		if (year >= 2000) {
			year = year - 2000;
		} else {
			year = year - 1900;
		}

		if (year < 10)
			termCode = termCode + "0";

		termCode = termCode + year;

		if (term.equals("Fal"))
			termCode = termCode + "0";
		if (term.equals("Spr"))
			termCode = termCode + "1";
		if (term.equals("Sum"))
			termCode = termCode + "2";

		return termCode;

	}

	/**
	 * Build the legacy year term code to represent this session
	 * 
	 * @return Legacy year term code (e.g. 061)
	 */
	public String getLegacyYearTermCode() {
		return (getLegacyYearTermCode(getYear(), getAcademicTerm()));
	}

	/**
	 * Populate last-like course mapping table (LL_COURSE_MAPPING)
	 * 
	 * @throws Exception
	 */
	public void createLastLikeCourseMapping() throws Exception {
		String currMsf = getMsfSchemaName(getAcademicYear(), getAcademicTerm());
		String llMsf = getLLMsfSchemaName(getAcademicYear(), getAcademicTerm());

		Debug.debug("Curr Msf: " + currMsf);
		Debug.debug("LL Msf: " + llMsf);

		// Check if schemas are valid
		if (currMsf.length() != 7 || llMsf.length() != 7) {
			throw new Exception("Error in determining MSF Schema names");
		}

		SessionDAO sdao = new SessionDAO();

		org.hibernate.Session hibSession = null;
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			Debug.debug("Building course info ...");

			hibSession = sdao.getSession();
			conn = hibSession.connection();

			// truncate mapping table
			stmt = conn.prepareStatement("delete from " + mappingTable);
			int ct = stmt.executeUpdate();
			stmt.close();

			// Add courses with a future course
			stmt = conn
					.prepareStatement("insert into "
							+ mappingTable
							+ " SELECT course.course, new_course.permanent_id perm_id, t.uniqueid, new_subject.abbreviation, new_course.COURSE_NUMBER course_nbr "
							+ "FROM ADMIN.COURSE@siq old_course, "
							+ "ADMIN.ACAD_INITIATIVE_SBJ_AREA@siq old_subj_area, "
							+ "ADMIN.SUBJECT_AREA@siq old_subject, "
							+ llMsf
							+ ".course, "
							+ llMsf
							+ ".this_term, "
							+ currMsf
							+ ".this_term new_this_term, "
							+ "timetable.subject_area t, "
							+ "admin.course@siq new_course, "
							+ "admin.acad_initiative_sbj_area@siq new_subj_area, "
							+ "admin.subject_area@siq new_subject "
							+ "WHERE "
							+ "old_subject.abbreviation=rtrim(substr(course.course,1,4)) "
							+ "AND old_course.course_number=rtrim(substr(course.course,5,8)) "
							+ "and (old_course.SUBJECT_AREA_UNIQUEID=old_subj_area.UNIQUEID  "
							+ "AND old_subj_area.SUBJECT_AREA_UNIQUEID=old_subject.UNIQUEID) "
							+ "AND (old_subj_area.ACADEMIC_INITIATIVE=?) "
							+ "AND old_course.first_effective_date<=this_term.begin_date "
							+ "AND (old_course.last_effective_date IS NULL OR old_course.last_effective_date>=this_term.begin_date) "
							+ "AND old_subj_area.first_effective_date<=this_term.begin_date "
							+ "AND (old_subj_area.last_effective_date IS NULL OR old_subj_area.last_effective_date>=this_term.begin_date) "
							+ "and old_course.permanent_id = new_course.permanent_id "
							+ "and (new_course.SUBJECT_AREA_UNIQUEID=new_subj_area.UNIQUEID  "
							+ "AND new_subj_area.SUBJECT_AREA_UNIQUEID=new_subject.UNIQUEID) "
							+ "AND new_course.first_effective_date<=new_this_term.begin_date "
							+ "AND (new_course.last_effective_date IS NULL OR new_course.last_effective_date>=new_this_term.begin_date) "
							+ "and t.sis_uniqueid = new_subj_area.uniqueid "
							+ "and t.session_id = ? ");
			stmt.setString(1, this.getAcademicInitiative());
			stmt.setInt(2, this.getSessionId().intValue());
			ct = stmt.executeUpdate();
			stmt.close();
			Debug.debug(ct + " Courses added with a future course");

			// Add courses where subject area has a successor
			stmt = conn
					.prepareStatement("insert into "
							+ mappingTable
							+ " SELECT course.course, old_course.permanent_id perm_id, t.uniqueid, new_subject.abbreviation, old_course.COURSE_NUMBER course_nbr "
							+ "FROM ADMIN.COURSE@siq old_course, "
							+ "ADMIN.ACAD_INITIATIVE_SBJ_AREA@siq old_subj_area, "
							+ "ADMIN.SUBJECT_AREA@siq old_subject, "
							+ llMsf
							+ ".course, "
							+ llMsf
							+ ".this_term, "
							+ "timetable.subject_area t, "
							+ "admin.acad_int_sbj_area_scsr@siq sdr, "
							+ "admin.acad_initiative_sbj_area@siq new_subj_area, "
							+ "admin.subject_area@siq new_subject "
							+ "WHERE "
							+ "not exists (select 1 from "
							+ mappingTable
							+ " llcm where course.course = llcm.course) "
							+ "and old_subject.abbreviation=rtrim(substr(course.course,1,4)) "
							+ "AND old_course.course_number=rtrim(substr(course.course,5,8)) "
							+ "and (old_course.SUBJECT_AREA_UNIQUEID=old_subj_area.UNIQUEID "
							+ "AND old_subj_area.SUBJECT_AREA_UNIQUEID=old_subject.UNIQUEID) "
							+ "AND (old_subj_area.ACADEMIC_INITIATIVE=?) "
							+ "AND old_course.first_effective_date<=this_term.begin_date "
							+ "AND (old_course.last_effective_date IS NULL OR old_course.last_effective_date>=this_term.begin_date) "
							+ "AND old_subj_area.first_effective_date<=this_term.begin_date "
							+ "AND (old_subj_area.last_effective_date IS NULL OR old_subj_area.last_effective_date>=this_term.begin_date) "
							+ "AND new_subj_area.SUBJECT_AREA_UNIQUEID=new_subject.UNIQUEID "
							+ "and t.sis_uniqueid = new_subj_area.uniqueid "
							+ "and t.session_id = ? "
							+ "and sdr.parentid  = old_subj_area.uniqueid "
							+ "and sdr.value = new_subj_area.uniqueid ");
			stmt.setString(1, this.getAcademicInitiative());
			stmt.setInt(2, this.getSessionId().intValue());
			ct = stmt.executeUpdate();
			stmt.close();
			Debug.debug(ct
					+ " Courses added where subject area has a successor");

			// Add other courses - where current subject is still valid
			stmt = conn
					.prepareStatement("insert into "
							+ mappingTable
							+ " SELECT "
							+ "course.course, old_course.permanent_id perm_id, t.uniqueid, old_subject.abbreviation, old_course.COURSE_NUMBER course_nbr "
							+ "FROM ADMIN.COURSE@siq old_course, "
							+ "ADMIN.ACAD_INITIATIVE_SBJ_AREA@siq old_subj_area, "
							+ "ADMIN.SUBJECT_AREA@siq old_subject, "
							+ llMsf
							+ ".course, "
							+ llMsf
							+ ".this_term, "
							+ "timetable.subject_area t "
							+ "WHERE "
							+ "not exists (select 1 from "
							+ mappingTable
							+ " llcm where course.course = llcm.course) "
							+ "and old_subject.abbreviation=rtrim(substr(course.course,1,4)) "
							+ "AND old_course.course_number=rtrim(substr(course.course,5,8)) "
							+ "and (old_course.SUBJECT_AREA_UNIQUEID=old_subj_area.UNIQUEID "
							+ "AND old_subj_area.SUBJECT_AREA_UNIQUEID=old_subject.UNIQUEID) "
							+ "AND (old_subj_area.ACADEMIC_INITIATIVE=?) "
							+ "AND old_course.first_effective_date<=this_term.begin_date "
							+ "AND (old_course.last_effective_date IS NULL OR old_course.last_effective_date>=this_term.begin_date) "
							+ "AND old_subj_area.first_effective_date<=this_term.begin_date "
							+ "AND (old_subj_area.last_effective_date IS NULL OR old_subj_area.last_effective_date>=this_term.begin_date) "
							+ "and t.sis_uniqueid = old_subj_area.uniqueid "
							+ "and t.session_id = ?");
			stmt.setString(1, this.getAcademicInitiative());
			stmt.setInt(2, this.getSessionId().intValue());
			ct = stmt.executeUpdate();
			stmt.close();
			Debug.debug(ct + " Other courses added");

		} catch (Exception e) {
			Debug.error(e);
			if (stmt != null)
				stmt.close();
			// if (hibSession!=null && hibSession.isOpen()) hibSession.close();
			throw (e);
		}

	}

	public static Session getSessionUsingInitiativeYearTerm(
			String academicInitiative, String academicYear, String academicTerm) {
		Session s = null;
		StringBuffer queryString = new StringBuffer();
		SessionDAO sdao = new SessionDAO();

		queryString.append(" from Session as s where s.academicInitiative = '"
				+ academicInitiative + "' ");
		queryString.append(" and s.academicYear = '" + academicYear + "' ");
		queryString.append(" and s.academicTerm = '" + academicTerm + "' ");

		Query q = sdao.getQuery(queryString.toString());
		if (q.list().size() == 1) {
			s = (Session) q.list().iterator().next();
		}
		return (s);

	}

	public Session getLastLikeSession() {
		String lastYr = new Integer(this.getYear() - 1).toString();
		return getSessionUsingInitiativeYearTerm(this.getAcademicInitiative(),
				lastYr, getAcademicTerm());
	}

	public Session getNextLikeSession() {
		String nextYr = new Integer(this.getYear() + 1).toString();
		return getSessionUsingInitiativeYearTerm(this.getAcademicInitiative(),
				nextYr, getAcademicTerm());
	}

	public String loadInstrAndCrsOffering() throws Exception {
		return ("done");
	}

	private String getInsertCrsOffering(String llMsf, int control)
			throws Exception {
		String sql = "INSERT INTO timetable.course_offering "
				+ "(UNIQUEID, SUBJECT_AREA_ID, COURSE_NBR, PERM_ID, INSTR_OFFR_ID, IS_CONTROL, PROJ_DEMAND) "
				+ "	SELECT "
				+ "    	   timetable.crs_offr_seq.nextval, "
				+ "	       subject_id, course_nbr, perm_id, "
				+ "    	   timetable.instr_offr_seq.currval, "
				+ control
				+ ", proj_demand"
				+ "   FROM ( "
				+ "	SELECT "
				+ "	       llcm.course_nbr, llcm.perm_id,"
				+ "    	   nvl(SUM(crscurr.requests), 0) proj_demand, llcm.subject_id "
				+ "  	  FROM "
				+ mappingTable
				+ " llcm, "
				+ llMsf
				+ ".crscurr, "
				+ mappingTable
				+ " llcm_related"
				+ "  	 WHERE llcm.course=?"
				+ "      AND llcm_related.subject_id  = llcm.subject_id AND llcm_related.course_nbr = llcm.course_nbr"
				+ "  	   AND llcm_related.course = crscurr.course(+)"
				+ "  	 GROUP BY llcm.course_nbr, llcm.perm_id, llcm.subject_id ) xx "
				+ " where not exists (select 1 from timetable.course_offering yy where xx.subject_id = yy.subject_area_id and xx.course_nbr = yy.course_nbr)";

		return sql;

	}

	public Long getSessionId() {
		return (this.getUniqueId());
	}

	public void setSessionId(Long sessionId) {
		this.setUniqueId(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.unitime.timetable.model.PreferenceGroup#canUserEdit(org.unitime.commons.User)
	 */
	protected boolean canUserEdit(User user) {
		return (false);
	}

	protected boolean canUserView(User user) {
		return (false);
	}

	public String htmlLabel() {
		return (this.getLabel());
	}

	public int getStartMonth() {		
		return DateUtils.getStartMonth(getSessionBeginDateTime(), getYear(), sNrExcessDays);
	}

	public int getEndMonth() {
		return DateUtils.getEndMonth(getSessionEndDateTime(), getYear(), sNrExcessDays);
	}

	public int getDayOfYear(int day, int month) {
		return DateUtils.getDayOfYear(day, month, getYear());
	}
	
	public int getNrDaysOfMonth(int month) {
		return DateUtils.getNrDaysOfMonth(month, getYear());
	}
	
	public int getHoliday(int day, int month) {
		return getHoliday(day, month, getYear(), getStartMonth(), getHolidays());
	}
	
	public static int getHoliday(int day, int month, int year, int startMonth, String holidays) {
		try {
			if (holidays == null)
				return sHolidayTypeNone;
			int idx = DateUtils.getDayOfYear(day, month, year)
					- DateUtils.getDayOfYear(1, startMonth, year);
			if (idx < 0 || idx >= holidays.length())
				return sHolidayTypeNone;
			return (int) (holidays.charAt(idx) - '0');
		} catch (IndexOutOfBoundsException e) {
			return sHolidayTypeNone;
		}
	}

	public String getHolidaysHtml() {
		return getHolidaysHtml(true);
	}

	public String getHolidaysHtml(boolean editable) {
		return getHolidaysHtml(getSessionBeginDateTime(), getSessionEndDateTime(), getClassesEndDateTime(), getExamBeginDate(),  getYear(), getHolidays(), editable);
	}

	public static String getHolidaysHtml(
			Date sessionBeginTime, 
			Date sessionEndTime, 
			Date classesEndTime,
			Date examBeginTime,
			int acadYear, 
			String holidays,
			boolean editable) {
		
		StringBuffer prefTable = new StringBuffer();
		StringBuffer prefNames = new StringBuffer();
		StringBuffer prefColors = new StringBuffer();

		for (int i = 0; i < sHolidayTypeNames.length; i++) {
			prefTable.append((i == 0 ? "" : ",") + "'" + i + "'");
			prefNames.append((i == 0 ? "" : ",") + "'" + sHolidayTypeNames[i]
					+ "'");
			prefColors.append((i == 0 ? "" : ",") + "'" + sHolidayTypeColors[i]
					+ "'");
		}

		StringBuffer holidayArray = new StringBuffer();
		StringBuffer borderArray = new StringBuffer();
		
		Calendar sessionBeginDate = Calendar.getInstance(Locale.US);
		sessionBeginDate.setTime(sessionBeginTime);
		
		Calendar sessionEndDate = Calendar.getInstance(Locale.US);
		sessionEndDate.setTime(sessionEndTime);
		
		Calendar classesEndDate = Calendar.getInstance(Locale.US);
		classesEndDate.setTime(classesEndTime);

        Calendar examBeginDate = Calendar.getInstance(Locale.US);
        examBeginDate.setTime(examBeginTime);

        int startMonth = DateUtils.getStartMonth(sessionBeginTime, acadYear, sNrExcessDays);
		int endMonth = DateUtils.getEndMonth(sessionEndTime, acadYear, sNrExcessDays);
		
		for (int m = startMonth; m <= endMonth; m++) {
			if (m != startMonth) {
				holidayArray.append(",");
				borderArray.append(",");
			}
			holidayArray.append("[");
			borderArray.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, acadYear);
			for (int d = 1; d <= daysOfMonth; d++) {
				if (d > 1) {
					holidayArray.append(",");
					borderArray.append(",");
				}
				holidayArray.append("'" + getHoliday(d, m, acadYear, startMonth, holidays) + "'");
				if (d == sessionBeginDate.get(Calendar.DAY_OF_MONTH)
						&& m == sessionBeginDate.get(Calendar.MONTH))
					borderArray.append("'#660000 2px solid'");
				else if (d == sessionEndDate.get(Calendar.DAY_OF_MONTH)
						&& m == sessionEndDate.get(Calendar.MONTH))
					borderArray.append("'#333399 2px solid'");
				else if (d == classesEndDate.get(Calendar.DAY_OF_MONTH)
						&& m == classesEndDate.get(Calendar.MONTH))
					borderArray.append("'#339933 2px solid'");
                else if (d == examBeginDate.get(Calendar.DAY_OF_MONTH)
                        && m == examBeginDate.get(Calendar.MONTH))
                    borderArray.append("'#999933 2px solid'");
				else
					borderArray.append("null");
			}
			holidayArray.append("]");
			borderArray.append("]");
		}

		StringBuffer table = new StringBuffer();
		table
				.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>");
		table.append("<script language='JavaScript'>");
		table.append("calGenerate(" + acadYear + "," + startMonth + ","
				+ endMonth + "," + "[" + holidayArray + "]," + "[" + prefTable
				+ "]," + "[" + prefNames + "]," + "[" + prefColors + "]," + "'"
				+ sHolidayTypeNone + "'," + "[" + borderArray + "]," + editable
				+ "," + editable + ");");
		table.append("</script>");
		return table.toString();
	}

	public void setHolidays(String holidays) {
		super.setHolidays(holidays);
	}

	public void setHolidays(HttpServletRequest request) {
		int startMonth = getStartMonth();
		int endMonth = getEndMonth();
		StringBuffer sb = new StringBuffer();
		for (int m = startMonth; m <= endMonth; m++) {
			int daysOfMonth = getNrDaysOfMonth(m);
			for (int d = 1; d <= daysOfMonth; d++) {
				String holiday = request.getParameter("cal_val_"
						+ ((12 + m) % 12) + "_" + d);
				sb.append(holiday == null ? String.valueOf(sHolidayTypeNone)
						: holiday);
			}
		}
		setHolidays(sb.toString());
	}

	public int compareTo(Object o) {
		if (o == null || !(o instanceof Session)) return -1;
		Session s = (Session) o;
		
		int cmp = getAcademicInitiative().compareTo(s.getAcademicInitiative());
		if (cmp!=0) return cmp;
		
		cmp = getSessionBeginDateTime().compareTo(s.getSessionBeginDateTime());
		if (cmp!=0) return cmp;
		
		return getUniqueId().compareTo(s.getUniqueId());
	}

	public DatePattern getDefaultDatePatternNotNull() {
		DatePattern dp = super.getDefaultDatePattern();
		if (dp == null) {
			dp = DatePattern.findByName(this, "Full Term");
		}
		return dp;
	}

	public int getNrWeeks() {
		Calendar sessionBeginDate = Calendar.getInstance(Locale.US);
		sessionBeginDate.setTime(getSessionBeginDateTime());
		Calendar sessionEndDate = Calendar.getInstance(Locale.US);
		sessionEndDate.setTime(getSessionEndDateTime());
		int beginDay = getDayOfYear(
				sessionBeginDate.get(Calendar.DAY_OF_MONTH), sessionBeginDate
						.get(Calendar.MONTH))
				- getDayOfYear(1, getStartMonth());
		int endDay = getDayOfYear(sessionEndDate.get(Calendar.DAY_OF_MONTH),
				sessionEndDate.get(Calendar.MONTH))
				- getDayOfYear(1, getStartMonth());

		int nrDays = 0;
		for (int i = beginDay; i <= endDay; i++) {
			if (getHolidays() == null || i >= getHolidays().length()
					|| (getHolidays().charAt(i) - '0') == sHolidayTypeNone)
				nrDays++;
		}
		nrDays -= 7;

		return (6 + nrDays) / 7;
	}

}
