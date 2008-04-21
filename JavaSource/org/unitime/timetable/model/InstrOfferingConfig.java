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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseInstrOfferingConfig;
import org.unitime.timetable.model.comparators.AcadAreaReservationComparator;
import org.unitime.timetable.model.comparators.CourseReservationComparator;
import org.unitime.timetable.model.comparators.IndividualReservationComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.comparators.PosReservationComparator;
import org.unitime.timetable.model.comparators.StudentGroupReservationComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.util.Constants;




public class InstrOfferingConfig extends BaseInstrOfferingConfig {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public InstrOfferingConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public InstrOfferingConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public InstrOfferingConfig (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.InstructionalOffering instructionalOffering,
		java.lang.Integer limit,
		java.lang.Boolean unlimitedEnrollment) {

		super (
			uniqueId,
			instructionalOffering,
			limit,
			unlimitedEnrollment);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public Department getDepartment() {
 		return (this.getInstructionalOffering().getDepartment());
	}

	public Session getSession() {
		return (this.getInstructionalOffering().getSession());
	}
	
	public Long getSessionId() {
		return (this.getInstructionalOffering().getSessionId());
	}
	
	public Vector toSimpleItypeConfig(User user) throws Exception{
	    
	    Vector sp = new Vector();
        Set subparts = getSchedulingSubparts();
        Iterator iterSp = subparts.iterator();
        
        // Loop through subparts
        while (iterSp.hasNext()) {
            SchedulingSubpart subpart = (SchedulingSubpart) iterSp.next();
            
            // Select top most subparts only
            if(subpart.getParentSubpart()!=null) continue;
            
            // Process each subpart
            SimpleItypeConfig sic = toSimpleItypeConfig(user, this, subpart);
            sp.addElement(sic);
        }
	    
        return sp;
	}

    /**
     * Read persistent class InstrOfferingConfig and convert it to a 
     * representation that can be displayed
     * @param config InstrOfferingConfig object
     * @param subpart Scheduling subpart
     * @return SimpleItypeConfig object representing the subpart
     * @throws Exception
     */
    private SimpleItypeConfig toSimpleItypeConfig (
            User user,
            InstrOfferingConfig config, 
            SchedulingSubpart subpart) throws Exception {
        
        ItypeDesc itype = subpart.getItype();
        SimpleItypeConfig sic = new SimpleItypeConfig(itype);
        
        boolean isDisabled = setSicProps(user, config, subpart, sic);

        Set s = subpart.getChildSubparts();
        Iterator iter = s.iterator();
        Vector v = new Vector();
        while(iter.hasNext()) {
            SchedulingSubpart child = (SchedulingSubpart) iter.next();
            SimpleItypeConfig childSic = toSimpleItypeConfig(user, config, child);
            boolean isDisabledChild = setSicProps(user, config, child, childSic);
            sic.addSubpart(childSic);            
            if(isDisabledChild)
                isDisabled = true;
        }
        
        if (isDisabled)
            sic.setDisabled(true);
        
        return sic;        	
    }   

    /**
     * Sets the class limit, min per wk and num classes properties 
     * @param config InstrOfferingConfig object
     * @param subpart Scheduling subpart
     * @return SimpleItypeConfig object representing the subpart
     */
    private boolean setSicProps(
            User user,
            InstrOfferingConfig config,
            SchedulingSubpart subpart,
            SimpleItypeConfig sic ) {
        
        int mnlpc = subpart.getMinClassLimit();
        int mxlpc = subpart.getMaxClassLimit();
        int mpw = subpart.getMinutesPerWk().intValue();
        int numClasses = subpart.getNumClasses();
        int numRooms = subpart.getMaxRooms();
        float rc = subpart.getMaxRoomRatio();
        long md = subpart.getManagingDept().getUniqueId().longValue(); 
        boolean mixedManaged = subpart.hasMixedManagedClasses();
        
        if(mnlpc<0) 
            mnlpc = config.getLimit().intValue();
        if(mxlpc<0) 
            mxlpc = mnlpc;
        if(numClasses<0)
            numClasses = 0;
        if (mixedManaged) 
            md = Constants.MANAGED_BY_MULTIPLE_DEPTS;
        
        sic.setMinLimitPerClass(mnlpc);
        sic.setMaxLimitPerClass(mxlpc);
        sic.setMinPerWeek(mpw);
        sic.setNumClasses(numClasses);
        sic.setNumRooms(numRooms);
        sic.setRoomRatio(rc);
        sic.setSubpartId(subpart.getUniqueId().longValue());
        sic.setManagingDeptId(md);
        
        // Check Permissions on subpart
        if (!subpart.isEditableBy(user) || mixedManaged) {
                sic.setDisabled(true);
                sic.setNotOwned(true);
                return true;
        }
        
        return false;
    }
    
	public String getCourseName(){
		return(this.getControllingCourseOffering().getCourseName());
	}

	public String getCourseNameWithTitle(){
		return(this.getControllingCourseOffering().getCourseNameWithTitle());
	}

	public boolean isEditableBy(User user){
    	if (user == null){
    		return(false);
    	}
    	if (user.isAdmin()){
    		return(true);
    	} 
		if (this.getInstructionalOffering().isEditableBy(user)){
			return(true);
		}
		if (this.getSchedulingSubparts() != null && this.getSchedulingSubparts().size() > 0){
			boolean canEdit = true;
			SchedulingSubpart ss = null;
			Iterator it = this.getSchedulingSubparts().iterator();
			while(canEdit && it.hasNext()){
				ss = (SchedulingSubpart) it.next();
				if (!ss.isEditableBy(user)){
					canEdit = false;
				}
			}
			if (canEdit){
				return(true);
			}
		}
		return(false);
     }
    public boolean isViewableBy(User user){
    	if (user == null){
    		return(false);
    	}
    	if (user.isAdmin()){
    		return(true);
    	} 
		if (this.getInstructionalOffering().isViewableBy(user)){
			return(true);
		}
		return(false);
     }
    
    /**
     * Check if config has atleast one externally managed subpart
     * @param user
     * @param checkClasses checks classes as well for externally managed flags
     * @return
     */
    public boolean hasExternallyManagedSubparts(User user, boolean checkClasses) {
		if (this.getSchedulingSubparts() != null && this.getSchedulingSubparts().size() > 0){
			Iterator it = this.getSchedulingSubparts().iterator();
			while(it.hasNext()){
			    SchedulingSubpart ss = (SchedulingSubpart) it.next();
				if (ss.isEditableBy(user) && ss.getManagingDept().isExternalManager().booleanValue()){
				    return true;
				}
				if (checkClasses && ss.hasExternallyManagedClasses(user))
				    return true;
			}
		}
        return false;
    }
    
	public CourseOffering getControllingCourseOffering() {
	       return(this.getInstructionalOffering().getControllingCourseOffering());
		}

    /**
     * Check if a config has at least one class
     * @return true if at least one class exists, false otherwise 
     */
    public boolean hasClasses() {
        Set subparts = this.getSchedulingSubparts();
    	if ( subparts!= null && !subparts.isEmpty()){
    	    for (Iterator i=subparts.iterator(); i.hasNext(); ) {
        		SchedulingSubpart ss = (SchedulingSubpart) i.next();
        		if (ss.getClasses()!= null && !ss.getClasses().isEmpty()){
        			return true;
        		}
    	    }
    	}
        return false;
    }
    
    public int getFirstSectionNumber(ItypeDesc itype) {
    	if (getInstructionalOffering().getInstrOfferingConfigs().size()<=1) return 1;
    	Comparator cmp = new InstrOfferingConfigComparator(null);
    	int ret = 1;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig cfg = (InstrOfferingConfig)i.next();
    		int size = 0;
    		if (cmp.compare(cfg,this)>=0) continue;
    		for (Iterator j=cfg.getSchedulingSubparts().iterator();j.hasNext();) {
    			SchedulingSubpart subpart = (SchedulingSubpart)j.next();
    			if (!subpart.getItype().equals(itype)) continue;
    			size = Math.max(size, subpart.getClasses().size());
    		}
    		ret += size;
    	}
    	return ret;
    }

