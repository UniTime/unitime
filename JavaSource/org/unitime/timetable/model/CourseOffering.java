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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.LazyInitializationException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.model.base.BaseCourseOffering;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.OverrideTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Heston Fernandes
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "course_offering")
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

	@Transient
	public String getCourseName() {
		return getSubjectAreaAbbv()+" "+getCourseNbr();
	}
	
	@Transient
	public String getCourseNameWithTitle() {
		return 
			getSubjectAreaAbbv()+" "+getCourseNbr()+
			(getTitle()!=null && !getTitle().isEmpty()?" - "+getTitle():""); 
	}

	@Transient
	public String getCourseNumberWithTitle(){
		return(getCourseNbr()+(getTitle()!=null && !getTitle().isEmpty()?" - "+getTitle():""));
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
	@Transient
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
	public static CourseOffering findBySessionSubjAreaIdCourseNbr(Long acadSessionId, Long subjAreaId, String courseNbr) {
		return CourseOfferingDAO.getInstance().getSession().createQuery(
				"from CourseOffering co " +
				"where co.subjectArea.uniqueId = :subjArea " +
				"and co.courseNbr = :crsNbr " +
				"and co.instructionalOffering.session.uniqueId = :acadSessionId", CourseOffering.class)
				.setParameter("crsNbr", courseNbr)
				.setParameter("subjArea", subjAreaId)
				.setParameter("acadSessionId", acadSessionId)
				.setMaxResults(1).uniqueResult();
	}
	
    public static CourseOffering findBySessionSubjAreaAbbvCourseNbr(Long acadSessionId, String subjAreaAbbv, String courseNbr) {
		return CourseOfferingDAO.getInstance().getSession().createQuery(
				"from CourseOffering co " +
				"where co.subjectArea.subjectAreaAbbreviation = :subjArea " +
				"and co.courseNbr = :crsNbr " +
				"and co.instructionalOffering.session.uniqueId = :acadSessionId", CourseOffering.class)
				.setParameter("crsNbr", courseNbr)
				.setParameter("subjArea", subjAreaAbbv)
				.setParameter("acadSessionId", acadSessionId)
				.setMaxResults(1).uniqueResult();
    }
	
    public static CourseOffering findBySessionSubjAreaAbbvCourseNbrTitle(Long acadSessionId, String subjAreaAbbv, String courseNbr, String title) {

        InstructionalOfferingDAO iDao = InstructionalOfferingDAO.getInstance();
        org.hibernate.Session hibSession = iDao.getSession();
        
        String sql = " from CourseOffering co " +
                     " where co.subjectArea.subjectAreaAbbreviation=:subjArea" +
                     " and co.courseNbr = :crsNbr" +
                     " and co.title = :title" +
                     " and co.instructionalOffering.session.uniqueId = :acadSessionId";
        Query<CourseOffering> query = hibSession.createQuery(sql, CourseOffering.class);
        query.setParameter("crsNbr", courseNbr);
        query.setParameter("subjArea", subjAreaAbbv);
        query.setParameter("acadSessionId", acadSessionId.longValue());
        query.setParameter("title", title);
        
        return query.uniqueResult();

    }

    /**
	 * Add a new course offering (instructional offering) to the database
	 * @param subjAreaId Subject Area Unique Id
	 * @param courseNbr Course Number
	 * @return CourseOffering object representing thenew course offering 
	 * @throws Exception
	 */
	public static synchronized CourseOffering addNew(Long subjAreaId, String courseNbr) throws Exception {
	    
	    CourseOffering co = null; 
	    InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
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
		    SubjectArea subjArea = SubjectAreaDAO.getInstance().get(subjAreaId);
		    org.unitime.timetable.model.Session acadSession = subjArea.getSession();
		    
		    CourseOfferingDAO cdao = CourseOfferingDAO.getInstance();
		    co = new CourseOffering();
		    co.setSubjectArea(subjArea);
		    co.setCourseNbr(courseNbr);
		    co.setProjectedDemand(Integer.valueOf(0));
            co.setDemand(Integer.valueOf(0));
		    co.setNbrExpectedStudents(Integer.valueOf(0));
		    co.setIsControl(Boolean.valueOf(true));
		    co.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)CourseOfferingDAO.getInstance().getSession(), co).toString());
		    
		    HashSet s = new HashSet();
		    s.add(co);
		    
	        // Add new Instructional Offering
		    InstructionalOffering io = new InstructionalOffering();
		    io.setNotOffered(Boolean.valueOf(false));
		    io.setSession(acadSession);
		    io.generateInstrOfferingPermId();
		    io.setLimit(Integer.valueOf(0));
		    io.setByReservationOnly(false);
		    cdao.getSession().persist(io);
		    idao.getSession().refresh(io);
		    co.setInstructionalOffering(io);
		    io.addToCourseOfferings(co);
		    cdao.getSession().persist(co);
		    cdao.getSession().flush();
		    cdao.getSession().refresh(co);
		    cdao.getSession().refresh(subjArea);
        	String className = ApplicationProperty.ExternalActionInstructionalOfferingAdd.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingAddAction addAction = (ExternalInstructionalOfferingAddAction) (Class.forName(className).getDeclaredConstructor().newInstance());
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
	public static List<ComboBoxLookup> getControllingCourses(Long sessionId) {
		List<ComboBoxLookup> l = new ArrayList<ComboBoxLookup>();
        for (Object[] o: CourseOfferingDAO.getInstance().getSession().createQuery(
                "select co.uniqueId, co.subjectAreaAbbv, co.courseNbr from CourseOffering co where co.isControl=true and co.subjectArea.session.uniqueId=:sessionId", Object[].class).
                setParameter("sessionId", sessionId.longValue()).setCacheable(true).list()) {
            l.add(new ComboBoxLookup(o[0].toString(), o[1]+" "+o[2]));
        }
		return l;
	}
	
	@Transient
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
	
	@Transient
	public Department getManagingDept() {
		Department dept = null;
		for (InstrOfferingConfig config: getInstructionalOffering().getInstrOfferingConfigs())
			for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
				Department mgr = subpart.getManagingDept();
				if (mgr == null) {
					continue;
				} else if (dept == null) {
					dept = mgr;
				} else if (!dept.equals(mgr)) {
					return getDepartment();
				}
			}
		return (dept == null ? getDepartment() : dept);
	}
	
	@Transient
    public List<LastLikeCourseDemand> getCourseOfferingDemands() {
        if (getPermId()!=null)
            return (CourseOfferingDAO.getInstance()).
                getSession().
                createQuery("select d from LastLikeCourseDemand d where d.coursePermId=:permId and d.subjectArea.session.uniqueId=:sessionId", LastLikeCourseDemand.class).
                setParameter("permId", getPermId()).
                setParameter("sessionId", getSubjectArea().getSessionId()).
                setCacheable(true).
                list();
        else 
            return (CourseOfferingDAO.getInstance()).
    		    getSession().
    		    createQuery("select d from LastLikeCourseDemand d where d.subjectArea.uniqueId=:subjectAreaId and d.courseNbr=:courseNbr", LastLikeCourseDemand.class).
    		    setParameter("subjectAreaId", getSubjectArea().getUniqueId()).
    		    setParameter("courseNbr", getCourseNbr()).
    		    setCacheable(true).
    		    list();    	
    }
    
    //TODO: to distinguish between last like semester student demands and all student demands in the future
	@Transient
    public List getLastLikeSemesterCourseOfferingDemands() {
    	return getCourseOfferingDemands();
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
        co.setFundingDept(this.getFundingDept());
    	return co;
    }
    //End
    
    public static List<CourseOffering> findAll(Long sessionId) {
        return CourseOfferingDAO.getInstance().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId", CourseOffering.class).
            setParameter("sessionId", sessionId.longValue()).
            list(); 
    }
    
    public static CourseOffering findBySubjectAreaCourseNbr(Long sessionId, String subjectAreaAbbv, String courseNbr) {
        return CourseOfferingDAO.getInstance().
            getSession().
            createQuery(
                "select c from CourseOffering c where "+
                "c.subjectArea.session.uniqueId=:sessionId and "+
                "c.subjectArea.subjectAreaAbbreviation=:subjectAreaAbbv and "+
                "c.courseNbr=:courseNbr", CourseOffering.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("subjectAreaAbbv", subjectAreaAbbv).
            setParameter("courseNbr", courseNbr).
            setCacheable(true).
            uniqueResult(); 
    }
    
    public static CourseOffering findByExternalId(Long sessionId, String externalId) {
        return CourseOfferingDAO.getInstance().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId and c.externalUniqueId=:externalId", CourseOffering.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("externalId", externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public static CourseOffering findByUniqueId(Long uniqueId) {
        return CourseOfferingDAO.getInstance().get(uniqueId);
    }

    public static CourseOffering findBySubjectCourseNbrInstrOffUniqueId( String subjectAreaAbbv, String courseNbr, Long instrOffrUniqueId) {
    	return CourseOfferingDAO.getInstance().
        getSession().
        createQuery(
            "select c from InstructionalOffering io inner join io.courseOfferings c where "+
            "io.uniqueId=:instrOffrUniqueId and "+
            "c.subjectArea.subjectAreaAbbreviation=:subjectAreaAbbv and "+
            "c.courseNbr=:courseNbr", CourseOffering.class).
        setParameter("instrOffrUniqueId", instrOffrUniqueId.longValue()).
        setParameter("subjectAreaAbbv", subjectAreaAbbv).
        setParameter("courseNbr", courseNbr).
        setCacheable(true).
        uniqueResult(); 
    }
    
    public static CourseOffering findByName(String name, Long sessionId) {
    	return CourseOfferingDAO.getInstance().getSession().createQuery(
    			"select c from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId and lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :name", CourseOffering.class)
    			.setParameter("sessionId", sessionId).setParameter("name", name.toLowerCase()).setCacheable(true).uniqueResult();
    }
    
    public static CourseOffering findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return CourseOfferingDAO.getInstance().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId and c.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom", CourseOffering.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }

    public int compareTo(Object o) {
    	if (o == null || !(o instanceof CourseOffering)) return -1;
    	CourseOffering co = (CourseOffering)o;
    	int cmp = getSubjectAreaAbbv().compareTo(co.getSubjectAreaAbbv());
    	if (cmp!=0) return cmp;
    	cmp = getCourseNbr().compareTo(co.getCourseNbr());
    	if (cmp!=0) return cmp;
    	return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(co.getUniqueId() == null ? -1 : co.getUniqueId());
    }
    
	@Transient
    public CourseCreditUnitConfig getCredit(){
    	if(this.getCreditConfigs() == null || this.getCreditConfigs().size() != 1){
    		return(null);
    	} else {
    		return((CourseCreditUnitConfig)this.getCreditConfigs().iterator().next());
    	}
    }

    public void setCredit(CourseCreditUnitConfig courseCreditUnitConfig){
    	if (this.getCreditConfigs() == null || this.getCreditConfigs().size() == 0){
    		this.addToCreditConfigs(courseCreditUnitConfig);
    	} else if (!this.getCreditConfigs().contains(courseCreditUnitConfig)){
    		this.getCreditConfigs().clear();
    		this.getCreditConfigs().add(courseCreditUnitConfig);
    	} else {
    		//course already contains this config so we do not need to add it again.
    	}
    }
    
	@Transient
    public boolean isAllowStudentScheduling() {
    	return getSubjectArea().getDepartment().isAllowStudentScheduling();
    }
    
    public List<StudentClassEnrollment> getClassEnrollments(Student s) {
    	List<StudentClassEnrollment> ret = new ArrayList<StudentClassEnrollment>();
    	for (StudentClassEnrollment e: s.getClassEnrollments()) {
    		if (this.equals(e.getCourseOffering())) ret.add(e);
    	}
    	return ret;
    }
    
	@Transient
    public Set<OverrideType> getEnabledOverrides() {
    	Set<OverrideType> ret = new TreeSet<OverrideType>();
    	for (OverrideType override: OverrideTypeDAO.getInstance().findAll()) {
    		if (!getDisabledOverrides().contains(override))
    			ret.add(override);
    	}
    	return ret;
    }
    
	@Transient
    public Department getEffectiveFundingDept() {
    	if (getFundingDept() == null) {
    		return getSubjectArea().getEffectiveFundingDept();
    	} else {
    		return getFundingDept();
    	}
    }
}
