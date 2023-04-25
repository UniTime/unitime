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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.SubjectAreaEditForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
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
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer, Heston Fernandes
 */
@Action(value = "subjectAreaEdit", results = {
		@Result(name = "add", type = "tiles", location = "subjectAreaAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "subjectAreaEdit.tiles"),
		@Result(name = "back", type = "redirect", location="/subjectList.action", params = {
				"anchor", "${form.uniqueId}"})
	})
@TilesDefinitions({
@TilesDefinition(name = "subjectAreaAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Subject Area"),
		@TilesPutAttribute(name = "body", value = "/admin/subjectAreaEdit.jsp")
	}),
@TilesDefinition(name = "subjectAreaEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Subject Area"),
		@TilesPutAttribute(name = "body", value = "/admin/subjectAreaEdit.jsp")
	})
})
public class SubjectAreaEditAction extends UniTimeAction<SubjectAreaEditForm> {
	private static final long serialVersionUID = 5726515478142736794L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	
	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new SubjectAreaEditForm();
		
		// Check Access
		sessionContext.checkPermission(Right.SubjectAreas);
		
        // Add
        if (stripAccessKey(MSG.buttonAddSubjectArea()).equals(op)) {
        	sessionContext.checkPermission(Right.SubjectAreaAdd);
        	form.reset();
    		LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
        	return "add";
        }
        
        // Edit
        if ("edit".equals(op)) {
            doLoad();
    		LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
        	return "edit";
        }
        
        // Update
        if (stripAccessKey(MSG.buttonSave()).equals(op) || stripAccessKey(MSG.buttonUpdate()).equals(op)) {
            // Validate input
        	form.validate(this);
        	if (!hasFieldErrors()) {
            	doUpdate();
            }
        }
        
        // Delete
        if (stripAccessKey(MSG.buttonDelete()).equals(op)) {
        	form.validate(this);
        	if (!hasFieldErrors()) {
            	doDelete();
            }
        }
        
    	if (hasFieldErrors()) {
			LookupTables.setupNonExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
	        return (form.getUniqueId() != null ? "edit" : "add");
    	}
    	
        return "back";
	}

	/**
	 * Load the subject area into the form
	 */
	private void doLoad() throws Exception {
    	sessionContext.checkPermission(id, "SubjectArea", Right.SubjectAreaEdit);
        SubjectArea sa = SubjectAreaDAO.getInstance().get(id);
        form.setUniqueId(id);
        form.setAbbv(sa.getSubjectAreaAbbreviation()!=null ? sa.getSubjectAreaAbbreviation() : "");        
        form.setDepartment(sa.getDepartment()!=null ? sa.getDepartment().getUniqueId() : null);
        form.setExternalId(sa.getExternalUniqueId() !=null ? sa.getExternalUniqueId() : "");
        form.setTitle(sa.getTitle() !=null ? sa.getTitle() : "");
	}
	
	/**
	 * Delete Subject Area
	 */
	private void doDelete() throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		sessionContext.checkPermission(form.getUniqueId(), "SubjectArea", Right.SubjectAreaDelete);
		
		try {
			SubjectAreaDAO sdao = SubjectAreaDAO.getInstance();
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();

			SubjectArea sa = sdao.get(form.getUniqueId());

        	String className = ApplicationProperty.ExternalActionCourseOfferingRemove.value();
			if (className != null && className.trim().length() > 0){
        		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).getDeclaredConstructor().newInstance());
    			for (Iterator i = sa.getCourseOfferings().iterator(); i.hasNext(); ) {
    	        	CourseOffering co = (CourseOffering) i.next();
    	        	removeAction.performExternalCourseOfferingRemoveAction(co, hibSession);
    	        }   		
        	}

			for (CourseOffering co: sa.getCourseOfferings()) {
				if (!co.isIsControl()) continue;
				InstructionalOffering io = co.getInstructionalOffering();
				io.deleteAllDistributionPreferences(hibSession);
				io.deleteAllClasses(hibSession);
				io.deleteAllCourses(hibSession);
				hibSession.remove(io);
			}

	        for (Iterator i = sa.getCourseOfferings().iterator(); i.hasNext(); ) {
	        	CourseOffering co = (CourseOffering) i.next();
	        	hibSession.remove(co);
	        }
	        
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sa, 
                    ChangeLog.Source.SUBJECT_AREA_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    sa.getDepartment());
            
            hibSession.remove(sa);
			
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
	 */
	private void doUpdate() throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		if (form.getUniqueId() == null)
			sessionContext.checkPermission(Right.SubjectAreaAdd);
		else
			sessionContext.checkPermission(form.getUniqueId(), "SubjectArea", Right.SubjectAreaEdit);
		
		try {
			SubjectAreaDAO sdao = SubjectAreaDAO.getInstance();
			DepartmentDAO ddao = DepartmentDAO.getInstance();
			
			SubjectArea sa = null;
			Department oldDept = null;
			
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();
			
			if (form.getUniqueId()!=null) 
				sa = sdao.get(form.getUniqueId());
			else 
				sa = new SubjectArea();
			
			Department dept = ddao.get(form.getDepartment());
            HashSet<Class_> updatedClasses = new HashSet<Class_>();
			
			sa.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId(), hibSession));
	        sa.setSubjectAreaAbbreviation(form.getAbbv());
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
                                        hibSession.remove(ci);
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
	        sa.setExternalUniqueId(form.getExternalId());
	        sa.setTitle(form.getTitle());
	        
	        hibSession.saveOrUpdate(sa);
			
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sa, 
                    ChangeLog.Source.SUBJECT_AREA_EDIT, 
                    (form.getUniqueId()==null?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    sa, 
                    dept);

	        form.setUniqueId(sa.getUniqueId());
	        
            tx.commit();			
			hibSession.refresh(sa);
			hibSession.flush();
			hibSession.refresh(sa.getSession());
			if (oldDept!=null) {
			    hibSession.refresh(oldDept); hibSession.refresh(sa.getDepartment());
			}
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
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
	
	public String getSession() {
		return sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel();
	}
}
