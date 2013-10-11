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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.base.BaseInstructionalOffering;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;
import org.unitime.timetable.webutil.Navigation;



public class InstructionalOffering extends BaseInstructionalOffering {
	private static final long serialVersionUID = 1L;

	private CourseOffering controllingCourseOffering = null;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public InstructionalOffering () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public InstructionalOffering (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String getCourseName(){
		return(this.getControllingCourseOffering() == null?"missing course name":this.getControllingCourseOffering().getCourseName());
	}

	public String getCourseNameWithTitle(){
		return(this.getControllingCourseOffering().getCourseNameWithTitle());
	}

	/**
	 * Remove a course offering from the instructional offering
	 * @param co Course offering object to be removed
	 * @return true if course offering was found and removed, false otherwise
	 */
	public boolean removeCourseOffering(CourseOffering co) {
	    Set s = getCourseOfferings();
        Iterator it = s.iterator();
        CourseOffering tempCo = null;
        while (it.hasNext()){
            tempCo = (CourseOffering) it.next();
            if (tempCo.getUniqueId().intValue()==co.getUniqueId().intValue()){
            	Debug.debug("Removing course from instructional offering");
                s.remove(tempCo);
                tempCo.setInstructionalOffering(null);
                setCourseOfferings(s);
                return true;
            }
        }
        return false;
	}

	public CourseOffering findSortCourseOfferingForSubjectArea(Long subjectAreaUniqueId) {
		CourseOffering controlingCourseOffering = getControllingCourseOffering();
		if (controlingCourseOffering!=null && (subjectAreaUniqueId==null || subjectAreaUniqueId.equals(controlingCourseOffering.getSubjectArea().getUniqueId())))
			return controlingCourseOffering;

		CourseOffering minCo = null;
		for (Iterator i=getCourseOfferings().iterator(); i.hasNext();) {
			CourseOffering co = (CourseOffering)i.next();
			if (subjectAreaUniqueId!=null && !co.getSubjectArea().getUniqueId().equals(subjectAreaUniqueId)) continue;
			if (minCo==null || minCo.getCourseNbr().compareTo(co.getCourseNbr())<0)
				minCo = co;
		}

		return minCo;
	}

	public CourseOffering findSortCourseOfferingForSubjectArea(SubjectArea subjectArea) {
	    return (this.findSortCourseOfferingForSubjectArea(subjectArea.getUniqueId()));
	}

	public TreeSet courseOfferingsMinusSortCourseOfferingForSubjectArea(Long subjectAreaUID){
	    CourseOffering co = this.findSortCourseOfferingForSubjectArea(subjectAreaUID);
	    TreeSet crsOffrs = new TreeSet(new CourseOfferingComparator());
	    Iterator it = this.getCourseOfferings().iterator();
	    CourseOffering tmpCo = null;
	    while (it.hasNext()){
	        tmpCo = (CourseOffering) it.next();
	        if(!tmpCo.getUniqueId().equals(co.getUniqueId())){
	            crsOffrs.add(tmpCo);
	        }
	    }
	    return(crsOffrs);
	}

	public TreeSet courseOfferingsMinusSortCourseOfferingForSubjectArea(SubjectArea subjectArea){
	    return(courseOfferingsMinusSortCourseOfferingForSubjectArea(subjectArea.getUniqueId()));
	}

	public Boolean getNotOffered() {
	    return(isNotOffered());
	}


	public Department getDepartment() {
		return (this.getControllingCourseOffering().getDepartment());
	}

	/**
	 * @return Returns the controllingCourseOffering.
	 */
	public CourseOffering getControllingCourseOffering() {
		if (controllingCourseOffering == null){
			if (this.getCourseOfferings() != null && this.getCourseOfferings().size() > 0){
				Iterator it = this.getCourseOfferings().iterator();
				CourseOffering co = null;
				while(controllingCourseOffering == null && it.hasNext()){
					co = (CourseOffering) it.next();
					if (co.isIsControl().booleanValue()){
						controllingCourseOffering = co;
					}
				}
			}
		}
		return controllingCourseOffering;
	}

	/**
	 * @return Course name of the controlling course
	 */
	public String toString() {
	    return this.getControllingCourseOffering().getCourseName();
	}

	/**
	 * Search for instructional offerings
	 * @param acadSessionId Academic Session
	 * @param subjectAreaId Subject Area
	 * @param courseNbr Course Number
	 * @return TreeSet of results
	 */
	public static TreeSet<InstructionalOffering> search(
	        Long acadSessionId,
	        Long subjectAreaId,
	        String courseNbr,
	        boolean fetchStructure,
	        boolean fetchCredits,
	        boolean fetchInstructors,
	        boolean fetchPreferences,
	        boolean fetchAssignments,
	        boolean fetchReservations) {

		org.hibernate.Session hibSession = (new InstructionalOfferingDAO()).getSession();

		StringBuffer query = new StringBuffer();
		query.append("select distinct io ");
		query.append(" from InstructionalOffering as io inner join io.courseOfferings as co ");

		if (fetchStructure) {
			query.append("left join fetch io.courseOfferings as cox ");
			query.append("left join fetch io.instrOfferingConfigs as ioc ");
			query.append("left join fetch ioc.schedulingSubparts as ss ");
			query.append("left join fetch ss.classes as c ");
			query.append("left join fetch ss.childSubparts as css ");
			query.append("left join fetch c.childClasses as cc ");
		}

		if (fetchCredits)
			query.append("left join fetch ss.creditConfigs as ssc ");

		if (fetchPreferences || fetchInstructors) {
			query.append("left join fetch c.classInstructors as ci ");
			query.append("left join fetch ci.instructor as di ");
		}

		if (fetchAssignments) {
			query.append("left join fetch c.assignments as ca ");
			query.append("left join fetch ca.rooms as car ");
		}

		if (fetchPreferences) {
			query.append("left join fetch c.preferences as cp ");
			query.append("left join fetch ss.preferences as ssp ");
			query.append("left join fetch di.preferences as dip ");
		}

		if (fetchReservations) {
			query.append("left join fetch ioc.individualReservations as ir ");
			query.append("left join fetch ioc.studentGroupReservations as sgr ");
			query.append("left join fetch ioc.acadAreaReservations as aar ");
			query.append("left join fetch ioc.posReservations as pr ");
		}

		query.append(" where io.session.uniqueId=:sessionId ");

		if (courseNbr != null && courseNbr.length() > 0){
		    query.append(" and co.courseNbr ");
		    if (courseNbr.indexOf('*')>=0) {
	            query.append(" like '");
	            courseNbr = courseNbr.replace('*', '%');
		    }
		    else {
	            query.append(" = '");
		    }
            if ("true".equals(ApplicationProperties.getProperty("tmtbl.courseNumber.upperCase", "true")))
            	courseNbr = courseNbr.toUpperCase();
            query.append(courseNbr);
            query.append("'  ");
        }

		query.append(" and co.subjectArea.uniqueId = :subjectAreaId ");

		Query q = hibSession.createQuery(query.toString());
		q.setFetchSize(1000);
		q.setLong("subjectAreaId", subjectAreaId);
		q.setLong("sessionId", acadSessionId.longValue());
		q.setCacheable(true);


        TreeSet<InstructionalOffering> ts = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(Long.valueOf(subjectAreaId)));

        long sTime = new java.util.Date().getTime();
		ts.addAll(q.list());
		long eTime = new java.util.Date().getTime();
        Debug.debug("fetch time = " + (eTime - sTime));

        return ts;
	}

