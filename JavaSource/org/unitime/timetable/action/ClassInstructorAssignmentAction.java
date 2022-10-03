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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ClassInstructorAssignmentForm;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;

/**
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value="classInstructorAssignment", results = {
		@Result(name = "classInstructorAssignment", type = "tiles", location = "classInstructorAssignment.tiles"),
		@Result(name = "instructionalOfferingDetail", type = "redirect", location = "/instructionalOfferingDetail.action", 
				params = { "io", "${form.instrOfferingId}", "op", "view"})
	})
@TilesDefinition(name = "classInstructorAssignment.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Assign Instructors"),
		@TilesPutAttribute(name = "body", value = "/user/classInstructorAssignment.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
})
public class ClassInstructorAssignmentAction extends UniTimeAction<ClassInstructorAssignmentForm> {
	private static final long serialVersionUID = -8297282704645449996L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected String op2 = null;
	private Long uid = null;
	
	public String getHdnOp() { return op2; }
	public void setHdnOp(String hdnOp) { this.op2 = hdnOp; }
	public Long getUid() { return uid; }
	public void setUid(Long uid) { this.uid = uid; }


    public String execute() throws Exception {
        if (form == null) {
        	form = new ClassInstructorAssignmentForm();
        }

        // Get operation
    	if (op == null) op = form.getOp();
    	if (op2 != null && !op2.isEmpty()) op = op2;

        if (op==null || op.trim().length()==0)
            throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
        
        if (op.equals(MSG.actionBackToIODetail()))
        	return "instructionalOfferingDetail";

        // Instructional Offering Config Id
        Long instrOffrConfigId = uid;
        if (uid == null) instrOffrConfigId = form.getInstrOffrConfigId();

        // Set the operation
        form.setOp(op);

        // Set the proxy so we can get the class time and room
        form.setProxy(getClassAssignmentService().getAssignment());

        InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(instrOffrConfigId);
        form.setInstrOffrConfigId(instrOffrConfigId);
        
        sessionContext.checkPermission(ioc, Right.AssignInstructors);

        ArrayList instructors = new ArrayList(ioc.getDepartment().getInstructors());
	    Collections.sort(instructors, new DepartmentalInstructorComparator());
        request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME, instructors);
        LookupTables.setupInstructorTeachingResponsibilities(request);

        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionAssignInstructors())) {
            doLoad(instrOffrConfigId, ioc);
        }
        
		if(op.equals(MSG.actionUpdateClassInstructorsAssignment()) ||
        		op.equals(MSG.actionNextIO()) ||
        		op.equals(MSG.actionPreviousIO()) ||
        		op.equals(MSG.actionUnassignAllInstructorsFromConfig())) {

            if (op.equals(MSG.actionUnassignAllInstructorsFromConfig())) {
            	form.unassignAllInstructors();
            }

        	// Validate input prefs
            form.validate(this);

            // No errors - Update class
            if (!hasFieldErrors()) {

            	try {
            		form.updateClasses();

                    InstrOfferingConfig cfg = new InstrOfferingConfigDAO().get(form.getInstrOffrConfigId());

                    org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
                    ChangeLog.addChange(
                    		hibSession,
                            sessionContext,
                            cfg,
                            ChangeLog.Source.CLASS_INSTR_ASSIGN,
                            ChangeLog.Operation.UPDATE,
                            cfg.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                            null);
                    
                	if (getPermissionOfferingLockNeeded().check(sessionContext.getUser(), cfg.getInstructionalOffering())) {
                		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), cfg.getInstructionalOffering().getSessionId(), cfg.getInstructionalOffering().getUniqueId());
                	}
                	
                	hibSession.flush();

                	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
                	if (className != null && className.trim().length() > 0){
        	        	ExternalInstrOfferingConfigAssignInstructorsAction assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).getDeclaredConstructor().newInstance());
        	       		assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, InstrOfferingConfigDAO.getInstance().getSession());
                	}

    	            if (op.equals(MSG.actionNextIO())) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.action?uid="+form.getNextId()+"&op="+URLEncoder.encode(MSG.actionAssignInstructors(), "UTF-8")));
    	            	return null;
    	            }

    	            if (op.equals(MSG.actionPreviousIO())) {
    	            	response.sendRedirect(response.encodeURL("classInstructorAssignment.action?uid="+form.getPreviousId()+"&op="+URLEncoder.encode(MSG.actionAssignInstructors(), "UTF-8")));
    	            	return null;
    	            }
                    return "instructionalOfferingDetail";
            	} catch (Exception e) {
            		throw e;
            	}
            }
        }

        if (op.equals("Delete")) {
        	form.deleteInstructor();
        }

        if (op.equals("Add Instructor")) {
        	form.addInstructor();
        }

        return "classInstructorAssignment";
    }

	/**
     * Loads the form with the classes that are part of the instructional offering config
     */
    private void doLoad(Long instrOffrConfigId, InstrOfferingConfig ioc) throws Exception {

        // Check uniqueid
        if (instrOffrConfigId==null)
            throw new Exception (MSG.exceptionMissingIOConfig());

        // Load details
        InstructionalOffering io = ioc.getInstructionalOffering();

        // Load form properties
        form.setInstrOffrConfigId(ioc.getUniqueId());
        form.setInstrOffrConfigLimit(ioc.getLimit());
        form.setInstrOfferingId(io.getUniqueId());

        form.setDisplayExternalId(ApplicationProperty.ClassSetupShowExternalIds.isTrue());

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        form.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception(MSG.exceptionIOConfigUndefined());

        InstrOfferingConfig config = ioc.getNextInstrOfferingConfig(sessionContext);
        if(config != null) {
        	form.setNextId(config.getUniqueId().toString());
        } else {
        	form.setNextId(null);
        }

        config = ioc.getPreviousInstrOfferingConfig(sessionContext);
        if(config != null) {
            form.setPreviousId(config.getUniqueId().toString());
        } else {
            form.setPreviousId(null);
        }

        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());

        for(Iterator it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception(MSG.exceptionInitialIOSetupIncomplete());
    		if (ss.getParentSubpart() == null){
        		loadClasses(form, ss.getClasses(), new String());
        	}
        }
        
        String coordinators = "";
        String instructorNameFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
        for (OfferingCoordinator coordinator: new TreeSet<OfferingCoordinator>(io.getOfferingCoordinators())) {
        	if (!coordinators.isEmpty()) coordinators += "<br>";
        	coordinators += "<a href='instructorDetail.action?instructorId=" + coordinator.getInstructor().getUniqueId() + "' class='noFancyLinks'>" +
        			coordinator.getInstructor().getName(instructorNameFormat) +
        			(coordinator.getResponsibility() == null ? 
        					(coordinator.getPercentShare() != 0 ? " (" + coordinator.getPercentShare() + "%)" : "") :
        					" (" + coordinator.getResponsibility().getLabel() + (coordinator.getPercentShare() > 0 ? ", " + coordinator.getPercentShare() + "%" : "") + ")") + 
        			"</a>";
        }
        form.setCoordinators(coordinators);

    }

    private void loadClasses(ClassInstructorAssignmentForm form, Set classes, String indent){
    	if (classes != null && classes.size() > 0){
    		ArrayList classesList = new ArrayList(classes);

    		if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(sessionContext.getUser()))) {
        		Collections.sort(classesList,
        			new ClassCourseComparator(
        					sessionContext.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					form.getProxy(),
        					false
        			)
        		);
        	} else {
        		Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
        	}

	    	Class_ cls = null;
	    	for(Iterator it = classesList.iterator(); it.hasNext();){
	    		cls = (Class_) it.next();
	    		form.addToClasses(cls, !sessionContext.hasPermission(cls, Right.AssignInstructorsClass), indent);
	    		loadClasses(form, cls.getChildClasses(), indent + "&nbsp;&nbsp;&nbsp;&nbsp;");
	    	}
    	}
    }
    
    protected Permission<InstructionalOffering> getPermissionOfferingLockNeeded() {
    	return getPermission("permissionOfferingLockNeeded");
    }
    
    public String getCrsNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
    
    private String nameFormat = null;
    public String getNameFormat() {
    	if (nameFormat == null)
    		nameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
    	return nameFormat;
    }
}
