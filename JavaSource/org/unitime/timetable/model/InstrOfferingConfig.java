/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseInstrOfferingConfig;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.NavigationComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.security.SessionContext;




/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
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
	
	public String getCourseName(){
		return(this.getControllingCourseOffering().getCourseName());
	}

	public String getCourseNameWithTitle(){
		return(this.getControllingCourseOffering().getCourseNameWithTitle());
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
    
    public InstrOfferingConfig getNextInstrOfferingConfig(SessionContext context) {
    	return getNextInstrOfferingConfig(context, new NavigationComparator());
    }
    
    public InstrOfferingConfig getPreviousInstrOfferingConfig(SessionContext context) {
    	return getPreviousInstrOfferingConfig(context, new NavigationComparator());
    }

    public InstrOfferingConfig getNextInstrOfferingConfig(SessionContext context, Comparator cmp) {
    	InstrOfferingConfig next = null;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (cmp.compare(this,c)>=0) continue;
			if (next==null || cmp.compare(next,c)>0)
				next = c;
    	}
    	if (next!=null) return next;
    	InstructionalOffering nextIO = getInstructionalOffering().getNextInstructionalOffering(context, cmp);
    	if (nextIO==null) return null;
    	for (Iterator i=nextIO.getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (next==null || cmp.compare(next,c)>0) next = c;
    	}
    	return next;
    }
    
    public InstrOfferingConfig getPreviousInstrOfferingConfig(SessionContext context, Comparator cmp) {
    	InstrOfferingConfig previous = null;
    	for (Iterator i=getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    		InstrOfferingConfig c = (InstrOfferingConfig)i.next();
    		if (cmp.compare(this,c)<=0) continue;
			if (previous==null || cmp.compare(previous,c)<0)
				previous = c;
    	}
    	if (previous!=null) return previous;
    	InstructionalOffering previousIO = getInstructionalOffering().getPreviousInstructionalOffering(context, cmp);
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
