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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructionalOfferingModifyForm;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.util.LookupTables;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Service("/instructionalOfferingModify")
public class InstructionalOfferingModifyAction extends Action {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	/**
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        MessageResources rsc = getResources(request);
        InstructionalOfferingModifyForm frm = (InstructionalOfferingModifyForm) form;
        
		LookupTables.setupExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
		TreeSet ts = new TreeSet();
		for (Iterator it = ((TreeSet) request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME)).iterator(); it.hasNext();){
			Department d = (Department) it.next();
			if (sessionContext.hasPermission(d, Right.MultipleClassSetupDepartment))
				ts.add(d);
		}
		request.setAttribute((Department.EXTERNAL_DEPT_ATTR_NAME + "list"), ts);

        // Get operation
        String op = (request.getParameter("op")==null)
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");

        if(op==null)
            op = request.getParameter("hdnOp");

        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.errorOperationNotInterpreted() + op);

        // Instructional Offering Config Id
        String instrOffrConfigId = "";

        // Set up Lists
        frm.setOp(op);

        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionClassSetup())) {

        	instrOffrConfigId = (request.getParameter("uid")==null)
								? (request.getAttribute("uid")==null)
								        ? null
								        : request.getAttribute("uid").toString()
								: request.getParameter("uid");
								        
            doLoad(request, frm, instrOffrConfigId);
        }

        // Add a class
        if(op.equalsIgnoreCase(rsc.getMessage("button.add"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                String addTemplateClass = frm.getAddTemplateClassId().toString();
                frm.addNewClassesBasedOnTemplate(addTemplateClass);
            }
            else {
                saveErrors(request, errors);
            }
        }
        // Move a class up
        if(op.equalsIgnoreCase(rsc.getMessage("button.moveUp"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                String moveUpClass = frm.getMoveUpClassId().toString();
                frm.moveClassUp(moveUpClass);
            }
            else {
                saveErrors(request, errors);
            }
        }

        // Move a class down
        if(op.equalsIgnoreCase(rsc.getMessage("button.moveDown"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                String moveDownClass = frm.getMoveDownClassId().toString();
                frm.moveClassDown(moveDownClass);
            }
            else {
                saveErrors(request, errors);
            }
        }

        // Remove a class and its children
        if(op.equalsIgnoreCase(rsc.getMessage("button.delete"))) {
            String deletedClass = request.getParameter("deletedClassId");
            if(deletedClass!=null && deletedClass.trim().length()>0)
                frm.removeFromClasses(deletedClass);
        }

        if (op.equalsIgnoreCase("multipleLimits")){
        	frm.setDisplayMaxLimit(new Boolean(!frm.getDisplayMaxLimit().booleanValue()));
        	if (!frm.getDisplayMaxLimit().booleanValue()){
        		if (!frm.maxLimitCanBeHidden()){
        			frm.setDisplayMaxLimit(new Boolean(true));
        			frm.setDisplayOptionForMaxLimit(new Boolean(true));
        		}
        	}
        }

        // Update the classes
        if(op.equalsIgnoreCase(MSG.actionUpdateMultipleClassSetup())) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                doUpdate(request, frm);
                ActionRedirect redirect = new ActionRedirect(mapping.findForward("instructionalOfferingDetail"));
                redirect.addParameter("io", frm.getInstrOfferingId());
                redirect.addParameter("op", "view");
                return redirect;
            }
            else {
                saveErrors(request, errors);
            }
        }

        if (frm.getInstrOfferingId()!=null) {
        	InstructionalOffering io = (new InstructionalOfferingDAO()).get(frm.getInstrOfferingId());
        	if (io!=null)
        		LookupTables.setupDatePatterns(request, sessionContext.getUser(), "Default", null, io.getDepartment(), io.getSession().getDefaultDatePatternNotNull()); // Facility Groups
        }

        frm.setDirectionsClassesCanMove(); //after all classes have been loaded into the form tell the form to determine whether each class can be moved up or down.
        frm.initalizeSubpartSubtotalsAndDisplayFlags();
        frm.initializeEnableAllClassesForStudentScheduling();
        frm.initializeDisplayAllClassInstructors();

        return mapping.findForward("instructionalOfferingModify");
    }

    /**
     * Loads the form with the classes that are part of the instructional offering config
     * @param frm Form object
     * @param instrCoffrConfigId Instructional Offering Config Id
     * @param user User object
     */
    private void doLoad(
    		HttpServletRequest request,
            InstructionalOfferingModifyForm frm,
            String instrOffrConfigId) throws Exception {

        // Check uniqueid
        if(instrOffrConfigId==null || instrOffrConfigId.trim().length()==0)
            throw new Exception (MSG.errorMissingIOConfig());
        
		sessionContext.checkPermission(instrOffrConfigId, "InstrOfferingConfig", Right.MultipleClassSetup);

        // Load details
        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(Long.valueOf(instrOffrConfigId));
        InstructionalOffering io = ioc.getInstructionalOffering();

        frm.setDisplayOptionForMaxLimit(CommonValues.Yes.eq(sessionContext.getUser().getProperty(UserProperty.VariableClassLimits)));
        // Load form properties
        frm.setInstrOffrConfigId(ioc.getUniqueId());
        frm.setInstrOffrConfigLimit(ioc.getLimit());
        frm.setInstrOfferingId(io.getUniqueId());
        String displayInstructorFlags = ApplicationProperties.getProperty("tmtbl.class_setup.show_display_instructor_flags", "false");
        frm.setDisplayDisplayInstructors(new Boolean(displayInstructorFlags));
        String displayEnabledForStudentScheduling = ApplicationProperties.getProperty("tmtbl.class_setup.show_enabled_for_student_scheduling", "true");
        frm.setDisplayEnabledForStudentScheduling(new Boolean(displayEnabledForStudentScheduling));
        String displayExternalIds = ApplicationProperties.getProperty("tmtbl.class_setup.show_display_external_ids", "false");
        frm.setDisplayExternalId(new Boolean(displayExternalIds));
       

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        frm.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception(MSG.errorIOConfigNotDefined());

        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
        for(Iterator it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception(MSG.errorInitialIOSetupIncomplete());
    		if (ss.getParentSubpart() == null){
        		loadClasses(frm, ss.getClasses(), new Boolean(true), new String(), proxy);
        	}
        }
        frm.initializeOrigSubparts();
        frm.setDirectionsClassesCanMove(); //after all classes have been loaded into the form tell the form to determine whether each class can be moved up or down.
        frm.initalizeSubpartSubtotalsAndDisplayFlags();
        frm.initializeEnableAllClassesForStudentScheduling();
        frm.initializeDisplayAllClassInstructors();
    }