    /**
     * Deletes all classes for this offering
     */
    public void deleteAllClasses(Session hibSession) {

        // Loop through configs - currently only one config is supported
        Set sIoc = this.getInstrOfferingConfigs();
        for (Iterator iterIoc=sIoc.iterator(); iterIoc.hasNext(); ) {
            InstrOfferingConfig tIoc = (InstrOfferingConfig) iterIoc.next();

            // Loop through subparts
            Set sSp = tIoc.getSchedulingSubparts();
            for (Iterator iterSp=sSp.iterator(); iterSp.hasNext(); ) {
                SchedulingSubpart tSp = (SchedulingSubpart) iterSp.next();

                // Loop through classes
                Set sCl = tSp.getClasses();
                for (Iterator iterCl=sCl.iterator(); iterCl.hasNext(); ) {
                    Class_ c = (Class_) iterCl.next();

                    // Delete Class Instructors
                    Set classInstrs = c.getClassInstructors();
                    for (Iterator iterCi=classInstrs.iterator(); iterCi.hasNext() ;) {
                        ClassInstructor ci = (ClassInstructor) iterCi.next();
                        DepartmentalInstructor instr = ci.getInstructor();
                        instr.removeClassInstructor(ci);
                        hibSession.delete(ci);
                    }
                    
                    Event.deleteFromEvents(hibSession, c);
                    Exam.deleteFromExams(hibSession, c);

                    // Delete class
                    hibSession.delete(c);
                }

                // Delete set of classes
                tSp.getClasses().clear();
            }
            
            Event.deleteFromEvents(hibSession, tIoc);
            Exam.deleteFromExams(hibSession, tIoc);
        }
    }

