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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.LocationDAO;


/** 
 * MyEclipse Struts
 * Creation date: 12-08-2005
 * 
 * XDoclet definition:
 * @struts:form name="classEditForm"
 *
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
public class ClassEditForm extends PreferencesForm {
	

    // --------------------------------------------------------- Class Constants

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257849883023915058L;

	// Messages
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	/** Class Start/End Date Format **/
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");    
    
    // --------------------------------------------------------- Instance Variables
    
    private Integer nbrRooms;
    private Integer expectedCapacity;
    private Long classId;
    private Long parentClassId;
    private String section;
    private Long managingDept;
    private Long controllingDept;
    private Long subpart;
    private String className;
    private String parentClassName;
    private String itypeDesc;
    private List<String> instrLead;
    private String managingDeptLabel;
    private String notes;
    private List<String> instructors;
    private List<String> instrPctShare;
    private List<String> assignments;
    private Long datePattern;
    private String subjectAreaId;
    private String instrOfferingId;
    private String courseName;
    private String courseTitle;
    private Boolean displayInstructor;
    private String schedulePrintNote;
    private String classSuffix;
    private Boolean enabledForStudentScheduling;
    private Integer maxExpectedCapacity;
    private Float roomRatio;
    private Integer minRoomLimit;
    private Boolean unlimitedEnroll;
    private Integer enrollment;
    private Integer snapshotLimit;
    private Boolean isCrosslisted;
    private String accommodation;
    private Boolean isCancelled;
    private List<String> instrResponsibility;
    private String defaultTeachingResponsibilityId;
    private String lms;
    private String fundingDept;
    private Boolean datePatternEditable;
    
    public ClassEditForm() {
    	super();
        reset();
    }

    @Override
    public void validate(UniTimeAction action) {
    	int iRoomCapacity = -1;
        if (nbrRooms!=null && nbrRooms.intValue()<0)
            action.addFieldError("nbrRooms", MSG.errorNumberOfRoomsNegative());

        if (roomRatio==null || roomRatio.floatValue()<0.0f)
        	action.addFieldError("nbrRooms", MSG.errorRoomRatioNegative());

        if (expectedCapacity==null || expectedCapacity.intValue()<0)
        	action.addFieldError("expectedCapacity", MSG.errorMinimumExpectedCapacityNegative());

        if (maxExpectedCapacity==null || maxExpectedCapacity.intValue()<0)
        	action.addFieldError("maxExpectedCapacity", MSG.errorMaximumExpectedCapacityNegative());
        else
        	if (maxExpectedCapacity.intValue()<expectedCapacity.intValue())
        		action.addFieldError("maxExpectedCapacity", MSG.errorMaximumExpectedCapacityLessThanMinimum());

        if (managingDept==null || managingDept.longValue()<=0)
        	action.addFieldError("managingDept", MSG.errorRequiredClassOwner());

        // Schedule print note has 2000 character limit
        if (schedulePrintNote!=null && schedulePrintNote.length()>1999)
        	action.addFieldError("notes", MSG.errorSchedulePrintNoteLongerThan1999());

        // Notes has 1000 character limit
        if (notes!=null && notes.length()>999)
        	action.addFieldError("notes", MSG.errorNotesLongerThan999());

        // Check no duplicates or blank instructors
        if (instructors.size()>0 && !super.checkPrefs(instructors, instrResponsibility))
        	action.addFieldError("instructors", MSG.errorInvalidInstructors());

        // Check that any room with a preference required has capacity >= room capacity for the class
        if (iRoomCapacity>0) {
        	List<String> rp = this.getRoomPrefs();
        	List<String> rpl = this.getRoomPrefLevels();
        	for (int i=0; i<rpl.size(); i++) {
                String pl = rpl.get(i);
                if (pl.trim().equalsIgnoreCase("1")) {
                    String roomId = rp.get(i);                    
                    Location room = new LocationDAO().get(Long.valueOf(roomId));
                    int rCap = room.getCapacity().intValue();
                    if (rCap<iRoomCapacity)
        	            action.addFieldError("roomPref", MSG.errorRequiredRoomTooSmall(room.getLabel(), rCap, iRoomCapacity));
                }
            }
        }

        // Check Other Preferences
        super.validate(action);
    }
    
    @Override
    public void reset() {
    	nbrRooms = null;
        expectedCapacity = null;
        classId = null;
        section = null;
        managingDept = null;
        controllingDept = null;
        subpart = null;
        className = "";
        courseName = "";
        courseTitle = "";
        parentClassName = "-";
        itypeDesc = "";
        datePattern = null;
        instrLead = new ArrayList();
        managingDeptLabel = "-";
        notes="";
        displayInstructor = null;
        schedulePrintNote = null;
        classSuffix = null;
        enabledForStudentScheduling = null;
        maxExpectedCapacity = null;
        roomRatio = null;
        unlimitedEnroll = null;
        isCrosslisted = null;
        isCancelled = null;

        instructors = new ArrayList();
        instrPctShare= new ArrayList();
        assignments = null;
        enrollment = null;
        snapshotLimit = null;
        accommodation = null;
        instrResponsibility = new ArrayList();
        TeachingResponsibility tr = TeachingResponsibility.getDefaultInstructorTeachingResponsibility();
        defaultTeachingResponsibilityId = (tr == null ? "" : tr.getUniqueId().toString());
        lms = null;
        fundingDept = null;
        datePatternEditable = false;

        super.reset();
    }

    /**
     * @return Returns the classId.
     */
    public Long getClassId() {
        return classId;
    }
    /**
     * @param classId The classId to set.
     */
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    /**
     * @return Returns the section.
     */
    public String getSection() {
        return section;
    }
    /**
     * @param section The section to set.
     */
    public void setSection(String section) {
        this.section = section;
    }
    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }
    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }
    /**
     * @return Returns the assignments.
     */
    public List<String> getAssignments() {
        return assignments;
    }
    /**
     * @return Returns the assignments.
     */
    public String getAssignments(int key) {
        return assignments.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setAssignments(int key, String value) {
        this.assignments.set(key, value);
    }
    /**
     * @param assignments The assignments to set.
     */
    public void setAssignments(List<String> assignments) {
        this.assignments = assignments;
    }

    public Long getDatePattern() {
        return datePattern;
    }
    public void setDatePattern(Long datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * @return Returns the expectedCapacity.
     */
    public Integer getExpectedCapacity() {
        return expectedCapacity;
    }    
    /**
     * @param expectedCapacity The expectedCapacity to set.
     */
    public void setExpectedCapacity(Integer expectedCapacity) {
        this.expectedCapacity = expectedCapacity;
    }
    
    /**
     * @return Returns the instructors.
     */
    public List<String> getInstructors() {
        return instructors;
    }
    /**
     * @return Returns the instructors.
     */
    public String getInstructors(int key) {
        return instructors.get(key);
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setInstructors(int key, String value) {
        this.instructors.set(key, value);
    }
    /**
     * @param instructors The instructors to set.
     */
    public void setInstructors(List<String> instructors) {
        this.instructors = instructors;
    }
    
    /**
     * @return Returns the instrLead.
     */
    public List<String> getInstrLead() {
        return instrLead;
    }
    /**
     * @param instrLead The instrLead to set.
     */
    public void setInstrLead(List<String> instrLead) {
        this.instrLead = instrLead;
    }
    
    public void addInstrLead(String instructorId) {
        instrLead.add(instructorId);
    }
    
    public String getInstrLead(int key) {
        return instrLead.get(key);
    }

    public void setInstrLead(int key, String value) {
        this.instrLead.set(key, value);
    }

    public boolean getInstrHasPref(int key) {
        if (!"true".equals(getInstrLead(key)) && !"on".equals(getInstrLead(key))) return false;
        String instructorId = getInstructors(key);
        if (instructorId==null || instructorId.trim().length()==0 || instructorId.equals("-")) return false;
        DepartmentalInstructor di = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
        if (di!=null && di.hasPreferences()) return true;
        return false;
    }

    /**
     * @return Returns the instrPctShare.
     */
    public List<String> getInstrPctShare() {
        return instrPctShare;
    }
    /**
     * @return Returns the instrPctShare.
     */
    public String getInstrPctShare(int key) {
        return instrPctShare.get(key).toString();
    }
    /**
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setInstrPctShare(int key, String value) {
        this.instrPctShare.set(key, value);
    }
    /**
     * @param instrPctShare The instrPctShare to set.
     */
    public void setInstrPctShare(List<String> instrPctShare) {
        this.instrPctShare = instrPctShare;
    }
    
    /**
     * @return Returns the nbrRooms.
     */
    public Integer getNbrRooms() {
        return nbrRooms;
    }
    /**
     * @param nbrRooms The nbrRooms to set.
     */
    public void setNbrRooms(Integer nbrRooms) {
        this.nbrRooms = nbrRooms;
    }
    /**
     * @return Returns the managingDept.
     */
    public Long getManagingDept() {
        return managingDept;
    }
    /**
     * @param managingDept The managingDept to set.
     */
    public void setManagingDept(Long owner) {
        this.managingDept = owner;
    }
    /**
     * @return Returns the parent.
     */
    public String getParentClassName() {
        return parentClassName;
    }
    /**
     * @param parent The parent to set.
     */
    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }
    /**
     * @return Returns the subpart.
     */
    public Long getSubpart() {
        return subpart;
    }
    /**
     * @param subpart The subpart to set.
     */
    public void setSubpart(Long subpart) {
        this.subpart = subpart;
    }
    /**
     * @return Returns the itypeDesc.
     */
    public String getItypeDesc() {
        return itypeDesc;
    }
    /**
     * @param itypeDesc The itypeDesc to set.
     */
    public void setItypeDesc(String itypeDesc) {
        this.itypeDesc = itypeDesc;
    }    
    /**
     * @return Returns the notes.
     */
    public String getNotes() {
        return notes;
    }
    /**
     * @param notes The notes to set.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }    
    /**
     * @return Returns the managingDeptLabel.
     */
    public String getManagingDeptLabel() {
        return managingDeptLabel;
    }
    /**
     * @param managingDeptLabel The managingDeptLabel to set.
     */
    public void setManagingDeptLabel(String ownerLabel) {
        this.managingDeptLabel = ownerLabel;
    }

    /**
     * @return Returns the parentClassId.
     */
    public Long getParentClassId() {
        return parentClassId;
    }
    
    public String getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(String subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }

    public String getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(String instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    
    /**
     * @param parentClassId The parentClassId to set.
     */
    public void setParentClassId(Long parentClassId) {
        this.parentClassId = parentClassId;
    }
    
    
    public String getClassSuffix() {
        return classSuffix;
    }
    public void setClassSuffix(String classSuffix) {
        this.classSuffix = classSuffix;
    }
    public Boolean getEnabledForStudentScheduling() {
        return enabledForStudentScheduling;
    }
    public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) {
        this.enabledForStudentScheduling = enabledForStudentScheduling;
    }
    public Boolean getDisplayInstructor() {
        return displayInstructor;
    }
    public void setDisplayInstructor(Boolean displayInstructor) {
        this.displayInstructor = displayInstructor;
    }
    public Integer getMaxExpectedCapacity() {
        return maxExpectedCapacity;
    }
    public void setMaxExpectedCapacity(Integer maxExpectedCapacity) {
        this.maxExpectedCapacity = maxExpectedCapacity;
    }
    public Float getRoomRatio() {
        return roomRatio;
    }
    public void setRoomRatio(Float roomRatio) {
        this.roomRatio = roomRatio;
    }
    public String getSchedulePrintNote() {
        return schedulePrintNote;
    }
    public void setSchedulePrintNote(String schedulePrintNote) {
        this.schedulePrintNote = schedulePrintNote;
    }
    public Integer getMinRoomLimit() {
        return minRoomLimit;
    }
    public void setMinRoomLimit(Integer minRoomLimit) {
        this.minRoomLimit = minRoomLimit;
    }
    
    public Boolean getUnlimitedEnroll() {
        return unlimitedEnroll;
    }
    public void setUnlimitedEnroll(Boolean unlimitedEnroll) {
        this.unlimitedEnroll = unlimitedEnroll;
    }
    
    public Boolean getIsCrosslisted() {
        return isCrosslisted;
    }
    public void setIsCrosslisted(Boolean isCrosslisted) {
        this.isCrosslisted = isCrosslisted;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }
    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
    
    /**
     * @param date
     * @return String representation of the date formatted as mm/dd/yyyy
     */
    public String dateToStr(Date date) {
        if(date==null)
            return "";
        else
            return dateFormat.format(date);
    }
    
    /**
     * @param date String representation of the date ( mm/dd/yyyy )
     * @return java.sql.Date object for the given string
     * @throws ParseException
     */
    public java.sql.Date strToDate(String date) throws ParseException {
        java.sql.Date dt = null;
        if(date==null || date.trim().length()==0)
            return null;
        else
            dt = new java.sql.Date( dateFormat.parse(date).getTime() );
        
        return dt;
    }
    
    /**
     * Add Instructor Data to List
     * If class instructor is null, a blank row is added
     * @param classInstr
     */
    public void addToInstructors(ClassInstructor classInstr) {
        
        // Default values
        String id = "";
        String pctShare = "0";
        boolean isLead = false;
        String resp = "";
        
        // Class Instructor Specified
        if(classInstr!=null) {
	        id = classInstr.getInstructor().getUniqueId().toString();
	        pctShare = classInstr.getPercentShare().toString();
	        isLead = classInstr.isLead().booleanValue();
	        if (classInstr.getResponsibility() != null)
	        	resp = classInstr.getResponsibility().getUniqueId().toString();
        }
        else {
            // If this is the only record - set 100% share and make lead
            if(this.instructors.size()==0) {
                pctShare = "100";
                isLead = true;
            }
            resp = getDefaultTeachingResponsibilityId();
        }
 
        // Add row
        this.instructors.add(id);
        this.instrPctShare.add(pctShare);
        this.instrLead.add(isLead?"true":"false");
        this.instrResponsibility.add(resp);
    }

    /**
     * Remove Instructor from List
     * @param deleteId
     */
    public void removeInstructor(int deleteId) {
        // Remove from lists
        this.instructors.remove(deleteId);
        this.instrPctShare.remove(deleteId);
        if (this.instrLead.size()>deleteId)
        	this.instrLead.remove(deleteId);
        this.instrResponsibility.remove(deleteId);
    }

    /**
     * Clears all preference lists
     */
    public void clearPrefs() {
        this.instructors.clear();
        this.instrPctShare.clear();
        this.instrLead.clear();
        this.instrResponsibility.clear();
    }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName=courseName; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle=courseTitle; }

    public Integer getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Integer enrollment) {
		this.enrollment = enrollment;
	}
	
    public Integer getSnapshotLimit() {
		return snapshotLimit;
	}

	public void setSnapshotLimit(Integer snapshotLimit) {
		this.snapshotLimit = snapshotLimit;
	}
	
    public String getAccommodation() { return accommodation; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }
    
    public Long getControllingDept() { return controllingDept; }
    public void setControllingDept(Long deptId) { controllingDept = deptId; }
    
    public List<String> getInstrResponsibility() { return instrResponsibility; }
    public String getInstrResponsibility(int key) { return instrResponsibility.get(key); }
    public void setInstrResponsibility(int key, String value) { this.instrResponsibility.set(key, value); }
    public void setInstrResponsibility(List<String> instrResponsibility) { this.instrResponsibility = instrResponsibility; }
	public String getDefaultTeachingResponsibilityId() { return defaultTeachingResponsibilityId; }
	public void setDefaultTeachingResponsibilityId(String defaultTeachingResponsibilityId) { this.defaultTeachingResponsibilityId = defaultTeachingResponsibilityId; }

	public String getLms() {
		return lms;
	}

	public void setLms(String lms) {
		this.lms = lms;
	}

	public String getFundingDept() {
		return fundingDept;
	}

	public void setFundingDept(String fundingDept) {
		this.fundingDept = fundingDept;
	}

	public Boolean getDatePatternEditable() { return datePatternEditable; }
	public Boolean isDatePatternEditable() { return datePatternEditable; }
    public void setDatePatternEditable(Boolean datePatternEditable) { this.datePatternEditable = datePatternEditable; }
}
