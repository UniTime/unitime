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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassEditForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "classEdit", results = {
		@Result(name = "editClass", type = "tiles", location = "classEditTile.tiles"),
		@Result(name = "instructionalOfferingSearch", type = "redirect", location = "/instructionalOfferingSearch.action"),
		@Result(name = "addDistributionPrefs", type = "redirect", location = "/distributionPrefs.action", 
			params = { "classId", "${form.classId}", "op", "${op}"}
		),
		@Result(name = "displayClassDetail", type = "redirect", location = "/classDetail.action",
			params = { "cid", "${form.classId}"}
		)
	})
@TilesDefinition(name = "classEditTile.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Class Edit"),
		@TilesPutAttribute(name = "body", value = "/user/classEdit.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true")
	})
public class ClassEditAction extends PreferencesAction2<ClassEditForm> {
	private static final long serialVersionUID = 8307712785352190036L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	protected String classId = null;
	protected String op2 = null;
	protected String deleteType = null;
	protected Long deleteId = null;

	public String getCid() { return classId; }
	public void setCid(String classId) { this.classId = classId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	
	public String getDeleteType() { return deleteType; }
	public void setDeleteType(String deleteType) { this.deleteType = deleteType; }
	public Long getDeleteId() { return deleteId; }
	public void setDeleteId(Long deleteId) { this.deleteId = deleteId; }

    /** Anchor names **/
    public final String HASH_INSTRUCTORS = "Instructors";

    public String execute() throws Exception {
    	if (form == null) form = new ClassEditForm();

		super.execute();

		if (classId == null && request.getAttribute("cid") != null)
			classId = (String)request.getAttribute("cid");

		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;


        // Read class id from form
        if (MSG.actionAddTimePreference().equals(op)
                || MSG.actionAddRoomPreference().equals(op)
                || MSG.actionAddBuildingPreference().equals(op)
                || MSG.actionAddRoomFeaturePreference().equals(op)
                || MSG.actionAddDistributionPreference().equals(op)
                || MSG.actionAddInstructor().equals(op)
                || MSG.actionUpdatePreferences().equals(op)
                || MSG.actionAddDatePatternPreference().equals(op)
                || MSG.actionAddAttributePreference().equals(op)
                || MSG.actionAddInstructorPreference().equals(op)
                || MSG.actionClearClassPreferences().equals(op)
                || MSG.actionRemoveBuildingPreference().equals(op)
        		|| MSG.actionRemoveDistributionPreference().equals(op)
        		|| MSG.actionRemoveRoomFeaturePreference().equals(op)
        		|| MSG.actionRemoveRoomGroupPreference().equals(op)
        		|| MSG.actionRemoveRoomPreference().equals(op)
        		|| MSG.actionRemoveDatePatternPreference().equals(op)
        		|| MSG.actionRemoveTimePattern().equals(op)
        		|| MSG.actionRemoveInstructor().equals(op)
        		|| MSG.actionRemoveAttributePreference().equals(op)
        		|| MSG.actionRemoveInstructorPreference().equals(op)
                || MSG.actionAddRoomGroupPreference().equals(op)
                || MSG.actionBackToDetail().equals(op)
                || MSG.actionNextClass().equals(op)
                || MSG.actionPreviousClass().equals(op)
                || "updateDatePattern".equals(op)
                || "updatePref".equals(op)) {
            classId = form.getClassId().toString();
        }

        // Determine if initial load
        if (op==null || op.trim().isEmpty()) {
            op = "init";
        }
        
        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        Debug.debug("op: " + op);
        Debug.debug("class: " + classId);

        // Check class exists
        if (classId==null || classId.trim().isEmpty()) {
           if (BackTracker.doBack(request, response))
        	   return null;
           else
        	   throw new Exception(MSG.errorClassInfoNotSupplied());
        }

        sessionContext.checkPermission(classId, "Class_", Right.ClassEdit);

        // Cancel - Go back to Class Detail Screen
        if (MSG.actionBackToDetail().equals(op)) {
        	return "displayClassDetail";
        }

        // If class id is not null - load class info
        Class_DAO cdao = new Class_DAO();
        Class_ c = cdao.get(Long.valueOf(classId));

		// Add Distribution Preference - Redirect to dist prefs screen
	    if (MSG.actionAddDistributionPreference().equals(op)) {
        	sessionContext.checkPermission(c, Right.DistributionPreferenceClass);
            return "addDistributionPrefs";
	    }

	    // Add Instructor
	    if(MSG.actionAddInstructor().equals(op))
            addInstructor();

	    // Delete Instructor
        if (MSG.actionRemoveInstructor().equals(op) && "instructor".equals(deleteType))
            deleteInstructor();

        // Restore all inherited preferences
        if (MSG.actionClearClassPreferences().equals(op)) {
        	sessionContext.checkPermission(c, Right.ClassEditClearPreferences);

            Set s = c.getPreferences();
            doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
            c.setPreferences(s);
            cdao.update(c);
            op = "init";

            ChangeLog.addChange(
                    null,
                    sessionContext,
                    c,
                    ChangeLog.Source.CLASS_EDIT,
                    ChangeLog.Operation.CLEAR_PREF,
                    c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    c.getManagingDept());

            return "displayClassDetail";
        }

        // Reset form for initial load
        if ("init".equals(op)) {
            form.reset();
        }

        // Load form attributes that are constant
        doLoad(c, op);

        // Update Preferences for Class
        if (MSG.actionUpdatePreferences().equals(op) || MSG.actionNextClass().equals(op) || MSG.actionPreviousClass().equals(op)) {
            // Validate input prefs
            form.validate(this);

            // No errors - Add to class and update
            if (!hasFieldErrors()) {

            	org.hibernate.Session hibSession = cdao.getSession();
            	Transaction tx = hibSession.beginTransaction();

            	try {
                    // Clear all old prefs
                    Set s = c.getPreferences();
                    doClear(s, Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);

                    // Save class data
                    doUpdate(c, hibSession);

                    // Save Prefs
                    super.doUpdate(c, s, timeVertical,
                    		Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
                    
                    hibSession.saveOrUpdate(c);

    	            tx.commit();
    	            
                    String className = ApplicationProperty.ExternalActionClassEdit.value();
                	if (className != null && className.trim().length() > 0){
                    	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
                   		editAction.performExternalClassEditAction(c, hibSession);
                	}

    	            if (MSG.actionNextClass().equals(op)) {
    	            	response.sendRedirect(response.encodeURL("classEdit.action?cid="+form.getNextId()));
    	            	return null;
    	            }

    	            if (MSG.actionPreviousClass().equals(op)) {
    	            	response.sendRedirect(response.encodeURL("classEdit.action?cid="+form.getPreviousId()));
    	            	return null;
    	            }

    	            return "displayClassDetail";
            	} catch (Exception e) {
            		tx.rollback(); throw e;
            	}
            }
        }

        Vector leadInstructors = new Vector();
        if ("updatePref".equals(op)) {
        	try {
                List instrLead = form.getInstrLead();
                List instructors = form.getInstructors();

                for(int i=0; i<instructors.size(); i++) {
                    String instrId = instructors.get(i).toString();
                    if (Preference.BLANK_PREF_VALUE.equals(instrId)) continue;
                    boolean lead = "on".equals(instrLead.get(i)) || "true".equals(instrLead.get(i));
                    if (lead) leadInstructors.add((DepartmentalInstructorDAO.getInstance()).get(Long.valueOf(instrId)));
                }
        		op="init";
        	} catch (NumberFormatException e) {}
        }

        if ("updateDatePattern".equals(op)) {        	
			initPrefs(c, leadInstructors, true);
			form.getDatePatternPrefs().clear();
        	form.getDatePatternPrefLevels().clear();
			DatePattern selectedDatePattern = (form.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(form.getDatePattern()));
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {
					boolean found = false;
					for (DatePatternPref dpp: (Set<DatePatternPref>)c.getPreferences(DatePatternPref.class)) {
						if (dp.equals(dpp.getDatePattern())) {
							form.addToDatePatternPrefs(dp.getUniqueId().toString(), dpp.getPrefLevel().getUniqueId().toString());
							found = true;
						}
					}
					if (!found)
						for (DatePatternPref dpp: (Set<DatePatternPref>)c.getSchedulingSubpart().getPreferences(DatePatternPref.class)) {
							if (dp.equals(dpp.getDatePattern())) {
								form.addToDatePatternPrefs(dp.getUniqueId().toString(), dpp.getPrefLevel().getUniqueId().toString());
								found = true;
							}
						}
					if (!found)
						form.addToDatePatternPrefs(dp.getUniqueId().toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
				}
			}
		}
        
        // Initialize Preferences for initial load
        form.setAvailableTimePatterns(TimePattern.findApplicable(
        		sessionContext.getUser(),
        		c.getSchedulingSubpart().getMinutesPerWk(),
        		(form.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(form.getDatePattern())),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		true,
        		c.getManagingDept()));
		Set timePatterns = null;
        if ("init".equals(op)) {
        	initPrefs(c, leadInstructors, true);
		    timePatterns = c.effectiveTimePatterns();
		    
		    DatePattern selectedDatePattern = c.effectiveDatePattern();
			
			if (selectedDatePattern != null) {
				for (DatePattern dp: selectedDatePattern.findChildren()) {					
					if (!form.getDatePatternPrefs().contains(
							dp.getUniqueId().toString())) {
						form.addToDatePatternPrefs(dp.getUniqueId().toString(), PreferenceLevel.PREF_LEVEL_NEUTRAL);
					}
				}
			}
		   
        }

		// Process Preferences Action
		processPrefAction();

        // Generate Time Pattern Grids
		super.generateTimePatternGrids(c, 
				c.getSchedulingSubpart().getMinutesPerWk(),
        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
        		(form.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(form.getDatePattern())),
				timePatterns, op, timeVertical, true, leadInstructors);

		// Instructors
        setupInstructors(c);
        setupChildren(c); // Date patterns allowed in the DDL for Date pattern preferences
        LookupTables.setupDatePatterns(request, sessionContext.getUser(), MSG.dropDefaultDatePattern(), c.getSchedulingSubpart().effectiveDatePattern(), c.getManagingDept(), c.effectiveDatePattern());

        LookupTables.setupRooms(request, c);		 // Rooms
        LookupTables.setupBldgs(request, c);		 // Buildings
        LookupTables.setupRoomFeatures(request, c); // Room Features
        LookupTables.setupRoomGroups(request, c);   // Room Groups

        form.setAllowHardPrefs(sessionContext.hasPermission(c, Right.CanUseHardRoomPrefs));

        BackTracker.markForBack(
        		request,
        		"classDetail.action?cid="+form.getClassId(),
        		MSG.backClass(form.getClassName()),
        		true, false);

        return "editClass";
    }

    /**
     * Loads class info into the form
     */
    private void doLoad(Class_ c, String op) {

        Department managingDept = c.getManagingDept();
        String parentClassName = "-";
        Long parentClassId = null;
        if(c.getParentClass()!=null) {
            parentClassName = c.getParentClass().toString();
            parentClassId = c.getParentClass().getUniqueId();
        }

        CourseOffering cco = c.getSchedulingSubpart().getControllingCourseOffering();

        // populate form
        form.setClassId(c.getUniqueId());
        form.setSection(c.getSectionNumberString());
        form.setClassName(c.getClassLabel());

        SchedulingSubpart ss = c.getSchedulingSubpart();
    	String itypeDesc = c.getItypeDesc();
    	if (ss.getInstrOfferingConfig().getInstructionalOffering().hasMultipleConfigurations())
    		itypeDesc += " [" + ss.getInstrOfferingConfig().getName() + "]";
        form.setItypeDesc(itypeDesc);

        form.setParentClassName(parentClassName);
        form.setParentClassId(parentClassId);
        form.setSubjectAreaId(cco.getSubjectArea().getUniqueId().toString());
        form.setInstrOfferingId(cco.getInstructionalOffering().getUniqueId().toString());
        form.setSubpart(c.getSchedulingSubpart().getUniqueId());
        form.setCourseName(cco.getInstructionalOffering().getCourseName());
        form.setCourseTitle(cco.getTitle());
        form.setManagingDept(managingDept.getUniqueId());
        form.setControllingDept(c.getControllingDept().getUniqueId());
        form.setManagingDeptLabel(managingDept.getManagingDeptLabel());
        form.setUnlimitedEnroll(c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment());
        form.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(c)));
        
        Class_ next = c.getNextClass(sessionContext, Right.ClassEdit);
        form.setNextId(next==null?null:next.getUniqueId().toString());
        Class_ previous = c.getPreviousClass(sessionContext, Right.ClassEdit);
        form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
        form.setMinRoomLimit(c.getMinRoomLimit());
        form.setEnrollment(c.getEnrollment());
        form.setSnapshotLimit(c.getSnapshotLimit());

        // Load from class only for initial load or reload
        if ("init".equals(op)) {
	        form.setExpectedCapacity(c.getExpectedCapacity());
	        form.setDatePattern(c.getDatePattern()==null?Long.valueOf(-1):c.getDatePattern().getUniqueId());
	        form.setDatePatternEditable(ApplicationProperty.WaitListCanChangeDatePattern.isTrue() || c.getEnrollment() == 0 || !c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().effectiveReScheduleNow());
	        form.setNbrRooms(c.getNbrRooms());
	        form.setNotes(c.getNotes());
	        form.setManagingDept(c.getManagingDept().getUniqueId());
		    form.setSchedulePrintNote(c.getSchedulePrintNote());
		    form.setClassSuffix(c.getDivSecNumber());
		    form.setMaxExpectedCapacity(c.getMaxExpectedCapacity());
		    form.setRoomRatio(c.getRoomRatio());
		    form.setLms(c.getLms() == null ? "" : c.getLms().getLabel());
	        if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
	        	form.setFundingDept(c.getEffectiveFundingDept().getLabel());
	        } else {
	        	form.setFundingDept("");
	        }
		    form.setEnabledForStudentScheduling(c.isEnabledForStudentScheduling());
		    form.setDisplayInstructor(c.isDisplayInstructor());

		    List instructors = new ArrayList(c.getClassInstructors());
		    Collections.sort(instructors, new InstructorComparator(sessionContext));

	        for(Iterator iter = instructors.iterator(); iter.hasNext(); ) {
	            ClassInstructor classInstr = (ClassInstructor) iter.next();
	            form.addToInstructors(classInstr);
	        }

	        if (instructors.isEmpty())
	        	form.addToInstructors(null);
        }
    }

    /**
     * Update class data
     */
    private void doUpdate(Class_ c, org.hibernate.Session hibSession ) {

        c.setExpectedCapacity(form.getExpectedCapacity());
        if (form.getDatePattern()==null || form.getDatePattern().intValue()<0)
        	c.setDatePattern(null);
        else
        	c.setDatePattern(DatePatternDAO.getInstance().get(form.getDatePattern()));
        c.setNbrRooms(form.getNbrRooms());
        c.setNotes(form.getNotes());
	    c.setSchedulePrintNote(form.getSchedulePrintNote());
	    //c.setClassSuffix(form.getClassSuffix());
	    c.setMaxExpectedCapacity(form.getMaxExpectedCapacity());
	    c.setRoomRatio(form.getRoomRatio());
	    
	    Boolean disb = form.getEnabledForStudentScheduling();
	    c.setEnabledForStudentScheduling(disb==null ? Boolean.valueOf(false) : disb);

	    Boolean di = form.getDisplayInstructor();
	    c.setDisplayInstructor(di==null ? Boolean.valueOf(false) : di);
	    
	    boolean assignTeachingRequest = Department.isInstructorSchedulingCommitted(c.getControllingDept().getUniqueId());

        // Class all instructors
	    Set<ClassInstructor> classInstrs = new HashSet<ClassInstructor>(c.getClassInstructors());

        // Get instructor data
        List instrLead = form.getInstrLead();
        List instructors = form.getInstructors();
        List instrPctShare = form.getInstrPctShare();
        List instrResponsibility = form.getInstrResponsibility();

        // Save instructor data to class
        for(int i=0; i<instructors.size(); i++) {

            String instrId = instructors.get(i).toString();
            if (Preference.BLANK_PREF_VALUE.equals(instrId)) continue;
            String pctShare = instrPctShare.get(i).toString();
            boolean lead = "on".equals(instrLead.get(i)) || "true".equals(instrLead.get(i));
            String resp = instrResponsibility.get(i).toString();

            DepartmentalInstructor deptInstr = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instrId));
            
            ClassInstructor classInstr = null;
            for (Iterator<ClassInstructor> j = classInstrs.iterator(); j.hasNext();) {
            	ClassInstructor adept = j.next();
            	if (adept.getInstructor().equals(deptInstr)) {
            		classInstr = adept;
            		j.remove();
            		break;
            	}
            }
            if (classInstr == null) {
            	classInstr = new ClassInstructor();
                classInstr.setClassInstructing(c);
                classInstr.setInstructor(deptInstr);
                deptInstr.getClasses().add(classInstr);
                c.getClassInstructors().add(classInstr);
                if (assignTeachingRequest) {
                	for (TeachingClassRequest tcr: c.getTeachingRequests()) {
                		if (tcr.getAssignInstructor() && tcr.getTeachingRequest().getAssignedInstructors().contains(deptInstr)) {
                			classInstr.setTeachingRequest(tcr.getTeachingRequest());
                			break;
                		}
                	}
                }
            }
            classInstr.setLead(Boolean.valueOf(lead));
            try {
            	classInstr.setPercentShare(Integer.valueOf(pctShare));
            } catch (NumberFormatException e) {
            	classInstr.setPercentShare(Integer.valueOf(0));
            }
            try {
            	classInstr.setResponsibility(TeachingResponsibilityDAO.getInstance().get(Long.valueOf(resp), hibSession));
            } catch (NumberFormatException e) {
            	classInstr.setResponsibility(null);
            }
            hibSession.saveOrUpdate(deptInstr);
        }

        for (Iterator<ClassInstructor> iter = classInstrs.iterator(); iter.hasNext() ;) {
            ClassInstructor ci = iter.next();
            DepartmentalInstructor instr = ci.getInstructor();
            instr.getClasses().remove(ci);
            c.getClassInstructors().remove(ci);
            hibSession.saveOrUpdate(instr);
            hibSession.delete(ci);
        }

        ChangeLog.addChange(
                hibSession,
                sessionContext,
                c,
                ChangeLog.Source.CLASS_EDIT,
                ChangeLog.Operation.UPDATE,
                c.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(),
                c.getManagingDept());
    }

    /**
     * Set up instructor lists
     */
    protected void setupInstructors(Class_ c ) throws Exception {

        List instructors = form.getInstructors();
        if(instructors.size()==0)
            return;

        // Get dept instructor list
        LookupTables.setupInstructors(request, sessionContext, c.getDepartmentForSubjectArea().getUniqueId());
        LookupTables.setupInstructorTeachingResponsibilities(request);
    }

    /**
     * Add an instructor to the list (UI)
     */
    protected void addInstructor() {
        if(request.getParameter("instrListTypeAction")!=null && request.getParameter("instrListTypeAction").toString().length()>0)
            return;

        form.addToInstructors(null);
        request.setAttribute(HASH_ATTR, HASH_INSTRUCTORS);
    }

    /**
     * Deletes an instructor from the list (UI)
     */
    protected void deleteInstructor() {
        if (deleteId != null) {
            form.removeInstructor(deleteId.intValue());
            request.setAttribute(HASH_ATTR, HASH_INSTRUCTORS);
        }
    }
    
    /**
     * This method is called to setup children of the selected date pattern of the class
     * or the selected date pattern of the scheduling subpart of the current class.
     */
	protected void setupChildren(Class_ c) {
		DatePattern selectedDatePattern = (form.getDatePattern() < 0 ? c.getSchedulingSubpart().effectiveDatePattern() : DatePatternDAO.getInstance().get(form.getDatePattern()));
		if (selectedDatePattern != null) {
			List<DatePattern> v =  selectedDatePattern.findChildren();
			request.setAttribute(DatePattern.DATE_PATTERN_CHILDREN_LIST_ATTR, v);	
			form.sortDatePatternPrefs(form.getDatePatternPrefs(), form.getDatePatternPrefLevels(), v);
		}
	}
}