    /**
     * Deletes all classes for this offering
     */
    public void deleteAllDistributionPreferences(org.hibernate.Session hibSession) {
    	for (Iterator i1=getInstrOfferingConfigs().iterator();i1.hasNext();) {
    		InstrOfferingConfig cfg = (InstrOfferingConfig)i1.next();
    		for (Iterator i2=cfg.getSchedulingSubparts().iterator();i2.hasNext();) {
    			SchedulingSubpart ss = (SchedulingSubpart)i2.next();
    			ss.deleteAllDistributionPreferences(hibSession);
    		}
    	}
    }

    public InstructionalOffering getNextInstructionalOffering(SessionContext context) {
    	return getNextInstructionalOffering(context, new NavigationComparator());
    }

    public InstructionalOffering getPreviousInstructionalOffering(SessionContext context) {
    	return getPreviousInstructionalOffering(context, new NavigationComparator());
    }


    public InstructionalOffering getNextInstructionalOffering(SessionContext context, Comparator cmp) {
    	Long nextId = Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, getUniqueId());
    	if (nextId!=null) {
    		if (nextId.longValue()<0) return null;
    		return (new InstructionalOfferingDAO()).get(nextId);
    	}
    	InstructionalOffering next = null;
		SubjectArea area = getControllingCourseOffering().getSubjectArea();
		Iterator i = null;
		try {
		    i = area.getCourseOfferings().iterator();
		}
		catch (ObjectNotFoundException e) {
		    new _RootDAO().getSession().refresh(area);
		    i = area.getCourseOfferings().iterator();
		}
		for (;i.hasNext();) {
			CourseOffering c = (CourseOffering)i.next();
			if (!c.isIsControl().booleanValue()) continue;
			InstructionalOffering o = (InstructionalOffering)c.getInstructionalOffering();
    		if (!o.isNotOffered().equals(isNotOffered())) continue;
			if (cmp.compare(this, o)>=0) continue;
			if (next==null || cmp.compare(next,o)>0)
				next = o;
    	}
    	return next;
    }

    public InstructionalOffering getPreviousInstructionalOffering(SessionContext context, Comparator cmp) {
    	Long previousId = Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, getUniqueId());
    	if (previousId!=null) {
    		if (previousId.longValue()<0) return null;
    		return (new InstructionalOfferingDAO()).get(previousId);
    	}
    	InstructionalOffering previous = null;
		SubjectArea area = getControllingCourseOffering().getSubjectArea();
		Iterator i = null;
		try {
		    i = area.getCourseOfferings().iterator();
		}
		catch (ObjectNotFoundException e) {
		    new _RootDAO().getSession().refresh(area);
		    i = area.getCourseOfferings().iterator();
		}
		for (;i.hasNext();) {
			CourseOffering c = (CourseOffering)i.next();
			if (!c.isIsControl().booleanValue()) continue;
			InstructionalOffering o = (InstructionalOffering)c.getInstructionalOffering();
    		if (!o.isNotOffered().equals(isNotOffered())) continue;
			if (cmp.compare(this, o)<=0) continue;
			if (previous==null || cmp.compare(previous,o)<0)
				previous = o;
    	}
    	return previous;
    }

    /**
     * Remove config from offering
     * @param ioc Config
     */
    public void removeConfiguration(InstrOfferingConfig ioc) {
        Set configs = this.getInstrOfferingConfigs();
        for (Iterator i=configs.iterator(); i.hasNext(); ) {
            InstrOfferingConfig config = (InstrOfferingConfig) i.next();
            if (config.equals(ioc)) {
                i.remove();
                break;
            }
        }
    }

    /**
     * Checks if the config name already exists in the Instructional Offering
     * @param name Config Name
     * @param configId Config ID that is to be excluded from the search
     * @return true if exists/ false otherwise
     */
    public boolean existsConfig(String name, Long configId) {
        Set configs = this.getInstrOfferingConfigs();
        for (Iterator i=configs.iterator(); i.hasNext(); ) {
            InstrOfferingConfig config = (InstrOfferingConfig) i.next();
            if (config.getName()!=null && config.getName().equals(name.trim()) && !config.getUniqueId().equals(configId)) {
                return true;
            }
        }
        return false;
    }

    public static List findAll(Long sessionId) {
    	return (new InstructionalOfferingDAO()).
    		getSession().
    		createQuery("select distinct io from InstructionalOffering io where " +
    				"io.session.uniqueId=:sessionId").
    		setLong("sessionId",sessionId.longValue()).
    		list();
    }

    public void computeLabels(org.hibernate.Session hibSession) {
    	hibSession.flush();
    	for (Iterator i1=getInstrOfferingConfigs().iterator();i1.hasNext();) {
    		InstrOfferingConfig cfg = (InstrOfferingConfig)i1.next();
    		for (Iterator i2=cfg.getSchedulingSubparts().iterator();i2.hasNext();) {
    			SchedulingSubpart ss = (SchedulingSubpart)i2.next();
    			ss.setSchedulingSubpartSuffixCache(null); ss.getSchedulingSubpartSuffix(false);
    			hibSession.saveOrUpdate(ss);
    			for (Iterator i3=ss.getClasses().iterator();i3.hasNext();) {
    				Class_ c = (Class_)i3.next();
    				c.setSectionNumberCache(null); c.getSectionNumber(false);
    				hibSession.saveOrUpdate(c);
    			}
    		}
    	}
    }
    public CourseCreditUnitConfig getCredit(){
    	if(this.getCreditConfigs() == null || this.getCreditConfigs().size() != 1){
    		return(null);
    	} else {
    		return((CourseCreditUnitConfig)this.getCreditConfigs().iterator().next());
    	}
    }

    public void setCredit(CourseCreditUnitConfig courseCreditUnitConfig){
    	if (this.getCreditConfigs() == null || this.getCreditConfigs().size() == 0){
    		this.addTocreditConfigs(courseCreditUnitConfig);
    	} else if (!this.getCreditConfigs().contains(courseCreditUnitConfig)){
    		this.getCreditConfigs().clear();
    		this.getCreditConfigs().add(courseCreditUnitConfig);
    	} else {
    		//course already contains this config so we do not need to add it again.
    	}
    }


	/** Return number of classes of all of the given ITYPE */
	public int getNrClasses(ItypeDesc itype) {
		int ret = 0;
    	for (Iterator i=getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig cfg = (InstrOfferingConfig)i.next();
    		for (Iterator j=cfg.getSchedulingSubparts().iterator();j.hasNext();) {
    			SchedulingSubpart subpart = (SchedulingSubpart)j.next();
    			if (subpart.getItype().equals(itype))
    				ret += subpart.getClasses().size();
    		}
    	}
		return ret;
	}

	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getSessionId());
		} else {
			return(null);
		}
	}
	
	   public boolean hasGroupedClasses(){
    	if (this.getInstrOfferingConfigs() != null && this.getInstrOfferingConfigs().size() > 0){
    		InstrOfferingConfig ioc = null;
    		for(Iterator it = this.getInstrOfferingConfigs().iterator(); it.hasNext();){
    			ioc = (InstrOfferingConfig) it.next();
    			if (ioc.hasGroupedClasses()){
    				return(true);
    			}
    		}
    	}
    	return(false);
    }   
    
	/**
	 * Checks if IO has more than 1 config
	 * @return true / false
	 */
	public boolean hasMultipleConfigurations() {
		return (this.getInstrOfferingConfigs().size()>1);
	}
    
    public void generateInstrOfferingPermId() throws HibernateException {
        setInstrOfferingPermId((Integer)InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new InstructionalOfferingDAO().getSession(), this));
    }

	/**
	 * Checks if offering has unlimted enrollment
	 * @return
	 */
	public Boolean hasUnlimitedEnrollment() {
        for (Iterator i=getInstrOfferingConfigs().iterator();i.hasNext();) {
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		return Boolean.TRUE;
        	}
        }
		return Boolean.FALSE;
	}
    
    public boolean hasClasses() {
        for (Iterator i=getInstrOfferingConfigs().iterator();i.hasNext();) {
            InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
            if (ioc.hasClasses()) return true;
        }
        return false;
    }
    
	/**
	 * Delete all course offerings in the instructional offering
	 * @param hibSession
	 */
	public void deleteAllCourses(Session hibSession) {
        for (Iterator i = getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = (CourseOffering) i.next();
        	Event.deleteFromEvents(hibSession, co);
            Exam.deleteFromExams(hibSession, co);
        	hibSession.delete(co);
        }
	}
	
	public void cloneOfferingConfigurationFrom(InstructionalOffering instrOffrToCloneFrom){
		if (instrOffrToCloneFrom == null || instrOffrToCloneFrom.getInstrOfferingConfigs() == null){
			return;
		}
		if (getInstrOfferingConfigs() != null){
			getInstrOfferingConfigs().clear();
		}
		this.setNotOffered(instrOffrToCloneFrom.getNotOffered());
		InstrOfferingConfig origIoc = null;
		InstrOfferingConfig newIoc = null;
		for (Iterator iocIt = instrOffrToCloneFrom.getInstrOfferingConfigs().iterator(); iocIt.hasNext();){
			origIoc = (InstrOfferingConfig) iocIt.next();
			newIoc = (InstrOfferingConfig)origIoc.cloneWithSubparts();
			newIoc.setInstructionalOffering(this);
			if (!getControllingCourseOffering().getSubjectArea().getUniqueId().equals(instrOffrToCloneFrom.getControllingCourseOffering().getSubjectArea().getUniqueId())){
				Department controlDept = getControllingCourseOffering().getSubjectArea().getDepartment();
				SchedulingSubpart ss = null;
				Class_ c = null;
				if (newIoc.getSchedulingSubparts() != null){
					for (Iterator ssIt = newIoc.getSchedulingSubparts().iterator(); ssIt.hasNext();){
						ss = (SchedulingSubpart) ssIt.next();
						if (ss.getClasses() != null){
							for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
								c = (Class_) cIt.next();
								if (c.getManagingDept() != null 
										&& !c.getManagingDept().getUniqueId().equals(controlDept.getUniqueId()) 
										&& !c.getManagingDept().isExternalManager().booleanValue()){
									c.setManagingDept(controlDept);
									if(c.getClassInstructors() != null && !c.getClassInstructors().isEmpty()){
										DepartmentalInstructor di = null;
										ClassInstructor ci = null;
										List al = new ArrayList();
										al.addAll(c.getClassInstructors());
										for (Iterator ciIt = al.iterator(); ciIt.hasNext();){
											ci = (ClassInstructor) ciIt.next();
											di = DepartmentalInstructor.findByPuidDepartmentId(ci.getInstructor().getExternalUniqueId(), controlDept.getUniqueId());
											if (di != null){
												ci.getInstructor().getClasses().remove(ci);
												ci.setInstructor(di);
												di.addToclasses(ci);
											} else {
												c.getClassInstructors().remove(ci);
												ci.setClassInstructing(null);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			this.addToinstrOfferingConfigs(newIoc);
		}
	}

    public static InstructionalOffering findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return (InstructionalOffering)new InstructionalOfferingDAO().
            getSession().
            createQuery("select io from InstructionalOffering io where io.session.uniqueId=:sessionId and io.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom").
            setLong("sessionId", sessionId.longValue()).
            setLong("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }
    
    public Integer getProjectedDemand() {
    	int demand = 0;
    	for (Iterator<CourseOffering> i = getCourseOfferings().iterator(); i.hasNext(); ) {
    		CourseOffering course = i.next();
    		if (course.getProjectedDemand() != null) 
    			demand += course.getProjectedDemand();
    	}
    	return demand;
    }

}
