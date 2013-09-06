/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseChangeLog;
import org.unitime.timetable.model.dao.ChangeLogDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.util.Formats;



public class ChangeLog extends BaseChangeLog implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ChangeLog () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ChangeLog (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
    
    public static enum Operation {
        CREATE("create"),
        UPDATE("update"),
        DELETE("delete"),
        CLEAR_PREF("clear preferences"),
        CLEAR_ALL_PREF("clear class preferences"),
        ASSIGN("assignment"),
        UNASSIGN("unassignment"),
        APPROVE("approve"),
        REJECT("reject"),
        MERGE("merge"),
        NOTE("note"),
        ;
        
        private String iTitle;
        Operation(String title) { iTitle = title; }
        public String getTitle() { return iTitle; }
    }
    
    public static enum Source {
        CLASS_EDIT("Class Edit"),
        SCHEDULING_SUBPART_EDIT("Scheduling Subpart Edit"),
        INSTR_CFG_EDIT("Configuration Edit"),
        CROSS_LIST("Cross Lists"),
        MAKE_OFFERED("Make Offered"),
        MAKE_NOT_OFFERED("Make Not Offered"),
        RESERVATION("Reservations"),
        COURSE_OFFERING_EDIT("Course Offering Edit"),
        CLASS_SETUP("Class Setup"),
        CLASS_INSTR_ASSIGN("Assign Instructors"),
        DIST_PREF_EDIT("Distribution Preferences"),
        DESIGNATOR_EDIT("Designator Edit"),
        INSTRUCTOR_EDIT("Instructor Edit"),
        INSTRUCTOR_PREF_EDIT("Instructor Preferences"),
        INSTRUCTOR_MANAGE("Manage Instructors"),
        ROOM_DEPT_EDIT("Room Availability"),
        ROOM_FEATURE_EDIT("Room Feature Edit"),
        ROOM_GROUP_EDIT("Room Group Edit"),
        ROOM_EDIT("Room Edit"),
        ROOM_PREF_EDIT("Room Preference Edit"),
        DEPARTMENT_EDIT("Department Edit"),
        SESSION_EDIT("Session Edit"),
        SOLVER_GROUP_EDIT("Solver Group Edit"),
        TIME_PATTERN_EDIT("Time Pattern Edit"),
        DATE_PATTERN_EDIT("Date Pattern Edit"),
        DIST_TYPE_EDIT("Distribution Type Edit"),
        MANAGER_EDIT("Timetabling Manager Edit"),
        SUBJECT_AREA_EDIT("Subject Area Edit"),
        BUILDING_EDIT("Building Edit"),
        EXAM_PERIOD_EDIT("Examination Period Edit"),
        EXAM_EDIT("Examination Edit"),
        DATA_IMPORT_OFFERINGS("Data Import: Offerings"),
        DATA_IMPORT_STUDENT_ENROLLMENTS("Data Import: Students"),
        DATA_IMPORT_SUBJECT_AREAS("Data Import: Subjects"),
        DATA_IMPORT_DEPARTMENTS("Data Import: Departments"),
        DATA_IMPORT_EXT_BUILDING_ROOM("Data Import: Rooms"),
        DATA_IMPORT_STAFF("Data Import: Staff"),
        EXAM_INFO("Examination Assignment"),
        EXAM_SOLVER("Examination Solver"),
        EVENT_EDIT("Event Edit"),
        DATA_IMPORT_EVENTS("Data Import: Events"),
        DATA_IMPORT_LASTLIKE_DEMAND("Data Import: Demands"),
        CLASS_INFO("Class Assignment"),
        DATA_IMPORT_CURRICULA("Data Import: Curricula"),
        CURRICULUM_EDIT("Curriculum Edit"),
        CUR_CLASF_EDIT("Curriculum Requested Enrollments"),
        CUR_PROJ_RULES("Course Projection Rules"),
        CURRICULA("Curricula"),
        SIMPLE_EDIT("Configuration"),
        DATA_IMPORT_RESERVATIONS("Data Import: Reservations"),
        OFFERING_DETAIL("Instructional Offering Detail"),
        ;
        
        private String iTitle;
        Source(String title) { iTitle = title; }
        public String getTitle() { return iTitle; }
    }
    
    public static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
    public static Formats.Format<Date> sDFdate = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
    
    public Operation getOperation() {
        return Operation.valueOf(getOperationString());
    }
    
    public void setOperation(Operation operation) {
        setOperationString(operation.name());
    }
    
    public Source getSource() {
        return Source.valueOf(getSourceString());
    }
    
    public void setSource(Source source) {
        setSourceString(source.name());
    }
    
    public static void addChange(
    		org.hibernate.Session hibSession,
            SessionContext context,
            Object object,
            String objectTitle,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
        try {
        	if (!context.isAuthenticated()) {
        		Debug.warning("Unable to add change log -- no user.");
                return;
        	}
            String userId = null;
        	if (context.getUser() instanceof UserContext.Chameleon) {
        		userId = ((UserContext.Chameleon)context.getUser()).getOriginalUserContext().getExternalUserId();
        	}
        	if (userId == null)
        		userId = context.getUser().getExternalUserId();
        	Session session = (context.getUser().getCurrentAuthority() == null ? null : SessionDAO.getInstance().get((Long)context.getUser().getCurrentAuthority().getAcademicSession().getQualifierId()));
            if (session == null) {
                Debug.warning("Unable to add change log -- no academic session.");
                return;
            }
            TimetableManager manager = TimetableManager.findByExternalId(userId);
            if (manager == null) {
                Debug.warning("Unable to add change log -- no timetabling manager.");
                return;
            }
            addChange(hibSession, manager, session, object, objectTitle, source, operation, subjArea, dept);
        } catch (Exception e) {
            Debug.error(e);
        }
    }
        
    public static void addChange(
            org.hibernate.Session hibSession,
            TimetableManager manager,
            Session session,
            Object object,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
    	addChange(hibSession, manager, session, object, null, source, operation, subjArea, dept);
    }
    
    public static void addChange(
            org.hibernate.Session hibSession,
            SessionContext contect,
            Object object,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
        addChange(hibSession, contect, object, null, source, operation, subjArea, dept);
    }

    
    public static void addChange(
            org.hibernate.Session hibSession,
            TimetableManager manager,
            Session session,
            Object object,
            String objectTitle,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
        try {
            if (session==null) {
                Debug.warning("Unable to add change log -- no academic session.");
                return;
            }
            if (manager==null) {
                Debug.warning("Unable to add change log -- no timetabling manager.");
                return;
            }
            Number objectUniqueId = (Number)object.getClass().getMethod("getUniqueId", new Class[]{}).invoke(object, new Object[]{});
            String objectType = object.getClass().getName();
            if (object instanceof Event) objectType = Event.class.getName();
            if (objectType.indexOf("$$")>=0)
                objectType = objectType.substring(0,objectType.indexOf("$$"));
            if (objectTitle==null || objectTitle.length()==0) {
                try {
                    objectTitle = (String)object.getClass().getMethod("getTitle",new Class[]{}).invoke(object, new Object[]{});
                } catch (Exception e) {}
                if (objectTitle==null || objectTitle.length()==0)
                    objectTitle = object.toString();
                if (object instanceof CourseOffering)
                    objectTitle = ((CourseOffering)object).getCourseName();
            }
            
            ChangeLog chl = new ChangeLog();
            chl.setSession(session);
            chl.setManager(manager);
            chl.setTimeStamp(new Date());
            chl.setObjectType(objectType);
            chl.setObjectUniqueId(new Long(objectUniqueId.longValue()));
            chl.setObjectTitle(objectTitle==null || objectTitle.length()==0?"N/A":(objectTitle.length() <= 255?objectTitle:objectTitle.substring(0,255)));
            chl.setSubjectArea(subjArea);
            if (dept==null && subjArea!=null) dept = subjArea.getDepartment();
            chl.setDepartment(dept);
            chl.setSource(source);
            chl.setOperation(operation);
            if (hibSession!=null)
                hibSession.saveOrUpdate(chl);
            else
                new ChangeLogDAO().saveOrUpdate(chl); 
            
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    /*
    public static String getMessage(ServletRequest request, String message) {
        MessageResources rsc = (MessageResources)request.getAttribute(Globals.MESSAGES_KEY);
        if (rsc==null) return message;
        String ret = rsc.getMessage(message);
        return (ret==null?message:ret);
    }
    */
            
    public String getOperationTitle() {
    	return getOperation().getTitle();
        // return getMessage(request, "changelog.operation."+getOperationString());
    }

    public String getSourceTitle() {
    	return getSource().getTitle();
    	//return getMessage(request, "changelog.source."+getSourceString());
    }
    
    public String getLabel() {
        return 
        "Last " + getOperationTitle() +
        " of " + getObjectTitle() +
        " was made by " + getManager().getShortName() +
        " at " + sDF.format(getTimeStamp()); 
    }

    public String getShortLabel() {
        return 
            "Last " + getOperationTitle() +
            " was made by " + getManager().getShortName() +
            " at " + sDF.format(getTimeStamp()); 
    }
    
    public String toString() { return getLabel(); }
    
    public int compareTo(Object obj) {
        if (obj==null || !(obj instanceof ChangeLog)) return -1;
        ChangeLog chl = (ChangeLog)obj;
        int cmp = getTimeStamp().compareTo(chl.getTimeStamp());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(chl.getUniqueId() == null ? -1 : chl.getUniqueId());
    }
    
    public static ChangeLog findLastChange(Object object) {
        return findLastChange(object, null);
    }

    public static ChangeLog findLastChange(Object object, Source source) {
        try {
            Number objectUniqueId = (Number)object.getClass().getMethod("getUniqueId", new Class[]{}).invoke(object, new Object[]{});
            String objectType = object.getClass().getName();
            return findLastChange(objectType, objectUniqueId, source);
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }
    
    public static ChangeLog findLastChange(String objectType, Number objectUniqueId) {
        return findLastChange(objectType, objectUniqueId, null);
    }
    
    public static ChangeLog findLastChange(String objectType, Number objectUniqueId, Source source) {
        try {
            org.hibernate.Session hibSession = new ChangeLogDAO().getSession(); 
            Query q = hibSession.createQuery(
                        "select ch from ChangeLog ch " +
                        "where ch.objectUniqueId=:objectUniqueId and ch.objectType=:objectType "+
                        (source==null?"":"and ch.sourceString=:source ") +
                        "and ch.operationString != :note " +
                        "order by ch.timeStamp desc");
            q.setLong("objectUniqueId", objectUniqueId.longValue());
            q.setString("objectType",objectType);
            q.setString("note", Operation.NOTE.toString());
            if (source!=null)
                q.setString("source",source.name());
            q.setMaxResults(1);
            q.setCacheable(true);
            List logs = q.list();
            return (logs.isEmpty()?null:(ChangeLog)logs.get(0));
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }
    
    public static ChangeLog findLastChange(String objectType, Collection objectUniqueIds, Source source) {
        try {
            org.hibernate.Session hibSession = new ChangeLogDAO().getSession();
            ChangeLog changeLog = null;

            StringBuffer ids = new StringBuffer(); int idx = 0;
            for (Iterator i=objectUniqueIds.iterator();i.hasNext();idx++) {
                ids.append(i.next()); idx++;
                if (idx==100) {
                    Query q = hibSession.createQuery(
                            "select ch from ChangeLog ch " +
                            "where ch.objectUniqueId in ("+ids+") and ch.objectType=:objectType "+
                            (source==null?"":"and ch.sourceString=:source ") +
                            "order by ch.timeStamp desc");
                    q.setString("objectType",objectType);
                    if (source!=null) q.setString("source",source.name());
                    q.setMaxResults(1);
                    List logs = q.list();
                    if (!logs.isEmpty()) {
                        ChangeLog cl = (ChangeLog)logs.get(0);
                        if (changeLog==null || changeLog.compareTo(cl)>0)
                            changeLog = cl;
                    }
                    ids = new StringBuffer();
                    idx = 0;
                } else if (i.hasNext()) {
                    ids.append(",");
                }
            }
            
            if (idx>0) {
                Query q = hibSession.createQuery(
                        "select ch from ChangeLog ch " +
                        "where ch.objectUniqueId in ("+ids+") and ch.objectType=:objectType "+
                        (source==null?"":"and ch.sourceString=:source ") +
                        "order by ch.timeStamp desc");
                q.setString("objectType",objectType);
                if (source!=null) q.setString("source",source.name());
                q.setMaxResults(1);
                q.setCacheable(true);
                List logs = q.list();
                if (!logs.isEmpty()) {
                    ChangeLog cl = (ChangeLog)logs.get(0);
                    if (changeLog==null || changeLog.compareTo(cl)>0)
                        changeLog = cl;
                }
            }
            
            return changeLog;
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    public static List findLastNChanges(Object object, int n) {
        return findLastNChanges(object, null, n);
    }
    
    public static List findLastNChanges(Object object, Source source, int n) {
        try {
            Number objectUniqueId = (Number)object.getClass().getMethod("getUniqueId", new Class[]{}).invoke(object, new Object[]{});
            String objectType = object.getClass().getName();
            org.hibernate.Session hibSession = new ChangeLogDAO().getSession(); 
            Query q = hibSession.createQuery(
                        "select ch from ChangeLog ch " +
                        "where ch.objectUniqueId=:objectUniqueId and ch.objectType=:objectType "+
                        (source==null?"":"and ch.sourceString=:source ") +
                        "order by ch.timeStamp desc");
            q.setLong("objectUniqueId", objectUniqueId.longValue());
            q.setString("objectType",objectType);
            if (source!=null)
                q.setString("source",source.name());
            q.setMaxResults(n);
            q.setCacheable(true);
            return q.list();
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    public static List findLastNChanges(Long sessionId, Long managerId, Long subjAreaId, Long departmentId, int n) {
        try {
            org.hibernate.Session hibSession = new ChangeLogDAO().getSession(); 
            Query q = hibSession.createQuery(
                        "select ch from ChangeLog ch where " +
                        "ch.session.uniqueId=:sessionId " +
                        (managerId==null?"":"and ch.manager.uniqueId=:managerId ") +
                        (subjAreaId==null?"":"and ch.subjectArea.uniqueId=:subjAreaId ") +
                        (departmentId==null?"":"and ch.department.uniqueId=:departmentId ") + 
                        "order by ch.timeStamp desc");
            q.setLong("sessionId", sessionId.longValue());
            if (managerId!=null) q.setLong("managerId",managerId.longValue());
            if (subjAreaId!=null) q.setLong("subjAreaId",subjAreaId.longValue());
            if (departmentId!=null) q.setLong("departmentId",departmentId.longValue());
            q.setMaxResults(n);
            q.setCacheable(true);
            return q.list();
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }
}