    /**
     * Add subpart to config
     * @param schedulingSubpart
     */
	public void addToschedulingSubparts (SchedulingSubpart schedulingSubpart) {
		if (null == getSchedulingSubparts()) setSchedulingSubparts(new HashSet());
		getSchedulingSubparts().add(schedulingSubpart);
	}

    /**
     * Returns a list containing all the types of reservations for a Class
     * in the order: Individual, Group, Acad Area, POS, Course Offering
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
     * Returns effective reservations for the config
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
    
    public String getName() {
    	String name = super.getName();
    	if (name!=null && name.length()>0) return name;
    	if (getInstructionalOffering()==null) return null;
    	if (getUniqueId()==null) return String.valueOf(getInstructionalOffering().getInstrOfferingConfigs().size()+1);
    	int idx = 0;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (c.getUniqueId().compareTo(getUniqueId())<0) idx++;
    	}
    	return String.valueOf(idx+1);
    }

    /**
     * Gets first unused number as the config name for the specified instr offering
     * @param io Instructional Offering
     * @return null if io is null. Name starting with number 1 as the first 
     */
    public static String getGeneratedName(InstructionalOffering io) {
        if (io==null) return null;
        int idx = 1;
        HashMap idxes = new HashMap();
    	for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		idxes.put(c.getName(), c.getName());
    	}
        for (;;) {
            if (idxes.get(""+idx)==null) break;
            ++idx;
        }
        return ""+idx;
    }
    
    public InstrOfferingConfig getNextInstrOfferingConfig(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getNextInstrOfferingConfig(session, new NavigationComparator(), user, canEdit, canView);
    }
    
    public InstrOfferingConfig getPreviousInstrOfferingConfig(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getPreviousInstrOfferingConfig(session, new NavigationComparator(), user, canEdit, canView);
    }

    public InstrOfferingConfig getNextInstrOfferingConfig(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
    	InstrOfferingConfig next = null;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (cmp.compare(this,c)>=0) continue;
			if (next==null || cmp.compare(next,c)>0)
				next = c;
    	}
    	if (next!=null) return next;
    	InstructionalOffering nextIO = getInstructionalOffering().getNextInstructionalOffering(session, cmp, user, canEdit, canView);
    	if (nextIO==null) return null;
    	for (Iterator i=nextIO.getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (next==null || cmp.compare(next,c)>0) next = c;
    	}
    	return next;
    }
    
    public InstrOfferingConfig getPreviousInstrOfferingConfig(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
    	InstrOfferingConfig previous = null;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (cmp.compare(this,c)<=0) continue;
			if (previous==null || cmp.compare(previous,c)<0)
				previous = c;
    	}
    	if (previous!=null) return previous;
    	InstructionalOffering previousIO = getInstructionalOffering().getPreviousInstructionalOffering(session, cmp, user, canEdit, canView);
    	if (previousIO==null) return null;
    	for (Iterator i=previousIO.getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (previous==null || cmp.compare(previous,c)<0) previous = c;
    	}
    	return previous;
    }
    
    public String toString() {
        return getCourseName()+" ["+getName()+"]";
    }
	
    public boolean hasGroupedClasses(){
    	if (this.getSchedulingSubparts() != null && this.getSchedulingSubparts().size() > 0){
    		SchedulingSubpart ss = null;
    		for(Iterator it = this.getSchedulingSubparts().iterator(); it.hasNext();){
    			ss = (SchedulingSubpart) it.next();
    			if (ss.getParentSubpart() != null){
    				if (!ss.getParentSubpart().getItype().getItype().equals(ss.getItype().getItype())){
    					return(true);
    				} else {
    					Class_ c = null;
    					for(Iterator cIt = ss.getParentSubpart().getClasses().iterator(); cIt.hasNext();){
    						c = (Class_)cIt.next();
    						if (c.isOddOrEvenWeeksOnly()){
    							return(true);
    						}
    					}
    					for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
    						c = (Class_)cIt.next();
    						if (c.isOddOrEvenWeeksOnly()){
    							return(true);
    						}
    					}
    				}
    			}
    		}
    	}
    	return(false);
    }
    
    public Object clone(){
    	InstrOfferingConfig newInstrOffrConfig = new InstrOfferingConfig();
    	newInstrOffrConfig.setLimit(getLimit());
    	newInstrOffrConfig.setName(getName());
    	newInstrOffrConfig.setUnlimitedEnrollment(isUnlimitedEnrollment());
    	return(newInstrOffrConfig);
    }
    
    private void setSubpartConfig(SchedulingSubpart schedulingSubpart, InstrOfferingConfig instrOffrConfig){
    	schedulingSubpart.setInstrOfferingConfig(instrOffrConfig);
    	instrOffrConfig.addToschedulingSubparts(schedulingSubpart);
    	if (schedulingSubpart.getChildSubparts() != null){
    		SchedulingSubpart childSubpart = null;
    		for (Iterator cssIt = schedulingSubpart.getChildSubparts().iterator(); cssIt.hasNext();){
    			childSubpart = (SchedulingSubpart) cssIt.next();
    			setSubpartConfig(childSubpart, instrOffrConfig);
    		}
    	}
    }
    public Object cloneWithSubparts(){
    	InstrOfferingConfig newInstrOffrConfig = (InstrOfferingConfig)clone();
    	if (getSchedulingSubparts() != null){
    		SchedulingSubpart origSubpart = null;
    		SchedulingSubpart newSubpart = null;
    		for (Iterator ssIt = getSchedulingSubparts().iterator(); ssIt.hasNext();){
    			origSubpart = (SchedulingSubpart) ssIt.next();
    			if (origSubpart.getParentSubpart() == null){
    				newSubpart = (SchedulingSubpart)origSubpart.cloneDeep();
    				setSubpartConfig(newSubpart, newInstrOffrConfig);
    			}
    		}
    	}
    	return(newInstrOffrConfig);
    }
    
    public static InstrOfferingConfig findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
        return (InstrOfferingConfig)new InstrOfferingConfigDAO().
            getSession().
            createQuery("select ioc from InstrOfferingConfig ioc where ioc.instructionalOffering.session.uniqueId=:sessionId and ioc.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom").
            setLong("sessionId", sessionId.longValue()).
            setLong("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            uniqueResult();
    }

}