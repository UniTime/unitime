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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.base.BaseCourseRequest;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "course_request")
public class CourseRequest extends BaseCourseRequest implements Comparable {
	private static final long serialVersionUID = 1L;
	
	public static enum CourseRequestOverrideStatus {
		PENDING, APPROVED, REJECTED, CANCELLED, NOT_CHECKED, NOT_NEEDED,
	}
	
	public static enum CourseRequestOverrideIntent {
		REGISTER, // pre-registration
		ADD, DROP, CHANGE, // open registration
		EX_ADD, EX_DROP, EX_CHANGE, // extended registration
		WAITLIST, // wait-list
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseRequest () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseRequest (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof CourseRequest)) return -1;
        CourseRequest cr = (CourseRequest)o;
        int cmp = getOrder().compareTo(cr.getOrder());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(cr.getUniqueId() == null ? -1 : cr.getUniqueId());
    }
    
	@Transient
    public List<StudentClassEnrollment> getClassEnrollments() {
    	List<StudentClassEnrollment> ret = new ArrayList<StudentClassEnrollment>();
    	for (StudentClassEnrollment e: getCourseDemand().getStudent().getClassEnrollments()) {
			if (getCourseOffering().equals(e.getCourseOffering()))
				ret.add(e);
    	}
    	return ret;
    }
    
    public CourseRequestOption getCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType type) {
    	if (getCourseRequestOptions() == null) return null;
    	for (CourseRequestOption option: getCourseRequestOptions())
    		if (type.equals(option.getType())) return option;
    	return null;
    }
    
    public void setCourseRequestOption(OnlineSectioningLog.CourseRequestOption option) {
    	if (getCourseRequestOptions() == null) {
    		setCourseRequestOptions(new HashSet<CourseRequestOption>());
    	}
    	CourseRequestOption o = getCourseRequestOption(option.getType());
    	if (o == null) {
    		o = new CourseRequestOption();
			o.setCourseRequest(this);
			o.setOption(option);
			getCourseRequestOptions().add(o);
    	} else {
    		o.setOption(option);
    	}
    }
    
    public void clearCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType type) {
    	if (getCourseRequestOptions() == null) return;
    	for (Iterator<CourseRequestOption> i = getCourseRequestOptions().iterator(); i.hasNext(); ) {
    		CourseRequestOption option = i.next();
    		if (type == null || type.equals(option.getType())) i.remove();
    	}
    }
    
    public void updateCourseRequestOption(OnlineSectioningLog.CourseRequestOption.OptionType type, OnlineSectioningLog.CourseRequestOption.Builder option) {
    	if (getCourseRequestOptions() == null)
    		setCourseRequestOptions(new HashSet<CourseRequestOption>());
    	for (Iterator<CourseRequestOption> i = getCourseRequestOptions().iterator(); i.hasNext(); ) {
    		CourseRequestOption o = i.next();
    		if (type.equals(o.getType())) {
    			if (option == null) {
    				i.remove();
    			} else {
    				o.setOption(option.build());
    			}
    			return;
    		}
    	}
    	if (option != null) {
        	CourseRequestOption o = new CourseRequestOption();
        	o.setCourseRequest(this);
    		o.setOption(option.build());
    		getCourseRequestOptions().add(o);
    	}
    }
    
	@Transient
    public CourseRequestOverrideStatus getCourseRequestOverrideStatus() {
    	if (getOverrideStatus() == null) return CourseRequestOverrideStatus.APPROVED;
    	return CourseRequestOverrideStatus.values()[getOverrideStatus()];
    }
    
    public void setCourseRequestOverrideStatus(CourseRequestOverrideStatus status) {
    	setOverrideStatus(status == null ? null : Integer.valueOf(status.ordinal()));
    }
    
    public void setCourseRequestOverrideIntent(CourseRequestOverrideIntent intent) {
    	if (intent == null)
    		setOverrideIntent(null);
    	else
    		setOverrideIntent(intent.ordinal());
    }
    
	@Transient
    public CourseRequestOverrideIntent getCourseRequestOverrideIntent() {
    	return (getOverrideIntent() == null ? null : CourseRequestOverrideIntent.values()[getOverrideIntent()]); 
    }
    
	@Transient
    public boolean isRequestApproved() {
    	return getOverrideStatus() == null || getOverrideStatus().intValue() == CourseRequestOverrideStatus.APPROVED.ordinal();
    }
    
	@Transient
    public boolean isRequestPending() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.PENDING.ordinal();
    }
    
	@Transient
    public boolean isRequestCancelled() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.CANCELLED.ordinal();
    }
    
	@Transient
    public boolean isRequestNeeded() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.NOT_CHECKED.ordinal();
    }
    
	@Transient
    public boolean isRequestNotNeeded() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.NOT_NEEDED.ordinal();
    }
    
	@Transient
    public boolean isRequestRejected() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.REJECTED.ordinal();
    }
    
    public boolean updatePreferences(RequestedCourse rc, org.hibernate.Session hibSession) {
    	List<StudentSectioningPref> remain = null;
    	boolean changed = false;
    	if (getPreferences() == null)
    		setPreferences(new HashSet<StudentSectioningPref>());
    	else
    		remain = new ArrayList<StudentSectioningPref>(getPreferences());
    	
    	if (rc != null && rc.hasSelectedClasses()) {
			p: for (Preference p: rc.getSelectedClasses()) {
				Class_ clazz = Class_DAO.getInstance().get(p.getId(), hibSession);
				if (clazz == null) continue;
				if (remain != null)
					for (Iterator<StudentSectioningPref> i = remain.iterator(); i.hasNext(); ) {
						StudentSectioningPref r = i.next();
						if (r instanceof StudentClassPref && ((StudentClassPref)r).getClazz().equals(clazz)) {
							i.remove();
							if (r.getRequired() != p.isRequired()) {
								r.setRequired(p.isRequired());
								hibSession.merge(r);
								changed = true;
							}
							continue p;
						}
					}
				StudentClassPref scp = new StudentClassPref();
				scp.setCourseRequest(this);
				scp.setRequired(p.isRequired());
				scp.setClazz(clazz);
				scp.setLabel(clazz.getClassPrefLabel(getCourseOffering()));
				getPreferences().add(scp);
				changed = true;
			}
		}
		if (rc != null && rc.hasSelectedIntructionalMethods()) {
			p: for (Preference p: rc.getSelectedIntructionalMethods()) {
				InstructionalMethod im = InstructionalMethodDAO.getInstance().get(p.getId(), hibSession);
				if (im == null) continue;
				if (remain != null)
					for (Iterator<StudentSectioningPref> i = remain.iterator(); i.hasNext(); ) {
						StudentSectioningPref r = i.next();
						if (r instanceof StudentInstrMthPref && ((StudentInstrMthPref)r).getInstructionalMethod().equals(im)) {
							i.remove();
							if (r.getRequired() != p.isRequired()) {
								r.setRequired(p.isRequired());
								hibSession.merge(r);
								changed = true;
							}
							continue p;
						}
					}
				StudentInstrMthPref imp = new StudentInstrMthPref();
				imp.setCourseRequest(this);
				imp.setRequired(p.isRequired());
				imp.setInstructionalMethod(im);
				imp.setLabel(im.getLabel());
				getPreferences().add(imp);
				changed = true;
			}
		}
    	if (remain != null) {
    		for (StudentSectioningPref p: remain) {
    			hibSession.remove(p);
    			getPreferences().remove(p);
    			changed = true;
    		}
    	}
    	return changed;
    }
    
	@Transient
    public boolean isRequired() {
    	Set<StudentSectioningPref> prefs = getPreferences(); 
    	if (prefs == null || prefs.isEmpty()) return true;
    	for (StudentClassEnrollment e: getCourseDemand().getStudent().getClassEnrollments()) {
    		if (!e.getCourseOffering().equals(getCourseOffering())) continue;
    		
    		Class_ section = e.getClazz();
    		InstrOfferingConfig config = section.getSchedulingSubpart().getInstrOfferingConfig();
    		
            boolean hasConfig = false, hasMatchingConfig = false;
            boolean hasSubpart = false, hasMatchingSection = false;
            boolean hasSectionReq = false;
            for (StudentSectioningPref choice: prefs) {
            	// only check required choices
            	if (!choice.isRequired()) continue;

                // has config -> check config
            	if (choice instanceof StudentInstrMthPref) {
            		StudentInstrMthPref imp = (StudentInstrMthPref) choice;
            		hasConfig = true;
            		if (imp.getInstructionalMethod().equals(config.getEffectiveInstructionalMethod()))
            			hasMatchingConfig = true;
            	}

            	// has section of the matching subpart -> check section
            	if (choice instanceof StudentClassPref) {
            		StudentClassPref cp = (StudentClassPref)choice;
            		Class_ reqSection = cp.getClazz();
                    hasSectionReq = true;
                    if (reqSection.getSchedulingSubpart().equals(section.getSchedulingSubpart())) {
                        hasSubpart = true;
                        if (reqSection.equals(section)) hasMatchingSection = true;
                    } else if (!hasMatchingConfig) {
                        for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
                            if (reqSection.getSchedulingSubpart().equals(subpart)) {
                                hasMatchingConfig = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (hasConfig && !hasMatchingConfig) return false;
            if (hasSubpart && !hasMatchingSection) return false;
            // no match, but there are section requirements for a different config -> not satisfied 
            if (!hasMatchingConfig && !hasMatchingSection && hasSectionReq) return false;
        }
        return true;
    }
}