    private void loadClasses(InstructionalOfferingModifyForm frm, Set classes, Boolean isReadOnly, String indent, ClassAssignmentProxy proxy){
    	if (classes != null && classes.size() > 0){
    		ArrayList classesList = new ArrayList(classes);
            Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
	    	Boolean readOnlyClass = new Boolean(false);
	    	Class_ cls = null;
	    	boolean first = true;
	    	for(Iterator it = classesList.iterator(); it.hasNext();){
	    		cls = (Class_) it.next();
	    		if (first){
	    			frm.setDisplayEnrollment(new Boolean(StudentClassEnrollment.sessionHasEnrollments(sessionContext.getUser().getCurrentAcademicSessionId())));
	    			first = false;
	    		}
	    		if (!isReadOnly.booleanValue()){
	    			readOnlyClass = new Boolean(isReadOnly.booleanValue());
	    		} else {
	    			readOnlyClass = new Boolean(!sessionContext.hasPermission(cls, Right.MultipleClassSetupClass));
	    		}
				frm.addToClasses(cls, readOnlyClass, indent, proxy, UserProperty.NameFormat.get(sessionContext.getUser()));
	    		loadClasses(frm, cls.getChildClasses(), new Boolean(true), indent + "&nbsp;&nbsp;&nbsp;&nbsp;", proxy);
	    	}
    	}
    }

