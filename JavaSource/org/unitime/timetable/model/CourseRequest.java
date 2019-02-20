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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.base.BaseCourseRequest;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;



/**
 * @author Tomas Muller
 */
public class CourseRequest extends BaseCourseRequest implements Comparable {
	private static final long serialVersionUID = 1L;
	
	public static enum CourseRequestOverrideStatus {
		PENDING, APPROVED, REJECTED, CANCELLED,
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
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cr.getUniqueId() == null ? -1 : cr.getUniqueId());
    }
    
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
    
    public CourseRequestOverrideStatus getCourseRequestOverrideStatus() {
    	if (getOverrideStatus() == null) return CourseRequestOverrideStatus.APPROVED;
    	return CourseRequestOverrideStatus.values()[getOverrideStatus()];
    }
    
    public void setCourseRequestOverrideStatus(CourseRequestOverrideStatus status) {
    	setOverrideStatus(status == null ? null : new Integer(status.ordinal()));
    }
    
    public boolean isRequestApproved() {
    	return getOverrideStatus() == null || getOverrideStatus().intValue() == CourseRequestOverrideStatus.APPROVED.ordinal();
    }
    
    public boolean isRequestPending() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.PENDING.ordinal();
    }
    
    public boolean isRequestCancelled() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.CANCELLED.ordinal();
    }
    
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
								hibSession.update(r);
								changed = true;
							}
							continue p;
						}
					}
				StudentClassPref scp = new StudentClassPref();
				scp.setCourseRequest(this);
				scp.setRequired(p.isRequired());
				scp.setClazz(clazz);
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
								hibSession.update(r);
								changed = true;
							}
							continue p;
						}
					}
				StudentInstrMthPref imp = new StudentInstrMthPref();
				imp.setCourseRequest(this);
				imp.setRequired(p.isRequired());
				imp.setInstructionalMethod(im);
				getPreferences().add(imp);
				changed = true;
			}
		}
    	if (remain != null) {
    		for (StudentSectioningPref p: remain) {
    			hibSession.delete(p);
    			getPreferences().remove(p);
    			changed = true;
    		}
    	}
    	return changed;
    }
}
