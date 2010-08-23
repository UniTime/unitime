/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC, and individual contributors
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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.impl.SessionImpl;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.model.base.BaseCourseOffering;
import org.unitime.timetable.model.comparators.AcadAreaReservationComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;




public class CourseOffering extends BaseCourseOffering implements Comparable {
	private static final long serialVersionUID = 1L;
	
	/** Request attribute name for list of course offerings */
    public static final String CRS_OFFERING_LIST_ATTR_NAME = "crsOfferingList";
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseOffering () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseOffering (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/

	public String getCourseName() {
		return getSubjectAreaAbbv()+" "+getCourseNbr();
	}
	
	public String getCourseNameWithTitle() {
		return 
			getSubjectAreaAbbv()+" "+getCourseNbr()+
			(getTitle()!=null?" - "+getTitle():""); 
	}

	public String getCourseNumberWithTitle(){
		return(getCourseNbr()+(getTitle()!=null?" - "+getTitle():""));
	}
	public String toString() {
		return (
		        this.getSubjectAreaAbbv() 
		        + " " + this.getCourseNbr() 
		        + ( (this.getTitle()!=null) 
		                ? " - " + this.getTitle().replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("'", "&quot;").replaceAll("&", "&amp;") 
		                : "") 
		           );
	}
	
	/**
	 * Same as isIsContol. Added so that beans in JSPs can access getter method
	 * @return true/false
	 */
	public Boolean getIsControl() {
	    return this.isIsControl();
	}

	/**
	 * Search for a particular course offering
	 * @param acadSessionId Academic Session
	 * @param subjAreaId Subject Area Unique Id
	 * @param courseNbr Course Number
	 * @return List object with matching course offering
	 */
	public static List search(Long acadSessionId, String subjAreaId, String courseNbr) {

	    InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
	    org.hibernate.Session hibSession = iDao.getSession();
	    
	    String sql = " from CourseOffering co " +
	    			 " where co.uniqueCourseNbr.subjectArea.uniqueId=:subjArea" +
	    			 " and co.uniqueCourseNbr.courseNbr = :crsNbr" +
	    			 " and co.instructionalOffering.session.uniqueId = :acadSessionId";
	    Query query = hibSession.createQuery(sql);
	    query.setString("crsNbr", courseNbr);
	    query.setString("subjArea", subjAreaId);
	    query.setLong("acadSessionId", acadSessionId.longValue());
	    
	    List l = query.list();

	    return l;
	}
	
    public static CourseOffering findBySessionSubjAreaAbbvCourseNbr(Long acadSessionId, String subjAreaAbbv, String courseNbr) {

        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
        org.hibernate.Session hibSession = iDao.getSession();
        
        String sql = " from CourseOffering co " +
                     " where co.uniqueCourseNbr.subjectArea.subjectAreaAbbreviation=:subjArea" +
                     " and co.uniqueCourseNbr.courseNbr = :crsNbr" +
                     " and co.instructionalOffering.session.uniqueId = :acadSessionId";
        Query query = hibSession.createQuery(sql);
        query.setString("crsNbr", courseNbr);
        query.setString("subjArea", subjAreaAbbv);
        query.setLong("acadSessionId", acadSessionId.longValue());
        
        return (CourseOffering)query.uniqueResult();
        
    }
	
    public static CourseOffering findBySessionSubjAreaAbbvCourseNbrTitle(Long acadSessionId, String subjAreaAbbv, String courseNbr, String title) {

        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
        org.hibernate.Session hibSession = iDao.getSession();
        
        String sql = " from CourseOffering co " +
                     " where co.subjectArea.subjectAreaAbbreviation=:subjArea" +
                     " and co.courseNbr = :crsNbr" +
                     " and co.title = :title" +
                     " and co.instructionalOffering.session.uniqueId = :acadSessionId";
        Query query = hibSession.createQuery(sql);
        query.setString("crsNbr", courseNbr);
        query.setString("subjArea", subjAreaAbbv);
        query.setLong("acadSessionId", acadSessionId.longValue());
        query.setString("title", title);
        
        return (CourseOffering)query.uniqueResult();

    }

    /**
	 * Add a new course offering (instructional offering) to the database
	 * @param subjAreaId Subject Area Unique Id
	 * @param courseNbr Course Number
	 * @return CourseOffering object representing thenew course offering 
	 * @throws Exception
	 */
	public static synchronized CourseOffering addNew(String subjAreaId, String courseNbr) throws Exception {
	    
	    Statement stmt = null;
	    ResultSet rs = null;

	    CourseOffering co = null; 
	    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	    Session hibSession = idao.getSession();
	    try {
            /*
		    // Get Instr Offering Perm Id
		    String permId = "";
		    String sql = "select timetable.instr_offr_permid_seq.nextval from dual";
		    stmt = Database.execute(sql);
		    rs = stmt.getResultSet();
 
		    if(rs.next()) {
		        permId = rs.getString(1);
		    }
		    else {
		        throw new Exception("Could not retrieve instr offering perm id");
		    }
            */
		    
		    // Add new Course Offering
		    SubjectArea subjArea = new SubjectAreaDAO().get(new Long(subjAreaId));
		    org.unitime.timetable.model.Session acadSession = subjArea.getSession();
		    
		    CourseOfferingDAO cdao = new CourseOfferingDAO();
		    co = new CourseOffering();
		    co.setSubjectArea(subjArea);
		    co.setCourseNbr(courseNbr);
		    co.setProjectedDemand(new Integer(0));
            co.setDemand(new Integer(0));
		    co.setNbrExpectedStudents(new Integer(0));
		    co.setIsControl(new Boolean(true));
		    co.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImpl)new CourseOfferingDAO().getSession(), co).toString());
		    
