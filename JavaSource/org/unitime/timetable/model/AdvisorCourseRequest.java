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



import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.model.base.BaseAdvisorCourseRequest;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.AdvisorCriticalCourses;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "advisor_crsreq")
public class AdvisorCourseRequest extends BaseAdvisorCourseRequest implements Comparable<AdvisorCourseRequest> {
	private static final long serialVersionUID = 1L;

	public AdvisorCourseRequest() {
		super();
	}

	public boolean updatePreferences(RequestedCourse rc, org.hibernate.Session hibSession) {
    	List<AdvisorSectioningPref> remain = null;
    	boolean changed = false;
    	if (getPreferences() == null)
    		setPreferences(new HashSet<AdvisorSectioningPref>());
    	else
    		remain = new ArrayList<AdvisorSectioningPref>(getPreferences());
    	
    	if (rc != null && rc.hasSelectedClasses()) {
			p: for (Preference p: rc.getSelectedClasses()) {
				Class_ clazz = Class_DAO.getInstance().get(p.getId(), hibSession);
				if (clazz == null) continue;
				if (remain != null)
					for (Iterator<AdvisorSectioningPref> i = remain.iterator(); i.hasNext(); ) {
						AdvisorSectioningPref r = i.next();
						if (r instanceof AdvisorClassPref && ((AdvisorClassPref)r).getClazz().equals(clazz)) {
							i.remove();
							if (r.getRequired() != p.isRequired()) {
								r.setRequired(p.isRequired());
								hibSession.update(r);
								changed = true;
							}
							continue p;
						}
					}
				AdvisorClassPref scp = new AdvisorClassPref();
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
					for (Iterator<AdvisorSectioningPref> i = remain.iterator(); i.hasNext(); ) {
						AdvisorSectioningPref r = i.next();
						if (r instanceof AdvisorInstrMthPref && ((AdvisorInstrMthPref)r).getInstructionalMethod().equals(im)) {
							i.remove();
							if (r.getRequired() != p.isRequired()) {
								r.setRequired(p.isRequired());
								hibSession.update(r);
								changed = true;
							}
							continue p;
						}
					}
				AdvisorInstrMthPref imp = new AdvisorInstrMthPref();
				imp.setCourseRequest(this);
				imp.setRequired(p.isRequired());
				imp.setInstructionalMethod(im);
				imp.setLabel(im.getLabel());
				getPreferences().add(imp);
				changed = true;
			}
		}
    	if (remain != null) {
    		for (AdvisorSectioningPref p: remain) {
    			hibSession.delete(p);
    			getPreferences().remove(p);
    			changed = true;
    		}
    	}
    	return changed;
    }
	
	public int isCritical(CriticalCourses cc) {
		if (cc == null) return 0;
		if (cc instanceof AdvisorCriticalCourses) {
			return ((AdvisorCriticalCourses)cc).isCritical(this);
		}
		if (getCourseOffering() == null || isSubstitute()) return 0;
		return cc.isCritical(getCourseOffering());
	}
	
	@Transient
	public float getCreditMin() {
		if (getCredit() == null || getCredit().isEmpty()) return 0f;
		try {
			return Float.parseFloat(getCredit().replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (getCredit().contains("-")) {
			try {
				return Float.parseFloat(getCredit().substring(0, getCredit().indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}
	
	@Transient
	public float getCreditMax() {
		if (getCredit() == null || getCredit().isEmpty()) return 0f;
		try {
			return Float.parseFloat(getCredit().replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (getCredit().contains("-")) {
			try {
				return Float.parseFloat(getCredit().substring(1 + getCredit().indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}

	@Override
	public int compareTo(AdvisorCourseRequest r) {
		int cmp = getPriority().compareTo(r.getPriority());
		if (cmp != 0) return cmp;
		cmp = getAlternative().compareTo(r.getAlternative());
		if (cmp != 0) return cmp;
		return getUniqueId().compareTo(r.getUniqueId());
	}
	
	@Transient
	public Critical getEffectiveCritical() {
    	if (getCritical() != null)
    		return Critical.values()[getCritical()];
    	return Critical.NORMAL;
    }
    
	@Transient
    public boolean isCriticalOrImportant() {
    	switch (getEffectiveCritical()) {
    	case CRITICAL:
    		return true;
    	case IMPORTANT:
    		return true;
    	case VITAL:
    		return true;
    	default:
    		return false;
    	}
    }
}
