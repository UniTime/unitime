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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.base.BaseChangeLog;
import org.unitime.timetable.model.dao.ChangeLogDAO;




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

	/**
	 * Constructor for required fields
	 */
	public ChangeLog (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		org.unitime.timetable.model.TimetableManager manager,
		java.util.Date timeStamp,
		java.lang.String objectType,
		java.lang.String objectTitle,
		java.lang.Long objectUniqueId,
		java.lang.String sourceString,
		java.lang.String operationString) {

		super (
			uniqueId,
			session,
			manager,
			timeStamp,
			objectType,
			objectTitle,
			objectUniqueId,
			sourceString,
			operationString);
	}

/*[CONSTRUCTOR MARKER END]*/
    
    public static enum Operation {
        CREATE,
        UPDATE,
        DELETE,
        CLEAR_PREF,
        CLEAR_ALL_PREF
    }
    
    public static enum Source {
        CLASS_EDIT,
        SCHEDULING_SUBPART_EDIT,
        INSTR_CFG_EDIT,
        CROSS_LIST,
        MAKE_OFFERED,
        MAKE_NOT_OFFERED,
        RESERVATION,
        COURSE_OFFERING_EDIT,
        CLASS_SETUP,
        CLASS_INSTR_ASSIGN,
        DIST_PREF_EDIT,
        DESIGNATOR_EDIT,
        INSTRUCTOR_EDIT,
        INSTRUCTOR_PREF_EDIT,
        INSTRUCTOR_MANAGE,
        ROOM_DEPT_EDIT,
        ROOM_FEATURE_EDIT,
        ROOM_GROUP_EDIT,
        ROOM_EDIT,
        ROOM_PREF_EDIT,
        DEPARTMENT_EDIT,
        SESSION_EDIT,
        SOLVER_GROUP_EDIT,
        TIME_PATTERN_EDIT,
        DATE_PATTERN_EDIT,
        DIST_TYPE_EDIT,
        MANAGER_EDIT,
        SUBJECT_AREA_EDIT,
        BUILDING_EDIT
    }
    
    public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    public static SimpleDateFormat sDFdate = new SimpleDateFormat("MM/dd/yy");
    
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
            HttpServletRequest request,
            Object object,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
        addChange(
                hibSession,
                request,
                object,
                null,
                source,
                operation,
                subjArea,
                dept);
    }

        public static void addChange(
            org.hibernate.Session hibSession,
            HttpServletRequest request,
            Object object,
            String objectTitle,
            Source source,
            Operation operation,
            SubjectArea subjArea,
            Department dept) {
        try {
            HttpSession httpSession = request.getSession();
            String userId = (String)httpSession.getAttribute("authUserExtId");
            User user = Web.getUser(httpSession);
            if (userId==null) {
                if (user!=null) {
                    Debug.warning("No authenticated user defined, using "+user.getName());
                    userId = user.getId();
                }
            }
            if (userId==null) {
                Debug.warning("Unable to add change log -- no user.");
                return;
            }
            Session session = Session.getCurrentAcadSession(user);
            if (session==null) {
                Debug.warning("Unable to add change log -- no academic session.");
                return;
            }
            TimetableManager manager = TimetableManager.findByExternalId(userId);
            if (manager==null)
                manager = TimetableManager.getManager(user);
            if (manager==null) {
                Debug.warning("Unable to add change log -- no timetabling manager.");
                return;
            }
            Number objectUniqueId = (Number)object.getClass().getMethod("getUniqueId", new Class[]{}).invoke(object, new Object[]{});
            String objectType = object.getClass().getName();
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
            chl.setObjectTitle(objectTitle==null || objectTitle.length()==0?"N/A":objectTitle);
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
    
    public static String getMessage(ServletRequest request, String message) {
        MessageResources rsc = (MessageResources)request.getAttribute(Globals.MESSAGES_KEY);
        if (rsc==null) return message;
        String ret = rsc.getMessage(message);
        return (ret==null?message:ret);
    }
            
    public String getOperationTitle(ServletRequest request) {
        return getMessage(request, "changelog.operation."+getOperationString());
    }

    public String getSourceTitle(ServletRequest request) {
        return getMessage(request, "changelog.source."+getSourceString());
    }
    
    public String getLabel(ServletRequest request) {
        return 
        "Last " + getOperationTitle(request) +
        " of " + getObjectTitle() +
        " was made by " + getManager().getShortName() +
        " at " + sDF.format(getTimeStamp()); 
    }

    public String getShortLabel(ServletRequest request) {
        return 
            "Last " + getOperationTitle(request) +
            " was made by " + getManager().getShortName() +
            " at " + sDF.format(getTimeStamp()); 
    }
    
    public int compareTo(Object obj) {
        if (obj==null || !(obj instanceof ChangeLog)) return -1;
        ChangeLog chl = (ChangeLog)obj;
        int cmp = getTimeStamp().compareTo(chl.getTimeStamp());
        if (cmp!=0) return cmp;
        return getUniqueId().compareTo(chl.getUniqueId());
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
                        "order by ch.timeStamp desc");
            q.setLong("objectUniqueId", objectUniqueId.longValue());
            q.setString("objectType",objectType);
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