		    HashSet s = new HashSet();
		    s.add(co);
		    
	        // Add new Instructional Offering
		    InstructionalOffering io = new InstructionalOffering();
		    io.setNotOffered(new Boolean(false));
		    io.setDesignatorRequired(new Boolean(false));
		    io.setSession(acadSession);
		    io.generateInstrOfferingPermId();
		    io.setLimit(new Integer(0));
		    idao.saveOrUpdate(io);
		    idao.getSession().refresh(io);
		    co.setInstructionalOffering(io);
		    io.addTocourseOfferings(co);
		    cdao.saveOrUpdate(co);		    
		    cdao.getSession().refresh(co);
		    cdao.getSession().refresh(subjArea);
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.add_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingAddAction addAction = (ExternalInstructionalOfferingAddAction) (Class.forName(className).newInstance());
	       		addAction.performExternalInstructionalOfferingAddAction(io, hibSession);
        	}

	    }
	    catch (Exception e) {
	        //Database.closeConnObjs(stmt, rs);
	        Debug.error(e);
	        throw new Exception("Could not create new course offering: " + e.getMessage() );
	    }
	    finally {
	    	//if (hibSession!=null && hibSession.isOpen()) hibSession.close();
	    }
	    
	    return co;
	}

	/**
	 * Get a list of all controlling courses for the academic session
	 * @param sessionId Academic Session Unique Id
	 * @return Vector containing ComboBoxLookup objects
	 * @see ComboBoxLookup
	 */
	public static Vector getControllingCourses(Long sessionId) {
        
        Vector l = new Vector();
        for (Iterator i=new CourseOfferingDAO().getSession().createQuery(
                "select co.uniqueId, co.subjectAreaAbbv, co.courseNbr from CourseOffering co where co.isControl=true and co.subjectArea.session.uniqueId=:sessionId").
                setLong("sessionId", sessionId.longValue()).setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            l.addElement(new ComboBoxLookup(o[0].toString(), o[1]+" "+o[2]));
        }
        
		return l;
	}
	
	public Department getDepartment() {
	    Department dept = null;
	    try {
	        dept = this.getSubjectArea().getDepartment();
	        if(dept.toString()==null) {
	        }
	    }
	    catch (LazyInitializationException lie) {
	        new _RootDAO().getSession().refresh(this);
	        dept = this.getSubjectArea().getDepartment();
	    }
	    
		return (dept);
	}
	
	/** The course as well as all its classes are editable by the user */
	public boolean isFullyEditableBy(User user) {
    	if (user==null) return false;
    	if (user.isAdmin()) return true;
		if (getDepartment()==null) return false;
		
		TimetableManager tm = TimetableManager.getManager(user);
		if (tm==null) return false;

		if (!tm.getDepartments().contains(getDepartment())) return false;
		
		if (!getDepartment().effectiveStatusType().canOwnerEdit()) return false;

    	if (getInstructionalOffering()!=null) {
    		for (Iterator i1=getInstructionalOffering().getInstrOfferingConfigs().iterator();i1.hasNext();) {
    			InstrOfferingConfig ioc = (InstrOfferingConfig) i1.next();
    			for (Iterator i2=ioc.getSchedulingSubparts().iterator();i2.hasNext();) {
    				SchedulingSubpart ss = (SchedulingSubpart)i2.next();
    				if (!ss.canUserEdit(user)) return false;
    			}
    		}
    	}

    	return true;
	}

    public boolean isEditableBy(User user){
    	if (user==null) return false;
    	if (user.isAdmin()) return true;
		if (getDepartment()==null) return false;
		
		TimetableManager tm = TimetableManager.getManager(user);
		if (tm==null) return false;
		
		if (!Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole())) return false;

		if (!tm.getDepartments().contains(getDepartment())) return false;
		
		if (!getDepartment().effectiveStatusType().canOwnerEdit()) return false;

    	return true;
    }
    
    public boolean isLimitedEditableBy(User user){
        if (user==null) return false;
        if (user.isAdmin()) return true;
        if (getDepartment()==null) return false;
        
        TimetableManager tm = TimetableManager.getManager(user);
        if (tm==null) return false;

        if (!Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole())) return false;

        if (!tm.getDepartments().contains(getDepartment())) return false;
        
        if (!getDepartment().effectiveStatusType().canOwnerLimitedEdit()) return false;

        return true;
    }

    public boolean isViewableBy(User user){
    	if (user==null) return false;
    	if (user.isAdmin()) return true;
		if (getDepartment()==null) return false;
		if (isEditableBy(user)) return true;
        if (user.getCurrentRole() != null && (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)))
            return true;
		
    	if (getInstructionalOffering()!=null) {
    		for (Iterator i1=getInstructionalOffering().getInstrOfferingConfigs().iterator();i1.hasNext();) {
    			InstrOfferingConfig ioc = (InstrOfferingConfig) i1.next();
    			for (Iterator i2=ioc.getSchedulingSubparts().iterator();i2.hasNext();) {
    				SchedulingSubpart ss = (SchedulingSubpart)i2.next();
    				if (ss.canUserView(user)) return true;
    			}
    		}
    	}

    	return false;
    }
    
    public List getCourseOfferingDemands() {
        if (getPermId()!=null)
            return (new CourseOfferingDAO()).
                getSession().
                createQuery("select d from LastLikeCourseDemand d where d.coursePermId=:permId and d.subjectArea.session.uniqueId=:sessionId").
                setString("permId",getPermId()).
                setLong("sessionId",getSubjectArea().getSessionId()).
                setCacheable(true).
                list();
        else 
            return (new CourseOfferingDAO()).
    		    getSession().
    		    createQuery("select d from LastLikeCourseDemand d where d.subjectArea.uniqueId=:subjectAreaId and d.courseNbr=:courseNbr").
    		    setLong("subjectAreaId",getSubjectArea().getUniqueId()).
    		    setString("courseNbr",getCourseNbr()).
    		    setCacheable(true).
    		    list();    	
    }
    
    //TODO: to distinguish between last like semester student demands and all student demands in the future
    public List getLastLikeSemesterCourseOfferingDemands() {
    	return getCourseOfferingDemands();
    }

    //TODO Reservations functionality to be removed later
    /**
     * Returns a list containing academic area reservations for a Course Offering
     * @param acadArea include academic area reservations
     * @return collection of reservations (collection is empty is none found)
     */
    public Collection getReservations( boolean acadArea ) {
        Collection resv = new Vector();
        if (acadArea && this.getAcadAreaReservations()!=null) {
            List c = new Vector(this.getAcadAreaReservations());
            Collections.sort(c, new AcadAreaReservationComparator());
            resv.addAll(c);
        }
        return resv;
    }

    /**
     * Returns effective reservations for the config
     * @param acadArea include academic area reservations
     * @return collection of reservations (collection is empty is none found)
     */
    public Collection effectiveReservations( boolean acadArea ) {
        //TODO hfernan - effective reservations - if applicable
        return getReservations( acadArea );
    }
    
    /**
     * Clones the course Offering
     * Note: It does not set the Instructional Offering
     */
    public Object clone() {
    	CourseOffering co = new CourseOffering();
        co.setCourseNbr(this.getCourseNbr());
        co.setDemand(this.getDemand());
        co.setPermId(this.getPermId());
        co.setNbrExpectedStudents(this.getNbrExpectedStudents());
        co.setProjectedDemand(this.getProjectedDemand());
        co.setSubjectArea(this.getSubjectArea());
        co.setSubjectAreaAbbv(this.getSubjectAreaAbbv());
        co.setTitle(this.getTitle());
        co.setDemandOffering(this.getDemandOffering());
        co.setDemandOfferingType(this.getDemandOfferingType());
        co.setExternalUniqueId(this.getExternalUniqueId());
        co.setScheduleBookNote(this.getScheduleBookNote());
        co.setIsControl(this.getIsControl());
    	return co;
    }
    //End
    
    public static List findAll(Long sessionId) {
        return new CourseOfferingDAO().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
            list(); 
    }
    
    public static CourseOffering findBySubjectAreaCourseNbr(Long sessionId, String subjectAreaAbbv, String courseNbr) {
        return (CourseOffering)new CourseOfferingDAO().
            getSession().
            createQuery(
                "select c from CourseOffering c where "+
                "c.subjectArea.session.uniqueId=:sessionId and "+
                "c.subjectArea.subjectAreaAbbreviation=:subjectAreaAbbv and "+
                "c.courseNbr=:courseNbr").
            setLong("sessionId", sessionId.longValue()).
            setString("subjectAreaAbbv", subjectAreaAbbv).
            setString("courseNbr", courseNbr).
            setCacheable(true).
            uniqueResult(); 
    }
    
    public static CourseOffering findByExternalId(Long sessionId, String externalId) {
        return (CourseOffering)new CourseOfferingDAO().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId and c.externalUniqueId=:externalId").
            setLong("sessionId", sessionId.longValue()).
            setString("externalId", externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public static CourseOffering findByUniqueId(Long uniqueId) {
        return (CourseOffering)new CourseOfferingDAO().get(uniqueId);
    }

    public static CourseOffering findBySubjectCourseNbrInstrOffUniqueId( String subjectAreaAbbv, String courseNbr, Long instrOffrUniqueId) {
    	return (CourseOffering)new CourseOfferingDAO().
        getSession().
        createQuery(
            "select c from InstructionalOffering io inner join io.courseOfferings c where "+
            "io.uniqueId=:instrOffrUniqueId and "+
            "c.subjectArea.subjectAreaAbbreviation=:subjectAreaAbbv and "+
            "c.courseNbr=:courseNbr").
        setLong("instrOffrUniqueId", instrOffrUniqueId.longValue()).
        setString("subjectAreaAbbv", subjectAreaAbbv).
        setString("courseNbr", courseNbr).
        setCacheable(true).
        uniqueResult(); 
    }
    
    public static CourseOffering findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return (CourseOffering)new CourseOfferingDAO().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId and c.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom").
            setLong("sessionId", sessionId.longValue()).
            setLong("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }

    public static void updateCourseOfferingEnrollmentForSession(org.unitime.timetable.model.Session acadSession, org.hibernate.Session hibSession) throws Exception{
        Transaction trans = null;
        try {
     	trans = hibSession.beginTransaction();
     	List<Long> courseIds = (List<Long>)hibSession.createQuery(
     			"select crs.uniqueId from CourseOffering crs inner join crs.instructionalOffering as io " +
                " where io.session.uniqueId = :sessionId)").
                setLong("sessionId", acadSession.getUniqueId().longValue()).list();
     	int count = 0;
     	String ids = "";
     	for (Long id: courseIds) {
     		if (count > 0) ids += ",";
     		ids += id;
     		if (count == 1000) {
     			hibSession.createQuery("update CourseOffering  c " +
     	         		"set c.enrollment=(select count(distinct d.student) " +
     	                 " from StudentClassEnrollment d " +
     	                 " where d.courseOffering.uniqueId =c.uniqueId) " + 
     	                 " where c.uniqueId in (" + ids + ")").executeUpdate();
     			count = 0; ids = "";
     		}
     	}
     	if (count > 0) {
 			hibSession.createQuery("update CourseOffering  c " +
 	         		"set c.enrollment=(select count(distinct d.student) " +
 	                 " from StudentClassEnrollment d " +
 	                 " where d.courseOffering.uniqueId =c.uniqueId) " + 
 	                 " where c.uniqueId in (" + ids + ")").executeUpdate();
     	}
     	/*
     	// This does not work on MySQL (You can't specify target table 'COURSE_OFFERING' for update in FROM clause)
         hibSession.createQuery("update CourseOffering  c " +
         		"set c.enrollment=(select count(distinct d.student) " +
                 " from StudentClassEnrollment d " +
                 " where d.courseOffering.uniqueId =c.uniqueId) " + 
                 " where c.uniqueId in (select crs.uniqueId " + 
                 " from CourseOffering crs inner join crs.instructionalOffering as io " +
                 " where io.session.uniqueId = :sessionId)").
                 setLong("sessionId", acadSession.getUniqueId().longValue()).executeUpdate();
         */
         trans.commit();
        } catch (Exception e) {
     	   if (trans != null){
     		   trans.rollback();
     	   }
 		   throw(e);
        }
     }
    
    public int compareTo(Object o) {
    	if (o == null || !(o instanceof CourseOffering)) return -1;
    	CourseOffering co = (CourseOffering)o;
    	int cmp = getSubjectAreaAbbv().compareTo(co.getSubjectAreaAbbv());
    	if (cmp!=0) return cmp;
    	cmp = getCourseNbr().compareTo(co.getCourseNbr());
    	if (cmp!=0) return cmp;
    	return getUniqueId().compareTo(co.getUniqueId());
    }
}
