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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorListUpdateForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.StaffComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "instructorListUpdate", results = {
		@Result(name = "showUpdateInstructorList", type = "tiles", location = "instructorListUpdate.tiles"),
		@Result(name = "showList", type = "redirect", location = "/instructorSearch.action")
	})
@TilesDefinition(name = "instructorListUpdate.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Manage Instructor List"),
		@TilesPutAttribute(name = "body", value = "/user/instructorListUpdate.jsp")
	})
public class InstructorListUpdateAction extends UniTimeAction<InstructorListUpdateForm> {
	private static final long serialVersionUID = 241708052808479802L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	public String execute() throws Exception {
		//Check permissions
		sessionContext.checkPermission(Right.ManageInstructors);
		
		if (form == null) form = new InstructorListUpdateForm();
		fillInDepartmentName();
		
		if (op == null) op = form.getOp();
		
        // Cancel - Go back to Instructors Detail Screen
        if (MSG.actionBackToInstructors().equals(op)) {
        	return "showList";
        }
        
        // Update - Update the instructor and go back to Instructor List Screen
        if (MSG.actionUpdateInstructorsList().equals(op)) {
	        update();
	        return "showList";
        }
		
        // Refresh - set filters
        if (MSG.actionApplyInstructorFilter().equals(op)) {
            request.setAttribute("filterApplied", "1");
        }
        
        Collection assigned = getAssigned();
		if (assigned != null) {
			form.setAssignedInstr(assigned);
		} 
		
		Collection available = getAvailable();
		if (available != null) {
			form.setAvailableInstr(available);
		}
		
		// Get Position Types
		LookupTables.setupPositionTypes(request);
		setupFilters();
		
		form.setInstructors();		
		form.setNameFormat(sessionContext.getUser().getProperty(UserProperty.NameFormat));
		
		return "showUpdateInstructorList";
	}

    private void setupFilters() {
        String[] defaultPosTypes = {"ADMIN_STAFF", "CLERICAL_STAFF", "SERVICE_STAFF", "FELLOWSHIP", "UNDRGRD_TEACH_ASST", "EMERITUS OTHER"};
        boolean filterSet = "1".equals(sessionContext.getUser().getProperty("instrListFilter", "0"));
        String filterApplied = (String) request.getAttribute("filterApplied");
        
        if (filterApplied!=null && !filterApplied.equals("1"))
            filterApplied = null;
        
        if (!filterSet) {
            form.setDisplayListType("both");
            form.setDisplayPosType(defaultPosTypes);
        } 
        else if (filterApplied == null) {            
            form.setDisplayListType(sessionContext.getUser().getProperty("displayListType"));
            String displayPosType = sessionContext.getUser().getProperty("displayPosType");
            if (displayPosType!=null) {
                String[] arr = null;
                if (displayPosType.trim().length()==0) {
                    arr = new String[] {"X"};
                }
                else {
                    StringTokenizer strTok = new StringTokenizer(displayPosType);
                    arr = new String [strTok.countTokens()];
                    int ct = 0;
                    while (strTok.hasMoreTokens()) {
                        arr[ct++] = (String) strTok.nextToken();
                    }
                }    
                form.setDisplayPosType(arr);
            }
        }
        
        if (form.getDisplayListType()==null || form.getDisplayListType().length()==0)
            form.setDisplayListType("both");
        
        if (form.getDisplayPosType()==null) {
            if (filterApplied!=null)
                form.setDisplayPosType(new String[] {"X"});
            else
                form.setDisplayPosType(defaultPosTypes);
        }
        
        sessionContext.getUser().setProperty("instrListFilter", "1");
        sessionContext.getUser().setProperty("displayListType", form.getDisplayListType());
        sessionContext.getUser().setProperty("displayPosType", Constants.arrayToStr(form.getDisplayPosType(), "", " "));
    }

