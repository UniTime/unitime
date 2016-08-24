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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.SubjectAreaEditForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 05-15-2007
 * 
 * XDoclet definition:
 * @struts.action path="/subjectAreaEdit" name="subjectAreaEditForm" input="/admin/subjectAreaEdit.jsp" scope="request"
 * @struts.action-forward name="editSubjectArea" path="SubjectAreaEditTile"
 * @struts.action-forward name="addSubjectArea" path="SubjectAreaAddTile"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer, Heston Fernandes
 */
@Service("/subjectAreaEdit")
public class SubjectAreaEditAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	/*
	 * Generated Methods
	 */

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
		
		// Check Access
		sessionContext.checkPermission(Right.SubjectAreas);
		
		SubjectAreaEditForm frm = (SubjectAreaEditForm) form;
		MessageResources rsc = getResources(request);
		ActionMessages errors=null;
		
		// Read operation to be performed
		String op = (frm.getOp()!=null
						? frm.getOp()
						: request.getParameter("op"));
		
        // Add
        if(op.equals(rsc.getMessage("button.addSubjectArea"))) {
        	sessionContext.checkPermission(Right.SubjectAreaAdd);
    		LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
        	return mapping.findForward("addSubjectArea");
        }
        
        // Edit
        if(op.equals(rsc.getMessage("op.edit"))) {
            doLoad(request, frm);
    		LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
        	return mapping.findForward("editSubjectArea");
        }
        
        // Update
        if (op.equals(rsc.getMessage("button.updateSubjectArea"))
        		|| op.equals(rsc.getMessage("button.saveSubjectArea")) ) {
            // Validate input
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
            	doUpdate(request, frm);
            }
        }
        
        // Delete
        if(op.equals(rsc.getMessage("button.deleteSubjectArea"))) {
            errors = frm.validate(mapping, request);
            if(errors.size()==0) {
            	doDelete(request, frm);
            }
        }
        
    	if (frm.getUniqueId()!=null)
       		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, frm.getUniqueId().toString());
    	
    	if (errors!=null && errors.size()>0) {
	        saveErrors(request, errors);
			LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
	        if (frm.getUniqueId()!=null)
	        	return mapping.findForward("editSubjectArea");
	        else
	        	return mapping.findForward("addSubjectArea");
    	}
    	
        return mapping.findForward("back");
	}

	/**
	 * Load the subject area into the form
	 * @param request
	 * @param frm
	 */
	private void doLoad(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Long id = null;
        
        try { 
            id = Long.parseLong(request.getParameter("id"));
        }
        catch (Exception e) {
        	throw new Exception ("Invalid Subject Area IDencountered");
        }
        
    	sessionContext.checkPermission(id, "SubjectArea", Right.SubjectAreaEdit);
        
        SubjectArea sa = new SubjectAreaDAO().get(id);
        frm.setUniqueId(id);
        frm.setAbbv(sa.getSubjectAreaAbbreviation()!=null ? sa.getSubjectAreaAbbreviation() : "");        
        frm.setDepartment(sa.getDepartment()!=null ? sa.getDepartment().getUniqueId() : null);
        frm.setExternalId(sa.getExternalUniqueId() !=null ? sa.getExternalUniqueId() : "");
        frm.setTitle(sa.getTitle() !=null ? sa.getTitle() : "");
	}
	
	/**
	 * Delete Subject Area
	 * @param request
	 * @param frm
	 */
	private void doDelete(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		sessionContext.checkPermission(frm.getUniqueId(), "SubjectArea", Right.SubjectAreaDelete);
		
		try {
			SubjectAreaDAO sdao = new SubjectAreaDAO();
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();

			SubjectArea sa = sdao.get(frm.getUniqueId());

        	String className = ApplicationProperty.ExternalActionCourseOfferingRemove.value();
			if (className != null && className.trim().length() > 0){
        		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).newInstance());
    			for (Iterator i = sa.getCourseOfferings().iterator(); i.hasNext(); ) {
    	        	CourseOffering co = (CourseOffering) i.next();
    	        	removeAction.performExternalCourseOfferingRemoveAction(co, hibSession);
    	        }   		
        	}

			Set s = sa.getInstructionalOfferings();
			for (Iterator i = s.iterator(); i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering) i.next();
				io.deleteAllDistributionPreferences(hibSession);
				io.deleteAllClasses(hibSession);
				io.deleteAllCourses(hibSession);
				hibSession.delete(io);
			}

	        for (Iterator i = sa.getCourseOfferings().iterator(); i.hasNext(); ) {
	        	CourseOffering co = (CourseOffering) i.next();
	        	hibSession.delete(co);
	        }
	        
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sa, 
                    ChangeLog.Source.SUBJECT_AREA_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    sa.getDepartment());
            
            hibSession.delete(sa);
			
			tx.commit();
			hibSession.flush();

			HibernateUtil.clearCache();
		}
		catch (Exception e) {
			if (tx!=null)
				tx.rollback();
			
			throw (e);
		}
	}

	/**
	 * Update Subject Area
	 * @param request
	 * @param frm
	 */
	private void doUpdate(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		if (frm.getUniqueId() == null)
			sessionContext.checkPermission(Right.SubjectAreaAdd);
		else
			sessionContext.checkPermission(frm.getUniqueId(), "SubjectArea", Right.SubjectAreaEdit);
		
		try {
			SubjectAreaDAO sdao = new SubjectAreaDAO();
			DepartmentDAO ddao = new DepartmentDAO();
			
			SubjectArea sa = null;
			Department oldDept = null;
			
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();
			
			if (frm.getUniqueId()!=null) 
				sa = sdao.get(frm.getUniqueId());
			else 
				sa = new SubjectArea();
			
			Department dept = ddao.get(frm.getDepartment());
            HashSet<Class_> updatedClasses = new HashSet<Class_>();
			
			sa.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId(), hibSession));
	        sa.setSubjectAreaAbbreviation(frm.getAbbv());
	        if (sa.getDepartment()!=null && !dept.equals(sa.getDepartment())) {
	            HashSet availableRooms = new HashSet();
	            HashSet availableBuildings = new HashSet();
	            for (Iterator i=dept.getRoomDepts().iterator();i.hasNext();) {
	                RoomDept roomDept = (RoomDept)i.next();
	                availableRooms.add(roomDept.getRoom());
	                if (roomDept.getRoom() instanceof Room)
	                    availableBuildings.add(((Room)roomDept.getRoom()).getBuilding());
	            }
	            for (Iterator i=sa.getCourseOfferings().iterator();i.hasNext();) {
	                CourseOffering co = (CourseOffering)i.next();
	                if (!co.getIsControl() || co.getInstructionalOffering()==null) continue;
	                for (Iterator j=co.getInstructionalOffering().getInstrOfferingConfigs().iterator();j.hasNext();) {
	                    InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
	                    for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
	                        SchedulingSubpart ss = (SchedulingSubpart)k.next();
	                        if (!ss.getManagingDept().isExternalManager()) {
	                            for (Iterator l=ss.getPreferences().iterator();l.hasNext();) {
	                                Preference p = (Preference)l.next();
	                                if (p instanceof TimePref) continue;
	                                if (p instanceof RoomPref) {
	                                    RoomPref rp = (RoomPref)p;
	                                    if (!availableRooms.contains(rp.getRoom())) l.remove();
                                    } else if (p instanceof BuildingPref) {
                                        BuildingPref bp = (BuildingPref)p;
                                        if (!availableBuildings.contains(bp.getBuilding())) l.remove();
	                                } else if (p instanceof RoomFeaturePref) {
	                                    RoomFeaturePref rfp = (RoomFeaturePref)p;
	                                    if (rfp.getRoomFeature() instanceof DepartmentRoomFeature) l.remove();
	                                } else if (p instanceof RoomGroupPref) {
	                                    RoomGroupPref rgp = (RoomGroupPref)p;
	                                    if (!rgp.getRoomGroup().isGlobal()) l.remove();
	                                }
	                            }
	                            hibSession.saveOrUpdate(ss);
	                        }
	                        for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
	                            Class_ c = (Class_)l.next();
	                            if (!c.getManagingDept().isExternalManager()) {
	                                for (Iterator m=c.getPreferences().iterator();m.hasNext();) {
	                                    Preference p = (Preference)m.next();
	                                    if (p instanceof TimePref) continue;
	                                    if (p instanceof RoomPref) {
	                                        RoomPref rp = (RoomPref)p;
	                                        if (!availableRooms.contains(rp.getRoom())) m.remove();
	                                    } else if (p instanceof BuildingPref) {
	                                        BuildingPref bp = (BuildingPref)p;
	                                        if (!availableBuildings.contains(bp.getBuilding())) m.remove();
	                                    } else if (p instanceof RoomFeaturePref) {
	                                        RoomFeaturePref rfp = (RoomFeaturePref)p;
	                                        if (rfp.getRoomFeature() instanceof DepartmentRoomFeature) m.remove();
	                                    } else if (p instanceof RoomGroupPref) {
	                                        RoomGroupPref rgp = (RoomGroupPref)p;
	                                        if (!rgp.getRoomGroup().isGlobal()) m.remove();
	                                    }
	                                }
	                                c.setManagingDept(dept, sessionContext.getUser(), hibSession);
	                            }
	                            for (Iterator m=c.getClassInstructors().iterator();m.hasNext();) {
	                                ClassInstructor ci = (ClassInstructor)m.next();
	                                DepartmentalInstructor newInstructor = null;
	                                if (ci.getInstructor().getExternalUniqueId()!=null) {
	                                    newInstructor = DepartmentalInstructor.findByPuidDepartmentId(
	                                            ci.getInstructor().getExternalUniqueId(), dept.getUniqueId());
	                                }
	                                ci.getInstructor().getClasses().remove(ci);
	                                hibSession.saveOrUpdate(ci.getInstructor());
                                    if (newInstructor!=null) {
                                        ci.setInstructor(newInstructor);
                                        newInstructor.getClasses().add(ci);
                                        hibSession.saveOrUpdate(newInstructor);
                                    } else {
                                        m.remove();
                                        hibSession.delete(ci);
                                    }
	                            }
	                            hibSession.saveOrUpdate(c);
                                updatedClasses.add(c);
	                        }
	                    }
	                }
	            }
	            
	            for (Iterator i=sa.getDepartment().getPreferences().iterator();i.hasNext();) {
	                Preference p = (Preference)i.next();
	                if (p instanceof DistributionPref) {
	                    DistributionPref dp = (DistributionPref)p;
	                    boolean change = true;
	                    for (Iterator j=dp.getOrderedSetOfDistributionObjects().iterator();j.hasNext();) {
	                        DistributionObject dobj = (DistributionObject)j.next();
	                        if (dobj.getPrefGroup() instanceof SchedulingSubpart) {
	                            SchedulingSubpart ss = (SchedulingSubpart)dobj.getPrefGroup();
	                            if (!ss.getControllingCourseOffering().getSubjectArea().equals(sa)) change=false;
	                             break;
	                        } else if (dobj.getPrefGroup() instanceof Class_) {
	                            Class_ c = (Class_)dobj.getPrefGroup();
	                            if (!c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().equals(sa)) change=false;
                                break;
	                        }
	                    }
	                    if (change) {
                            dp.setOwner(dept);
                            hibSession.saveOrUpdate(dp);
	                    }
	                }
	            }
	            oldDept = sa.getDepartment();
	            sa.setDepartment(dept);
	        } else if (sa.getDepartment()==null) {
	            sa.setDepartment(dept);
	        }
	        sa.setExternalUniqueId(frm.getExternalId());
	        sa.setTitle(frm.getTitle());
	        
	        hibSession.saveOrUpdate(sa);			
			
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sa, 
                    ChangeLog.Source.SUBJECT_AREA_EDIT, 
                    (frm.getUniqueId()==null?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    sa, 
                    dept);

            tx.commit();			
			hibSession.refresh(sa);
			hibSession.flush();
			hibSession.refresh(sa.getSession());
			if (oldDept!=null) {
			    hibSession.refresh(oldDept); hibSession.refresh(sa.getDepartment());
			}
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}		
		}
		catch (Exception e) {
			if (tx!=null)
				tx.rollback();
			
			throw (e);
		}
	}
}
