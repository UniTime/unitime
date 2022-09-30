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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * @author Zuzana Mullerova, Tomas Muller
 */
public class CrossListsModifyForm implements UniTimeForm {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
    // --------------------------------------------------------- Instance Variables

	private static final long serialVersionUID = 3638385556572422628L;
	private String op;   
    private Long subjectAreaId;
    private Long instrOfferingId;
    private Long addCourseOfferingId;
    private Long ctrlCrsOfferingId;
    private String instrOfferingName;
    private Boolean ownedInstrOffr;
    private List<Long> originalOfferings;
    private List<Long> courseOfferingIds;
    private List<String> courseOfferingNames;
    private List<Boolean> ownedCourse;

    private List<String> resvId;
    private List<Integer> limits;
    private List<Integer> requested;
    private List<Integer> projected;
    private List<Integer> lastTerm;
    private List<Boolean> canDelete;
    
	private Integer ioLimit;
	private Boolean unlimited;
	
    private Long readOnlyCrsOfferingId;

    /** Factory to create dynamic list element for Course Offerings */
    protected DynamicListObjectFactory<String> factoryCourseOfferings;
    
    public CrossListsModifyForm() {
    	factoryCourseOfferings = new DynamicListObjectFactory<String>() {
            public String create() {
                return new String("");
            }
        };
        reset();
    }


    @Override
    public void validate(UniTimeAction action) {
        if (op.equals(MSG.actionAddCourseToCrossList())) {
            // Check Added Course
	        if (this.addCourseOfferingId==null || this.addCourseOfferingId.intValue()<=0) {
	        	action.addFieldError("form.addCourseOfferingId", MSG.errorRequiredCourseOffering());            
	        }
        }
        
        if (op.equals(MSG.actionUpdateCrossLists())) {
	        // Check controlling course
	        if (this.ctrlCrsOfferingId==null || this.ctrlCrsOfferingId.intValue()<=0) {
	        	action.addFieldError("form.ctrlCrsOfferingId", MSG.errorRequiredControllingCourse());            
	        }
        }
    }

    @Override
    public void reset() {
        subjectAreaId = null;
        instrOfferingId = null;
        ctrlCrsOfferingId = null;
        readOnlyCrsOfferingId = null;
        instrOfferingName = null;
        originalOfferings = DynamicList.getInstance(new ArrayList<Long>(), new DynamicListObjectFactory<Long>() {
            public Long create() { return -1l; }
        });
        courseOfferingIds = DynamicList.getInstance(new ArrayList<Long>(), new DynamicListObjectFactory<Long>() {
            public Long create() { return -1l; }
        });
        courseOfferingNames = DynamicList.getInstance(new ArrayList<String>(), factoryCourseOfferings);
        ownedCourse = DynamicList.getInstance(new ArrayList<Boolean>(), new DynamicListObjectFactory<Boolean>() {
            public Boolean create() { return false; }
        });
        resvId = DynamicList.getInstance(new ArrayList<String>(), factoryCourseOfferings);
        limits = DynamicList.getInstance(new ArrayList<Integer>(), new DynamicListObjectFactory<Integer>() {
            public Integer create() { return null; }
        });
        requested = DynamicList.getInstance(new ArrayList<Integer>(), new DynamicListObjectFactory<Integer>() {
            public Integer create() { return null; }
        });
        projected = DynamicList.getInstance(new ArrayList<Integer>(), new DynamicListObjectFactory<Integer>() {
            public Integer create() { return null; }
        });
        lastTerm = DynamicList.getInstance(new ArrayList<Integer>(), new DynamicListObjectFactory<Integer>() {
            public Integer create() { return null; }
        });
        canDelete = DynamicList.getInstance(new ArrayList<Boolean>(), new DynamicListObjectFactory<Boolean>() {
            public Boolean create() { return false; }
        });
        ioLimit = null;
        unlimited = null;
    }

    public List<Long> getOriginalOfferings() {
        return originalOfferings;
    }
    public Long getOriginalOfferings(int key) {
        return originalOfferings.get(key);
    }
    public void setOriginalOfferings(int key, Long value) {
        this.originalOfferings.set(key, value);
    }
    public void setOriginalOfferings(List<Long> courseOfferingIds) {
        this.originalOfferings = courseOfferingIds;
    }

    public List<Long> getCourseOfferingIds() {
        return courseOfferingIds;
    }
    public Long getCourseOfferingIds(int key) {
        return courseOfferingIds.get(key);
    }
    public void setCourseOfferingIds(int key, Long value) {
        this.courseOfferingIds.set(key, value);
    }
    public void setCourseOfferingIds(List<Long> courseOfferingIds) {
        this.courseOfferingIds = courseOfferingIds;
    }

    public List<String> getCourseOfferingNames() {
        return courseOfferingNames;
    }
    public String getCourseOfferingNames(int key) {
        return courseOfferingNames.get(key).toString();
    }
    public void setCourseOfferingNames(int key, String value) {
        this.courseOfferingNames.set(key, value);
    }
    public void setCourseOfferingNames(List<String> courseOfferingNames) {
        this.courseOfferingNames = courseOfferingNames;
    }

    public List<Boolean> getOwnedCourse() {
        return ownedCourse;
    }
    public Boolean getOwnedCourse(int key) {
        return ownedCourse.get(key);
    }
    public void setOwnedCourse(int key, Boolean value) {
        this.ownedCourse.set(key, value);
    }
    public void setOwnedCourse(List<Boolean> ownedCourse) {
        this.ownedCourse = ownedCourse;
    }