	private void update() throws Exception {
		String[] selectedAssigned = form.getAssignedSelected();
		String[] selectedNotAssigned = form.getAvailableSelected();
		Collection assigned = getAssigned();
		Collection available = getAvailable();
		
		
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>();
		if (selectedAssigned.length != 0)
			for (String id: selectedAssigned) s1.add(id);
		if (selectedNotAssigned.length != 0)
			for (String id: selectedNotAssigned) s2.add(id); 
		
		DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		HashSet<Class_> updatedClasses = new HashSet<Class_>(); 
		try {	
			tx = hibSession.beginTransaction();

			//remove instructor from assigned
			if ( form.getDisplayListType()!=null 
							&& ( form.getDisplayListType().equals("assigned") 
									|| form.getDisplayListType().equals("both")
								) ) { 
			
				for (Iterator iter = assigned.iterator(); iter.hasNext(); ) {
					DepartmentalInstructor inst = (DepartmentalInstructor) iter.next();
					if (!s1.contains(inst.getUniqueId().toString())) {
						if (!inst.getExams().isEmpty() || !inst.getClasses().isEmpty()) continue;
						
                        ChangeLog.addChange(
                                hibSession, 
                                sessionContext, 
                                inst, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.DELETE, 
                                null, inst.getDepartment());

                        for (Iterator i=inst.getClasses().iterator();i.hasNext();) {
				        	ClassInstructor ci = (ClassInstructor)i.next();
				        	updatedClasses.add(ci.getClassInstructing());
				        	ci.getClassInstructing().getClassInstructors().remove(ci);
				        	hibSession.saveOrUpdate(ci);
				        	hibSession.delete(ci);
				        }
					    
				        for (Iterator i=inst.getAssignments().iterator();i.hasNext();) {
				        	Assignment a = (Assignment)i.next();
				        	a.getInstructors().remove(inst);
				        	hibSession.saveOrUpdate(a);
				        }
				        inst.getDepartment().getInstructors().remove(inst);
				        
						hibSession.delete(inst);
					}
				}
			}
			
			if ( form.getDisplayListType()!=null 
					&& ( form.getDisplayListType().equals("available") 
							|| form.getDisplayListType().equals("both")
						) ) { 
	
				//move instructor from staff to department
				for (Iterator iter = available.iterator(); iter.hasNext(); ) {
					Staff staff = (Staff) iter.next();
					if (s2.contains(staff.getUniqueId().toString())) {
						DepartmentalInstructor inst = new DepartmentalInstructor();
						inst.setLastName(staff.getLastName());
						inst.setEmail(staff.getEmail());
						
						String deptId = (String) request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME);
						Department d = new DepartmentDAO().get(Long.valueOf(deptId));
						inst.setDepartment(d);
						d.getInstructors().add(inst);
						
						if (staff.getFirstName() != null) {
							inst.setFirstName(staff.getFirstName());
						}
						if (staff.getMiddleName() != null) {
							inst.setMiddleName(staff.getMiddleName());
						}
						if (staff.getAcademicTitle() != null) {
							inst.setAcademicTitle(staff.getAcademicTitle());
						}
						if (staff.getExternalUniqueId() != null) {
							inst.setExternalUniqueId(staff.getExternalUniqueId());
						}
						if (staff.getPositionType() != null) {
						    inst.setPositionType(staff.getPositionType());
						}
                        
                        inst.setIgnoreToFar(Boolean.FALSE);
                        
						hibSession.save(inst);

                        ChangeLog.addChange(
                                hibSession, 
                                sessionContext, 
                                inst, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.CREATE, 
                                null, inst.getDepartment());
					}
				}
			}
			
			tx.commit();
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
	}

	private Collection getAvailable() throws Exception {
		if (request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
			String deptId = (String) request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME);
			Department d = new DepartmentDAO().get(Long.valueOf(deptId));
			List available = Staff.getStaffByDept(d.getDeptCode().trim(), sessionContext.getUser().getCurrentAcademicSessionId());			
			Collections.sort(available, new StaffComparator(StaffComparator.COMPARE_BY_POSITION));
			return available;
		} else {
			return null;
		}
	}

	private Collection getAssigned() throws Exception {
		String deptId = (String)sessionContext.getAttribute(SessionAttribute.DepartmentId);
		if (deptId != null) {
			List<DepartmentalInstructor> assigned = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));
			Collections.sort(assigned, new DepartmentalInstructorComparator(DepartmentalInstructorComparator.COMPARE_BY_POSITION));
			return assigned;
		} else {
			return null;
		}
	}
	
	private void fillInDepartmentName(){
		String deptId = (String)request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME);
		if (deptId != null) {
			try {
				Department d = new DepartmentDAO().get(Long.valueOf(deptId));
				if (d != null) {
					form.setDeptName(d.getName().trim());
				}
			} catch (Exception e) {
			    Debug.error(e);
			}			
		}
	}
}