    /**
     * Update the instructional offering config
     * @param request
     * @param frm
     */
    private void doUpdate(HttpServletRequest request, InstructionalOfferingModifyForm frm)
    	throws Exception {

        // Get Instructional Offering Config
        InstrOfferingConfigDAO iocdao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocdao.get(frm.getInstrOffrConfigId());
        Session hibSession = iocdao.getSession();
    	// Get default room group
		RoomGroup rg = RoomGroup.getGlobalDefaultRoomGroup(ioc.getSession());

		sessionContext.checkPermission(ioc, Right.MultipleClassSetup);

		Transaction tx = null;

        try {
	        tx = hibSession.beginTransaction();

	        // If the instructional offering config limit has changed update it.
	        if (!frm.getInstrOffrConfigLimit().equals(ioc.getLimit())){
	        	ioc.setLimit(frm.getInstrOffrConfigLimit());
	        	hibSession.update(ioc);
	        }

	        // Get map of subpart ownership so that after the classes have changed it is possible to see if the ownership for a subparts has changed
	        HashMap origSubpartManagingDept = new HashMap();
	        if (ioc.getSchedulingSubparts() != null){
	        	SchedulingSubpart ss = null;
	        	for (Iterator it = ioc.getSchedulingSubparts().iterator(); it.hasNext();){
	        		ss = (SchedulingSubpart) it.next();
	        		origSubpartManagingDept.put(ss.getUniqueId(), ss.getManagingDept());
	        	}
	        }

	        // For all added classes, create the classes and save them, get back a map of the temp ids to the new classes
	        HashMap tmpClassIdsToClasses = addClasses(frm, ioc, hibSession);

	        // For all changed classes, update them
	        modifyClasses(frm, ioc, hibSession, rg, tmpClassIdsToClasses);

	        // Update subpart ownership
	        modifySubparts(ioc, origSubpartManagingDept, rg, hibSession);

	        // Delete all classes in the original classes that are no longer in the modified classes
	        deleteClasses(frm, ioc, hibSession, tmpClassIdsToClasses);

	        ioc.getInstructionalOffering().computeLabels(hibSession);

            ChangeLog.addChange(
                    hibSession,
                    sessionContext,
                    ioc,
                    ChangeLog.Source.CLASS_SETUP,
                    ChangeLog.Operation.UPDATE,
                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);

            tx.commit();
	        hibSession.flush();
	        hibSession.refresh(ioc);
	        hibSession.refresh(ioc.getInstructionalOffering());
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr_config.change_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstrOffrConfigChangeAction configChangeAction = (ExternalInstrOffrConfigChangeAction) (Class.forName(className).newInstance());
	       		configChangeAction.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), hibSession);
        	}

        }
        catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
    }

    private void modifySubparts(InstrOfferingConfig ioc, HashMap origSubpartManagingDept, RoomGroup rg, Session hibSession) {
        if (ioc.getSchedulingSubparts() != null){
        	SchedulingSubpart ss = null;
        	Department origManagingDept = null;
        	Department currentManagingDept = null;
        	TimePref tp = null;
        	RoomPref rp = null;
        	BuildingPref bp = null;
        	RoomFeaturePref rfp = null;
        	RoomGroupPref rgp = null;
        	TimePref ntp = null;
        	RoomPref nrp = null;
        	BuildingPref nbp = null;
        	RoomFeaturePref nrfp = null;
        	RoomGroupPref nrgp = null;
        	Class_ c = null;
        	Set prefObjs = null;
        	boolean classChanged;

        	//FacilityGroup deptFacilityGroup = FacilityGroup.getFacilityGroupByReference(Constants.FACILITY_GROUP_DEPT);
        	Department controllingDept = null;
        	PreferenceLevel prefLevel = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired);

        	for (Iterator it = ioc.getSchedulingSubparts().iterator(); it.hasNext();){
        		ss = (SchedulingSubpart) it.next();
        		if (controllingDept == null){
        			controllingDept = ss.getControllingDept();
        		}
        		currentManagingDept = ss.getManagingDept();
        		origManagingDept = (Department)origSubpartManagingDept.get(ss.getUniqueId());
        		if(origManagingDept != null && !currentManagingDept.getUniqueId().equals(origManagingDept.getUniqueId())){

        			if (!origManagingDept.getUniqueId().equals(controllingDept.getUniqueId())){
        				if (ss.getClasses() != null){
        					for (Iterator it2 = ss.getClasses().iterator(); it2.hasNext();){
        						c = (Class_) it2.next();
        						classChanged = false;
        						if (c.getManagingDept().getUniqueId().equals(origManagingDept.getUniqueId())){
               						prefObjs = c.getTimePatterns();
               						for (Iterator it3 = ss.getTimePreferences().iterator();it3.hasNext();){
        								tp = (TimePref) it3.next();
        								if (prefObjs != null && !prefObjs.contains(tp.getTimePattern())){
        									ntp = new TimePref();
        									ntp.setOwner(c);
        									ntp.setPrefLevel(prefLevel);
        									ntp.setTimePattern(tp.getTimePattern());
        									ntp.setPreference(tp.getPreference());
        									c.addTopreferences(ntp);
        									classChanged = true;
        								}
        							}
        							prefObjs = new HashSet();
        							for(Iterator it3 = c.getBuildingPreferences().iterator(); it3.hasNext();){
        								prefObjs.add(((BuildingPref) it3.next()).getBuilding());
        							}
        							for (Iterator it3 = ss.getBuildingPreferences().iterator();it3.hasNext();){
        								bp = (BuildingPref) it3.next();
        								if (!prefObjs.contains(bp.getBuilding())){
        									nbp = new BuildingPref();
        									nbp.setOwner(c);
        									nbp.setPrefLevel(bp.getPrefLevel());
        									nbp.setBuilding(bp.getBuilding());
        									nbp.setDistanceFrom(bp.getDistanceFrom());
        									c.addTopreferences(nbp);
        									classChanged = true;
        								}
        							}
        							prefObjs = new HashSet();
        							for(Iterator it3 = c.getRoomPreferences().iterator(); it3.hasNext();){
        								prefObjs.add(((RoomPref) it3.next()).getRoom());
        							}
        							for (Iterator it3 = ss.getRoomPreferences().iterator();it3.hasNext();){
        								rp = (RoomPref) it3.next();
        								if (!prefObjs.contains(rp.getRoom())){
        									nrp = new RoomPref();
        									nrp.setOwner(c);
        									nrp.setPrefLevel(rp.getPrefLevel());
        									nrp.setRoom(rp.getRoom());
        									c.addTopreferences(nrp);
        									classChanged = true;
        								}
        							}
        							prefObjs = new HashSet();
        							for(Iterator it3 = c.getRoomFeaturePreferences().iterator(); it3.hasNext();){
        								prefObjs.add(((RoomFeaturePref) it3.next()).getRoomFeature());
        							}
        							for (Iterator it3 = ss.getRoomFeaturePreferences().iterator();it3.hasNext();){
        								rfp = (RoomFeaturePref) it3.next();
        								if (!prefObjs.contains(rfp.getRoomFeature())){
        									nrfp = new RoomFeaturePref();
        									nrfp.setOwner(c);
        									nrfp.setPrefLevel(rfp.getPrefLevel());
        									nrfp.setRoomFeature(rfp.getRoomFeature());
        									c.addTopreferences(nrfp);
        									classChanged = true;
        								}
        							}
        							prefObjs = new HashSet();
        							for(Iterator it3 = c.getRoomGroupPreferences().iterator(); it3.hasNext();){
        								prefObjs.add(((RoomGroupPref) it3.next()).getRoomGroup());
        							}
        							for (Iterator it3 = ss.getRoomGroupPreferences().iterator();it3.hasNext();){
        								rgp = (RoomGroupPref) it3.next();
        								if (!prefObjs.contains(rgp.getRoomGroup())){
        									nrgp = new RoomGroupPref();
        									nrgp.setOwner(c);
        									nrgp.setPrefLevel(rgp.getPrefLevel());
        									nrgp.setRoomGroup(rgp.getRoomGroup());
        									c.addTopreferences(nrgp);
        									classChanged = true;
        								}
        							}
        							if (classChanged){
        								hibSession.update(c);
        							}
        						}
        					}
        				}
        			}

        			ss.deleteAllDistributionPreferences(hibSession);

                    boolean weaken = true;
                    if (!currentManagingDept.isExternalManager().booleanValue()) weaken = false;
                    if (weaken && currentManagingDept.isAllowReqTime()!=null && currentManagingDept.isAllowReqTime().booleanValue())
                        weaken = false;
                    if (weaken && ss.getControllingDept().isAllowReqTime()!=null && ss.getControllingDept().isAllowReqTime().booleanValue())
                        weaken = false;

					Set timePrefs = ss.getTimePreferences();
					Set prefs = new HashSet();
					prefs.addAll(ss.getPreferences());
					ss.getPreferences().removeAll(ss.getPreferences());
					for(Iterator it2 = timePrefs.iterator(); it2.hasNext();){
                        TimePref timePref = (TimePref)it2.next();
						tp = new TimePref();
						tp.setOwner(ss);
						tp.setPrefLevel(timePref.getPrefLevel());
						tp.setTimePattern(timePref.getTimePattern());
						tp.setPreference(timePref.getPreference());
                        if (weaken)
                            tp.weakenHardPreferences();
						ss.addTopreferences(tp);
					}

        			if (currentManagingDept.getUniqueId().equals(controllingDept.getUniqueId()) && rg!=null){
    					rgp = new RoomGroupPref();
    					rgp.setOwner(ss);
    					rgp.setPrefLevel(prefLevel);
    					rgp.setRoomGroup(rg);
    					ss.addTopreferences(rgp);
        			}
        			hibSession.update(ss);
        		}
        	}
        }

	}

	private void buildClassList(Set classes, ArrayList lst){
    	if(classes != null && classes.size() > 0){
			ArrayList classesList = new ArrayList(classes);
	        Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
	        Class_ c = null;
	        for (Iterator it2 = classesList.iterator(); it2.hasNext();){
	        	c = (Class_) it2.next();
	        	lst.add(c);
	        	buildClassList(c.getChildClasses(), lst);
	        }
    	}
    }

    private void deleteClasses(InstructionalOfferingModifyForm frm, InstrOfferingConfig ioc, Session hibSession, HashMap tmpClassIdsToRealClasses){
    	if (ioc.getSchedulingSubparts() != null) {
			SchedulingSubpart ss = null;
			ArrayList lst = new ArrayList();
	        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
	        Collections.sort(subpartList, new SchedulingSubpartComparator());

	        for(Iterator it = subpartList.iterator(); it.hasNext();){
	        	ss = (SchedulingSubpart) it.next();
	        	if (ss.getParentSubpart() == null){
	        		buildClassList(ss.getClasses(), lst);
	        	}
	        }

	        Class_ c;
	        for (int i = (lst.size() - 1); i >= 0; i--){
	        	c = (Class_) lst.get(i);
	        	if (!frm.getClassIds().contains(c.getUniqueId().toString()) && !tmpClassIdsToRealClasses.containsValue(c)){
					if (c.getParentClass() != null){
						Class_ parent = c.getParentClass();
						parent.getChildClasses().remove(c);
						hibSession.update(parent);
					}
					c.getSchedulingSubpart().getClasses().remove(c);
					if (c.getPreferences() != null)
					    c.getPreferences().removeAll(c.getPreferences());
					
					c.deleteAllDependentObjects(hibSession, false);
					
					hibSession.delete(c);
	        	}
	        }
    	}
     }

    private HashMap addClasses(InstructionalOfferingModifyForm frm, InstrOfferingConfig ioc, Session hibSession){
    	HashMap tmpClsToRealClass = new HashMap();
		SchedulingSubpartDAO ssdao = new SchedulingSubpartDAO();
		SchedulingSubpart ss = null;
		Class_DAO cdao = new Class_DAO();
		Class_ parentClass = null;
		DepartmentDAO deptdao = new DepartmentDAO();
		Department managingDept = null;
		DatePatternDAO dpdao = new DatePatternDAO();
		DatePattern dp = null;

		Iterator it1 = frm.getClassIds().listIterator();
		Iterator it2 = frm.getSubpartIds().listIterator();
		Iterator it3 = frm.getParentClassIds().listIterator();
		Iterator it4 = frm.getMinClassLimits().listIterator();
		Iterator it5 = frm.getDepartments().listIterator();
		Iterator it6 = frm.getDatePatterns().listIterator();
		Iterator it7 = frm.getNumberOfRooms().listIterator();
		Iterator it8 = frm.getMaxClassLimits().listIterator();
		Iterator it9 = frm.getRoomRatios().listIterator();
		Iterator it10 = frm.getDisplayInstructors().listIterator();
		Iterator it11 = frm.getEnabledForStudentScheduling().listIterator();

		for(;it1.hasNext();){
			Long classId = new Long(it1.next().toString());
			Long subpartId = new Long(it2.next().toString());
			String parentId = it3.next().toString();
			Long parentClassId = null;
			if (parentId.length() != 0)
				parentClassId = new Long(parentId);
			Integer minClassLimit = new Integer(it4.next().toString());
			String managingDeptIdString = (String)it5.next();
			Long managingDeptId = null;
			if (managingDeptIdString.length() != 0)
				managingDeptId = new Long(managingDeptIdString);
			String datePatternId = it6.next().toString();
			Long datePattern = null;
			if (datePatternId.length() != 0)
				datePattern = new Long(datePatternId);
			Integer numberOfRooms = new Integer(it7.next().toString());
			Integer maxClassLimit = new Integer(it8.next().toString());
			Float roomRatio = new Float(it9.next().toString());
			String displayInstructorStr = null;
			if(it10.hasNext())
				displayInstructorStr = (String) it10.next();
			Boolean displayInstructor = new Boolean(false);
			if (displayInstructorStr != null && displayInstructorStr.length() > 0){
				displayInstructor = new Boolean(true);
			}
			String enabledForStudentSchedulingStr = null;
			if (it11.hasNext()) {
				enabledForStudentSchedulingStr = (String) it11.next();
			}
			Boolean enabledForStudentScheduling = new Boolean(false);
			if (enabledForStudentSchedulingStr != null && enabledForStudentSchedulingStr.length() > 0){
				enabledForStudentScheduling = new Boolean(true);
			}

			if (classId.longValue() < 0){
				Class_ newClass = new Class_();
				if (ss == null || !ss.getUniqueId().equals(subpartId))
					ss = ssdao.get(subpartId);
				newClass.setSchedulingSubpart(ss);
				ss.addToclasses(newClass);
				if (parentClassId != null){
					if (parentClassId.longValue() > 0 && (parentClass == null || !parentClass.getUniqueId().equals(parentClassId)))
						parentClass = cdao.get(parentClassId);
					else if (parentClassId.longValue() < 0)
						parentClass = (Class_)tmpClsToRealClass.get(parentClassId);
					newClass.setParentClass(parentClass);
					parentClass.addTochildClasses(newClass);
				}
				if (managingDept == null || !managingDept.getUniqueId().equals(managingDeptId))
					managingDept = deptdao.get(managingDeptId);
				newClass.setControllingDept(ss.getControllingDept());
				newClass.setManagingDept(managingDept);
				if (dp == null || !dp.getUniqueId().equals(datePattern))
					dp = dpdao.get(datePattern);
				newClass.setDatePattern(dp);
				newClass.setExpectedCapacity(minClassLimit);
				newClass.setNbrRooms(numberOfRooms);
				newClass.setMaxExpectedCapacity(maxClassLimit);
				newClass.setRoomRatio(roomRatio);
				newClass.setDisplayInstructor(displayInstructor);
				newClass.setEnabledForStudentScheduling(enabledForStudentScheduling);

				hibSession.save(newClass);
				hibSession.save(ss);
				tmpClsToRealClass.put(classId, newClass);
			}
		}
		return(tmpClsToRealClass);
   }

    private void modifyClasses(InstructionalOfferingModifyForm frm, InstrOfferingConfig ioc, Session hibSession, RoomGroup rg, HashMap tmpClassIdsToRealClasses){
		Class_DAO cdao = new Class_DAO();
		DepartmentDAO deptdao = new DepartmentDAO();
		Department managingDept = null;
		DatePatternDAO dpdao = new DatePatternDAO();
		DatePattern dp = null;

		Iterator it1 = frm.getClassIds().listIterator();
		Iterator it2 = frm.getMinClassLimits().listIterator();
		Iterator it3 = frm.getDepartments().listIterator();
		Iterator it4 = frm.getDatePatterns().listIterator();
		Iterator it5 = frm.getNumberOfRooms().listIterator();
		Iterator it6 = frm.getMaxClassLimits().listIterator();
		Iterator it7 = frm.getRoomRatios().listIterator();
		Iterator it8 = frm.getDisplayInstructors().listIterator();
		Iterator it9 = frm.getEnabledForStudentScheduling().listIterator();
		Iterator it10 = frm.getParentClassIds().listIterator();

		for(;it1.hasNext();){
			Long classId = new Long(it1.next().toString());
			Integer minClassLimit = new Integer(it2.next().toString());
			Long managingDeptId = new Long(it3.next().toString());
			String datePatternId = it4.next().toString();
			Long datePattern = null;
			if (datePatternId.length() != 0)
				datePattern = new Long(datePatternId);
			Integer numberOfRooms = new Integer(it5.next().toString());
			Integer maxClassLimit = new Integer(it6.next().toString());
			Float roomRatio = new Float(it7.next().toString());
			String displayInstructorStr = null;
			if(it8.hasNext()){
				displayInstructorStr = (String) it8.next();
			}
			Boolean displayInstructor = new Boolean(false);
			if (displayInstructorStr != null && displayInstructorStr.length() > 0){
				displayInstructor = new Boolean(true);
			}
			String enabledForStudentSchedulingStr = null;
			if (it9.hasNext()){
				enabledForStudentSchedulingStr = (String)it9.next();
			}
			Boolean enabledForStudentScheduling = new Boolean(false);
			if (enabledForStudentSchedulingStr != null && enabledForStudentSchedulingStr.length() > 0){
				enabledForStudentScheduling = new Boolean(true);
			}

			Long parentClassId = null;
			String parentClassIdString = (String) it10.next();
			if (parentClassIdString != null && parentClassIdString.length() > 0){
				parentClassId = new Long(parentClassIdString);
			}
			if (classId.longValue() > 0){
				boolean changed = false;
				Class_ modifiedClass = cdao.get(classId);
				if (modifiedClass.getParentClass() != null && parentClassId != null){
					if (!modifiedClass.getParentClass().getUniqueId().equals(parentClassId)){
						Class_ origParent = modifiedClass.getParentClass();
						if (parentClassId.longValue() < 0){
							modifiedClass.setParentClass((Class_) tmpClassIdsToRealClasses.get(parentClassId));
						} else {
							modifiedClass.setParentClass(cdao.get(parentClassId));
						}
						hibSession.update(modifiedClass.getParentClass());
						hibSession.update(origParent);
					}
				}
				if (managingDeptId.equals(new Long(-1))){
					managingDeptId = modifiedClass.getControllingDept().getUniqueId();
				}
				if (!modifiedClass.getManagingDept().getUniqueId().equals(managingDeptId)){
						changed = true;
						if (managingDept == null || !managingDept.getUniqueId().equals(managingDeptId))
							managingDept = deptdao.get(managingDeptId);
						modifiedClass.setManagingDept(managingDept);
						Set timePrefs = modifiedClass.getTimePreferences();
						Set prefs = new HashSet();
						prefs.addAll(modifiedClass.getPreferences());
						modifiedClass.getPreferences().removeAll(prefs);

                        boolean weaken = true;
                        if (!managingDept.isExternalManager().booleanValue()) weaken = false;
                        if (weaken && managingDept.isAllowReqTime()!=null && managingDept.isAllowReqTime().booleanValue())
                            weaken = false;
                        if (weaken && modifiedClass.getControllingDept().isAllowReqTime()!=null && modifiedClass.getControllingDept().isAllowReqTime().booleanValue())
                            weaken = false;

						for(Iterator it = timePrefs.iterator(); it.hasNext();){
                            TimePref timePref = (TimePref)it.next();
							TimePattern timePattern = timePref.getTimePattern();
							if (TimePattern.sTypeExactTime==timePattern.getType().intValue()) continue;
							TimePref tp = new TimePref();
							tp.setOwner(modifiedClass);
							tp.setPrefLevel(timePref.getPrefLevel());
							tp.setTimePattern(timePattern);
							tp.setPreference(timePref.getPreference());
                            if (weaken) tp.weakenHardPreferences();
							modifiedClass.addTopreferences(tp);
						}
						modifiedClass.deleteAllDistributionPreferences(hibSession);
					}
				if ((modifiedClass.getDatePattern() == null && datePattern != null) || (modifiedClass.getDatePattern() != null && !modifiedClass.getDatePattern().getUniqueId().equals(datePattern))){
					changed = true;
					if (dp == null || !dp.getUniqueId().equals(datePattern))
						dp = dpdao.get(datePattern);
					modifiedClass.setDatePattern(dp);
				}
				if (!modifiedClass.getExpectedCapacity().equals(minClassLimit)){
					changed = true;
					modifiedClass.setExpectedCapacity(minClassLimit);
				}
				if (!modifiedClass.getNbrRooms().equals(numberOfRooms)){
					changed = true;
					modifiedClass.setNbrRooms(numberOfRooms);
				}
				if (!modifiedClass.getMaxExpectedCapacity().equals(maxClassLimit)){
					changed = true;
					modifiedClass.setMaxExpectedCapacity(maxClassLimit);
				}
				if(!modifiedClass.getRoomRatio().equals(roomRatio)){
					changed = true;
					modifiedClass.setRoomRatio(roomRatio);
				}
		        Boolean displayInstructorFlags = new Boolean(ApplicationProperties.getProperty("tmtbl.class_setup.show_display_instructor_flags", "false"));
				if (displayInstructorFlags && !modifiedClass.isDisplayInstructor().equals(displayInstructor)){
					changed = true;
					modifiedClass.setDisplayInstructor(displayInstructor);
				}
				Boolean displayEnabledForStudentScheduling = new Boolean(ApplicationProperties.getProperty("tmtbl.class_setup.show_enabled_for_student_scheduling", "true"));
				if (displayEnabledForStudentScheduling && !modifiedClass.isEnabledForStudentScheduling().equals(enabledForStudentScheduling)){
					changed = true;
					modifiedClass.setEnabledForStudentScheduling(enabledForStudentScheduling);
				}
				if (changed)
					hibSession.update(modifiedClass);
			}
		}
    }

}