    public List<Integer> getLimits() {
        return limits;
    }
    public Integer getLimits(int key) {
        return limits.get(key);
    }
    public void setLimits(int key, Integer value) {
        this.limits.set(key, value);
    }
    public void setLimits(List<Integer> limits) {
        this.limits = limits;
    }

    public List<String> getResvId() {
        return resvId;
    }
    public String getResvId(int key) {
        return resvId.get(key).toString();
    }
    public void setResvId(int key, String value) {
        this.resvId.set(key, value);
    }
    public void setResvId(List<String> resvId) {
        this.resvId = resvId;
    }

    public List<Integer> getRequested() {
        return requested;
    }
    public Integer getRequested(int key) {
        return requested.get(key);
    }
    public void setRequested(int key, Integer value) {
        this.requested.set(key, value);
    }
    public void setRequested(List<Integer> requested) {
        this.requested = requested;
    }

    public List<Integer> getProjected() {
        return projected;
    }
    public Integer getProjected(int key) {
        return projected.get(key);
    }
    public void setProjected(int key, Integer value) {
        this.projected.set(key, value);
    }
    public void setProjected(List<Integer> projected) {
        this.projected = projected;
    }

    public List<Integer> getLastTerm() {
        return lastTerm;
    }
    public Integer getLastTerm(int key) {
        return lastTerm.get(key);
    }
    public void setLastTerm(int key, Integer value) {
        this.lastTerm.set(key, value);
    }
    public void setLastTerm(List<Integer> lastTerm) {
        this.lastTerm = lastTerm;
    }
    
    public List<Boolean> getCanDelete() {
        return canDelete;
    }
    public Boolean getCanDelete(int key) {
        return (Boolean)canDelete.get(key);
    }
    public void setCanDelete(int key, Boolean value) {
        this.canDelete.set(key, value);
    }
    public void setCanDelete(List<Boolean> canDelete) {
        this.canDelete = canDelete;
    }

   public Long getCtrlCrsOfferingId() {
        return ctrlCrsOfferingId;
    }
    public void setCtrlCrsOfferingId(Long ctrlCrsOfferingId) {
        this.ctrlCrsOfferingId = ctrlCrsOfferingId;
    }
    public Long getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(Long instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    public String getInstrOfferingName() {
        return instrOfferingName;
    }
    public void setInstrOfferingName(String instrOfferingName) {
        this.instrOfferingName = instrOfferingName;
    }
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public Long getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(Long subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
    
    public Long getAddCourseOfferingId() {
        return addCourseOfferingId;
    }
    public void setAddCourseOfferingId(Long addCourseOfferingId) {
        this.addCourseOfferingId = addCourseOfferingId;
    }
    
    public Long getReadOnlyCrsOfferingId() {
        return readOnlyCrsOfferingId;
    }
    public void setReadOnlyCrsOfferingId(Long readOnlyCrsOfferingId) {
        this.readOnlyCrsOfferingId = readOnlyCrsOfferingId;
    }
    
    public Boolean getOwnedInstrOffr() {
		return ownedInstrOffr;
	}
	public void setOwnedInstrOffr(Boolean ownedInstrOffr) {
		this.ownedInstrOffr = ownedInstrOffr;
	}

	public Integer getIoLimit() {
		return ioLimit;
	}

	public void setIoLimit(Integer ioLimit) {
		this.ioLimit = ioLimit;
	}

	public Boolean getUnlimited() {
		return unlimited;
	}

	public void setUnlimited(Boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
     * Add course offering to original course offerings list
     * @param co Course Offering object
     */
    public void addToOriginalCourseOfferings(CourseOffering co) {
        this.originalOfferings.add(co.getUniqueId());
    }
    
    /**
     * Add course offering to the list
     * @param co Course Offering object
     * @param resv
     * @param isOwner
     */
    public void addToCourseOfferings(CourseOffering co, Boolean isOwner, Boolean canDelete) {
        this.courseOfferingIds.add(co.getUniqueId());
        this.courseOfferingNames.add((co.getCourseNameWithTitle()));
        this.ownedCourse.add(isOwner);
        this.resvId.add("");
        this.limits.add(co.getReservation());
        this.requested.add(null);
        this.projected.add(co.getProjectedDemand());
        this.lastTerm.add(co.getDemand());
        this.canDelete.add(canDelete);
    }
    
    /**
     * Remove course offering from the list
     * @param courseOfferingId Course Offering Id 
     */
    public void removeFromCourseOfferings(Long courseOfferingId) {
        int ct=0;
        for (Iterator<Long> i = this.courseOfferingIds.iterator(); i.hasNext(); ) {
            Long co1 = i.next();
            if(co1.equals(courseOfferingId)) {
                i.remove();
                this.courseOfferingNames.remove(ct);
                this.ownedCourse.remove(ct);
                this.resvId.remove(ct);
                this.limits.remove(ct);
                this.requested.remove(ct);
                this.projected.remove(ct);
                this.lastTerm.remove(ct);
                this.canDelete.remove(ct);
                break;
            }
            ct++;
        }
    }

    /**
     * @param course
     * @return -1 if not found
     */
    public int getIndex(Long courseOfferingId) {
        for (int i=0; i<courseOfferingIds.size(); i++ ) {
        	Long co1 = courseOfferingIds.get(i);
            if(co1.equals(courseOfferingId)) {
                return i;
            }
        }
        return -1;
    }
}
