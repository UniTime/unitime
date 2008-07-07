/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseSession;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.ExamDAO;
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
        if (Roles.EXAM_MGR_ROLE.equals(role.getRole().getReference()) && sessions.isEmpty())
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
		if (getSessionBeginDateTime()!=null) {
			Calendar c = Calendar.getInstance(Locale.US);
			c.setTime(getSessionBeginDateTime());
			return c.get(Calendar.YEAR);
		}
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
	
	public int getExamBeginOffset() {
	    return (int)Math.round((getSessionBeginDateTime().getTime() - getExamBeginDate().getTime()) / 86.4e6); 
	}
	
	public String getBorder(int day, int month) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSessionBeginDateTime());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && ((12+month)%12)==cal.get(Calendar.MONTH))
			return "'blue 2px solid'";
		cal.setTime(getSessionEndDateTime());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && ((12+month)%12)==cal.get(Calendar.MONTH))
			return "'blue 2px solid'";
		cal.setTime(getExamBeginDate());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && ((12+month)%12)==cal.get(Calendar.MONTH))
			return "'green 2px solid'";
		int holiday = getHoliday(day, month);
		if (holiday!=Session.sHolidayTypeNone)
			return "'"+Session.sHolidayTypeColors[holiday]+" 2px solid'";
		return "null";
	}
	
	/** Return distance of the given date outside the session start/end date (in milliseconds) */
	public long getDistance(Date date) {
		if (date.compareTo(getSessionBeginDateTime())<0) //before session 
			return getSessionBeginDateTime().getTime() - date.getTime();
		if (date.compareTo(getSessionEndDateTime())>0) //after session
			return date.getTime() - getSessionEndDateTime().getTime();
		return 0; //inside session
	}
	
	public boolean hasStudentSchedule() {
        return ((Number)new ExamDAO().getSession().
                createQuery("select count(x) from StudentClassEnrollment x " +
                        "where x.student.session.uniqueId=:sessionId").
                setLong("sessionId",getUniqueId()).uniqueResult()).longValue()>0;
	}

}
