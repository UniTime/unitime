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
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseCourseOffering;
import org.unitime.timetable.model.comparators.AcadAreaReservationComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.ComboBoxLookup;




public class CourseOffering extends BaseCourseOffering {
	private static final long serialVersionUID = 1L;
	private String courseName;
	
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

	/**
	 * Constructor for required fields
	 */
	public CourseOffering (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SubjectArea subjectArea,
		org.unitime.timetable.model.InstructionalOffering instructionalOffering,
		java.lang.Boolean isControl,
		java.lang.String permId,
		java.lang.Integer nbrExpectedStudents,
		java.lang.String courseNbr) {

		super (
			uniqueId,
			subjectArea,
			instructionalOffering,
			isControl,
			permId,
			nbrExpectedStudents,
			courseNbr);
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

	public String toString() {
		return (
		        this.getSubjectAreaAbbv() 
		        + " " + this.getCourseNbr() 
		        + ( (this.getTitle()!=null) 
		                ? " - " + this.getTitle() 
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
		    co.setNbrExpectedStudents(new Integer(0));
		    co.setIsControl(new Boolean(true));
		    //FIXME hfernan - What is the perm id for a new course offering?
		    co.setPermId("-1");
		    
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
                setLong("sessionId", sessionId.longValue()).setCacheable(true).iterate();i.hasNext();) {
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

		if (!tm.getDepartments().contains(getDepartment())) return false;
		
		if (!getDepartment().effectiveStatusType().canOwnerEdit()) return false;

    	return true;
    }
    
    public boolean isViewableBy(User user){
    	if (user==null) return false;
    	if (user.isAdmin()) return true;
		if (getDepartment()==null) return false;
		if (isEditableBy(user)) return true;
		
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
    	return (new CourseOfferingDAO()).
    		getSession().
    		createCriteria(LastLikeCourseDemand.class).
    		add(Restrictions.eq("subjectArea",getSubjectArea())).
    		add(Restrictions.eq("courseNbr",getCourseNbr())).
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
}