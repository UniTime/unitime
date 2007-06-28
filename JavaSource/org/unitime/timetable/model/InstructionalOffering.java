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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.impl.SessionImpl;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseInstructionalOffering;
import org.unitime.timetable.model.comparators.AcadAreaReservationComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.CourseReservationComparator;
import org.unitime.timetable.model.comparators.IndividualReservationComparator;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.comparators.PosReservationComparator;
import org.unitime.timetable.model.comparators.StudentGroupReservationComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao._RootDAO;
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

	/**
	 * Constructor for required fields
	 */
	public InstructionalOffering (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.Integer instrOfferingPermId,
		java.lang.Boolean notOffered,
		java.lang.Boolean designatorRequired) {

		super (
			uniqueId,
			session,
			instrOfferingPermId,
			notOffered,
			designatorRequired);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String getCourseName(){
		return(this.getControllingCourseOffering().getCourseName());
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


	/**
	 * Return the value associated with the column: instrOfferingConfigs
	 */
	public java.util.Set getInstrOfferingConfigs () {
		return super.getInstrOfferingConfigs();
	}

	public Department getDepartment() {
		return (this.getControllingCourseOffering().getDepartment());
	}


	public boolean isEditableBy(User user){
    	Debug.debug("Checking edit permission on: " + this.getCourseName());

    	if (user == null){
        	Debug.debug(" - Cannot Edit: User Info not found ");
    		return(false);
    	}
    	if (user.isAdmin()){
        	Debug.debug(" - Can Edit: User is admin");
    		return(true);
    	}

		if(this.getDepartment() == null){
			return(false);
    	} else {
    	    // Check if controlling course belongs to the user
    		if(!this.getControllingCourseOffering().isEditableBy(user)) {
            	Debug.debug(" - Cannot Edit: Controlling course owned by another user ");
    			return false;
    		}

//    		// Check if cross-listed with a course in a different department
//    		for (Iterator i=this.getCourseOfferings().iterator();i.hasNext();) {
//    		    CourseOffering co = (CourseOffering) i.next();
//    		    if(!co.isEditableBy(user)) {
//                	Debug.debug(" - Cannot Edit: Cross listed with a course in a different department. ");
//    		        return false;
//    		    }
//    		}

        	Debug.debug(" - Can Edit.");
    		return true;
    	}
     }

    public boolean isViewableBy(User user){
    	if(user == null){
    		return(false);
    	}
    	if (user.isAdmin()){
    		return(true);
    	}
    	if (isEditableBy(user)){
    		return(true);
    	}
    	if(this.getCourseOfferings() != null && this.getCourseOfferings().size() > 0){
    		Iterator it = this.getCourseOfferings().iterator();
    		CourseOffering co = null;
    		while(it.hasNext()){
    			co = (CourseOffering) it.next();
    			if (co.isViewableBy(user)){
    				return(true);
    			}
    		}
    	}
		return(false);
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
	public static TreeSet search(
	        Long acadSessionId,
	        String subjectAreaId,
	        String courseNbr,
	        boolean fetchStructure,
	        boolean fetchCredits,
	        boolean fetchInstructors,
	        boolean fetchPreferences,
	        boolean fetchAssignments,
	        boolean fetchReservations) {

		org.hibernate.Session hibSession = (new InstructionalOfferingDAO()).getSession();
		hibSession.clear();

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
			query.append("left join fetch ioc.courseReservations as cr ");
		}

		query.append(" where io.session.uniqueId=:sessionId ");

		if (courseNbr != null && courseNbr.length() > 0){
		    query.append(" and co.courseNbr ");
		    if (courseNbr.indexOf('*')>=0) {
	            query.append(" like '");
	            courseNbr = courseNbr.replace('*', '%').toUpperCase();
		    }
		    else {
	            query.append(" = '");
		    }
            query.append(courseNbr.toUpperCase());
            query.append("'  ");
        }

		query.append(" and co.subjectArea.uniqueId = :subjectAreaId ");

		Query q = hibSession.createQuery(query.toString());
		q.setFetchSize(1000);
		q.setInteger("subjectAreaId", Integer.parseInt(subjectAreaId));
		q.setLong("sessionId", acadSessionId.longValue());
		q.setCacheable(true);


        TreeSet ts = new TreeSet(new InstructionalOfferingComparator(Long.valueOf(subjectAreaId)));

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

                    // Delete class
                    hibSession.delete(c);
                }

                // Delete set of classes
                tSp.getClasses().clear();
            }
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

    public InstructionalOffering getNextInstructionalOffering(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getNextInstructionalOffering(session, new NavigationComparator(), user, canEdit, canView);
    }

    public InstructionalOffering getPreviousInstructionalOffering(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getPreviousInstructionalOffering(session, new NavigationComparator(), user, canEdit, canView);
    }


    public InstructionalOffering getNextInstructionalOffering(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
    	Long nextId = Navigation.getNext(session, Navigation.sInstructionalOfferingLevel, getUniqueId());
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
    		//if (canEdit && !o.isEditableBy(user)) continue;
    		//if (canView && !o.isViewableBy(user)) continue;
    		if (!o.isNotOffered().equals(isNotOffered())) continue;
			if (cmp.compare(this, o)>=0) continue;
			if (next==null || cmp.compare(next,o)>0)
				next = o;
    	}
    	return next;
    }

    public InstructionalOffering getPreviousInstructionalOffering(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
    	Long previousId = Navigation.getPrevious(session, Navigation.sInstructionalOfferingLevel, getUniqueId());
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
    		//if (canEdit && !o.isEditableBy(user)) continue;
    		//if (canView && !o.isViewableBy(user)) continue;
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


    /**
     * Returns a list containing all the types of reservations for an Instructional
     * Offering in the order: Individual, Group, Acad Area, POS, Course Offering
     * @param individual include individual reservations
     * @param studentGroup include student group reservations
     * @param acadArea include academic area reservations
     * @param pos include pos reservations
     * @param course include course reservations
     * @return collection of reservations (collection is empty is none found)
     */
    public Collection getReservations(
            boolean individual, boolean studentGroup, boolean acadArea, boolean pos, boolean course ) {

        Collection resv = new Vector();

        if (individual && this.getIndividualReservations()!=null) {
            List c = new Vector(this.getIndividualReservations());
            Collections.sort(c, new IndividualReservationComparator());
            resv.addAll(c);
        }
        if (studentGroup && this.getStudentGroupReservations()!=null) {
            List c = new Vector(this.getStudentGroupReservations());
            Collections.sort(c, new StudentGroupReservationComparator());
            resv.addAll(c);
        }
        if (acadArea && this.getAcadAreaReservations()!=null) {
            List c = new Vector(this.getAcadAreaReservations());
            Collections.sort(c, new AcadAreaReservationComparator());
            resv.addAll(c);
        }
        if (pos && this.getPosReservations()!=null) {
            List c = new Vector(this.getPosReservations());
            Collections.sort(c, new PosReservationComparator());
            resv.addAll(c);
        }
        if (course && this.getCourseReservations()!=null) {
            List c = new Vector(this.getCourseReservations());
            Collections.sort(c, new CourseReservationComparator());
            resv.addAll(c);
        }

        return resv;
    }

    /**
     * Returns effective reservations for the class
     * @param individual include individual reservations
     * @param studentGroup include student group reservations
     * @param acadArea include academic area reservations
     * @param pos include pos reservations
     * @param course include course reservations
     * @return collection of reservations (collection is empty is none found)
     */
    public Collection effectiveReservations(
            boolean individual, boolean studentGroup, boolean acadArea, boolean pos, boolean course ) {
        //TODO hfernan - effective reservations - if applicable
        return getReservations( individual, studentGroup, acadArea, pos, course );
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
        setInstrOfferingPermId((Integer)InstrOfferingPermIdGenerator.getGenerator().generate((SessionImpl)new InstructionalOfferingDAO().getSession(), this));
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
     * Delete all resrvations
     * @param hibSession
     * @throws Exception
     */
    public void deleteAllReservations(
    		org.hibernate.Session hibSession) throws Exception{
    	
	        // Remove all academic area reservations
	        for (Iterator i = getCourseOfferings().iterator(); i.hasNext(); ) {
	        	CourseOffering co = (CourseOffering) i.next();
	            Set acadResvs = co.getAcadAreaReservations();
	            if (acadResvs!=null) {
	                for (Iterator resvIter=acadResvs.iterator(); resvIter.hasNext();) {
	                    AcadAreaReservation ar = (AcadAreaReservation) resvIter.next();
	                    resvIter.remove();
	                    hibSession.delete(ar);
	                }
	            }
	        }            

	        // Remove all course reservations
            Set ioResv = getCourseReservations();
            for (Iterator iterR=ioResv.iterator(); iterR.hasNext(); ) {
                CourseOfferingReservation resv = (CourseOfferingReservation) iterR.next();
                iterR.remove();
                hibSession.delete(resv);
            }
    }

	/**
	 * Delete all course offerings in the instructional offering
	 * @param hibSession
	 */
	public void deleteAllCourses(Session hibSession) {
        for (Iterator i = getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = (CourseOffering) i.next();
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
		InstrOfferingConfig origIoc = null;
		InstrOfferingConfig newIoc = null;
		for (Iterator iocIt = instrOffrToCloneFrom.getInstrOfferingConfigs().iterator(); iocIt.hasNext();){
			origIoc = (InstrOfferingConfig) iocIt.next();
			newIoc = (InstrOfferingConfig)origIoc.cloneWithSubparts();
			newIoc.setInstructionalOffering(this);
			this.addToinstrOfferingConfigs(newIoc);
		}
	}
}