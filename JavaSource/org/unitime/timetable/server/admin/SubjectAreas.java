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
package org.unitime.timetable.server.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
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
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Sean Justice
 */
@Service("gwtAdminTable[type=subjectArea]")
public class SubjectAreas implements AdminTable {
	private static Log sLog = LogFactory.getLog(SubjectAreas.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSubjectArea(), MESSAGES.pageSubjectAreas());
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<Department> deptList = DepartmentDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId());
		List<ListItem> depts = new ArrayList<ListItem>();
		List<ListItem> fundDepts = new ArrayList<ListItem>();

		for (Department dept: Department.getUserDepartments(context.getUser())) {
			if (dept.isExternalManager() == null || !dept.isExternalManager()) {
				depts.add(new ListItem(dept.getUniqueId().toString(), dept.getLabel()));
			}
		}
		fundDepts.add(new ListItem("-1", MESSAGES.noFundingDepartment()));
		Collections.sort(deptList);
		for (Department dept: deptList) {
			if (dept.isExternalFundingDept() != null && dept.isExternalFundingDept()) {
				fundDepts.add(new ListItem(dept.getUniqueId().toString(), dept.getLabel()));
			}
		}
		boolean dispLastChange = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
		HashMap<Long, String> subjToChanges = new HashMap<Long, String>();
		Field lastChangeField = null;
		if (dispLastChange) {
			lastChangeField = new Field(MESSAGES.fieldLastChange(), FieldType.text, 50, Flag.READ_ONLY, Flag.NO_DETAIL);
			subjToChanges = lastChangeForAllSubjects(context, hibSession);
		} else {
			lastChangeField = new Field(MESSAGES.fieldLastChange(), FieldType.text, 50, Flag.READ_ONLY, Flag.HIDDEN);
		}
		HashMap<Long, String> deptToManagers = managersForAllDepts(context, hibSession);
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbv(), FieldType.text, 100, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldTitle(), FieldType.text, 120, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.UNIQUE_IF_SET),
				new Field(MESSAGES.fieldDepartment(), FieldType.list, 300, depts, Flag.NOT_EMPTY),
				(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()? new Field(MESSAGES.fieldFundingDepartment(), FieldType.list, 300, fundDepts) : new Field(MESSAGES.fieldFundingDepartment(), FieldType.list, 300, fundDepts, Flag.HIDDEN)),
				new Field(MESSAGES.fieldManagers(), FieldType.textarea, 100, Flag.READ_ONLY, Flag.NO_DETAIL),
				lastChangeField
		);
		data.setSortBy(1,2);
		for (SubjectArea area: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(area.getUniqueId());
			boolean canEdit = context.hasPermission(area, Right.SubjectAreaEdit);
			r.setField(0, area.getSubjectAreaAbbreviation(), canEdit);
			r.setField(1, area.getTitle(), canEdit);
			r.setField(2, area.getExternalUniqueId(), canEdit);
			r.setField(3, (canEdit ? area.getDepartment().getUniqueId().toString() : area.getDepartment().getLabel()), canEdit && context.hasPermission(area, Right.SubjectAreaChangeDepartment));
			if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
			  r.setField(4, area.getFundingDept() == null?"-1":area.getFundingDept().getUniqueId().toString(), canEdit);
			}
			r.setDeletable(context.hasPermission(area, Right.SubjectAreaDelete));
			String managers = deptToManagers.get(area.getDepartment().getUniqueId());
			if (managers == null) {
				managers = "";
			}
            r.setField(5, managers);
            if (dispLastChange) {
            	String changes = subjToChanges.get(area.getUniqueId());
            	if (changes != null && !changes.isEmpty()) {
            		r.setField(6, changes);
            	}
            }
		}
		data.setAddable(context.hasPermission(Right.SubjectAreaAdd));
		data.setEditable(context.hasPermission(Right.SubjectAreaEdit) || context.hasPermission(Right.SubjectAreaAdd));
		if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ConfirmationDialogs)))
			data.setConfirmDelete(MESSAGES.confirmDeleteSubjectArea());
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<Long, String> managersForAllDepts(SessionContext context, Session hibSession){
		HashMap<Long, String> subjToManagers = new HashMap<Long, String>();
		String instrNameFormat = UserProperty.NameFormat.get(context.getUser());
		
		String query = "select d.uniqueId, tm from Department d inner join d.timetableManagers as tm where d.session.uniqueId = :sessionId";
		for(Object[] result : hibSession.createQuery(query, Object[].class).setParameter("sessionId", context.getUser().getCurrentAcademicSessionId()).list()) {
			long deptId = (long)result[0];
			TimetableManager tm = (TimetableManager)result[1];
			String tmName = subjToManagers.get(deptId);
			if (tmName == null) {
				tmName = tm.getName(instrNameFormat);
			} else {
				tmName += "\n" + tm.getName(instrNameFormat);
			}
			subjToManagers.put(deptId, tmName);
		}
		
		return subjToManagers;
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<Long, String> lastChangeForAllSubjects(SessionContext context, Session hibSession) {
		HashMap<Long, String> subjToChanges = new HashMap<Long, String>();
		String query = "select cl from ChangeLog cl where cl.uniqueId in (select max(cl2.uniqueId) from ChangeLog cl2 where cl2.session.uniqueId = :sessionId and cl2.subjectArea.session.uniqueId = :sessionId group by cl2.subjectArea.uniqueId)";
		for (ChangeLog cl : hibSession.createQuery(query, ChangeLog.class).setParameter("sessionId", context.getUser().getCurrentAcademicSessionId()).list()) {
			if (cl.getSubjectArea() == null) {
				continue;
			}
			String changes = subjToChanges.get(cl.getSubjectArea().getUniqueId());
			if (changes == null) {
				changes = MESSAGES.lastChange(ChangeLog.sDFdate.format(cl.getTimeStamp()), cl.getManager().getShortName());
				subjToChanges.put(cl.getSubjectArea().getUniqueId(), changes);
			} else {
				changes += "\n" + MESSAGES.lastChange(ChangeLog.sDFdate.format(cl.getTimeStamp()), cl.getManager().getShortName());
			}
		}
		return subjToChanges;
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (SubjectArea area: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(area.getUniqueId());
			if (r == null) {
				if (context.hasPermission(area, Right.SubjectAreaDelete)) {
					delete(area, context, hibSession);
				}
			} else {
				if (context.hasPermission(area, Right.SubjectAreaEdit)) {
					update(area, r, context, hibSession);
				}
			}
		}
		if (context.hasPermission(Right.SubjectAreaAdd)) {
			for (Record r: data.getNewRecords())			
				save(r, context, hibSession);
		}
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreaAdd')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Department dept = DepartmentDAO.getInstance().get(Long.valueOf(record.getField(3)), hibSession);
		Department fundDept = (record.getField(4) == null || "-1".equals(record.getField(4)) ? null : DepartmentDAO.getInstance().get(Long.valueOf(record.getField(4)), hibSession));

		SubjectArea area = new SubjectArea();
		area.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		area.setSubjectAreaAbbreviation(record.getField(0));
		area.setTitle(record.getField(1));
		area.setExternalUniqueId(record.getField(2));
		area.setDepartment(dept);
		dept.getSubjectAreas().add(area);
		area.setFundingDept(fundDept);
		
		hibSession.persist(area);
		record.setUniqueId(area.getUniqueId());
		if (fundDept == null) record.setField(4, "-1");
		String instrNameFormat = UserProperty.NameFormat.get(context.getUser());
		String managers = "";
		for (TimetableManager mgr: area.getManagers())
			managers += (managers.isEmpty() ? "" : "\n") + mgr.getName(instrNameFormat);
        record.setField(5, managers);
        record.setField(6, MESSAGES.lastChange(ChangeLog.sDFdate.format(new Date()), context.getUser().getName()));
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.CREATE,
			    area,
			    area.getDepartment());

	}	

	protected void update(SubjectArea area, Record record, SessionContext context, Session hibSession) {
		if (area==null) return;
		boolean changed = false;
		boolean fundingEnabled = ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue();
		Department dept = DepartmentDAO.getInstance().get(Long.valueOf(record.getField(3)), hibSession);
		Department fundDept = (record.getField(4) == null ? null : DepartmentDAO.getInstance().get(Long.valueOf(record.getField(4)), hibSession));
		changed =
			changed ||
			!ToolBox.equals(area.getSubjectAreaAbbreviation(), record.getField(0)) ||
			!ToolBox.equals(area.getTitle(), record.getField(1)) ||
			!ToolBox.equals(area.getExternalUniqueId(), record.getField(2)) ||
			!ToolBox.equals(dept, area.getDepartment()) || 
			(fundingEnabled && !ToolBox.equals(fundDept, area.getFundingDept()));
		
        Set<Class_> updatedClasses = new HashSet<Class_>();
		if (changed) {
			area.setSubjectAreaAbbreviation(record.getField(0));
			area.setTitle(record.getField(1));
			area.setExternalUniqueId(record.getField(2));
			if (!dept.equals(area.getDepartment())) {
				Set<Location> availableRooms = new HashSet<Location>();
	            Set<Building> availableBuildings = new HashSet<Building>();
	            for (RoomDept roomDept: dept.getRoomDepts()) {
	                availableRooms.add(roomDept.getRoom());
	                if (roomDept.getRoom() instanceof Room)
	                    availableBuildings.add(((Room)roomDept.getRoom()).getBuilding());
	            }
	            for (CourseOffering co: area.getCourseOfferings()) {
	            	if (!co.getIsControl() || co.getInstructionalOffering() == null) continue;
	            	for (InstrOfferingConfig ioc: co.getInstructionalOffering().getInstrOfferingConfigs()) {
	                    for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
	                        if (!ss.getManagingDept().isExternalManager()) {
	                        	for (Iterator<Preference> i = ss.getPreferences().iterator(); i.hasNext(); ) {
	                        		Preference p = i.next();
	                                if (p instanceof TimePref) continue;
	                                if (p instanceof RoomPref) {
	                                    RoomPref rp = (RoomPref)p;
	                                    if (!availableRooms.contains(rp.getRoom())) i.remove();
                                    } else if (p instanceof BuildingPref) {
                                        BuildingPref bp = (BuildingPref)p;
                                        if (!availableBuildings.contains(bp.getBuilding())) i.remove();
	                                } else if (p instanceof RoomFeaturePref) {
	                                    RoomFeaturePref rfp = (RoomFeaturePref)p;
	                                    if (rfp.getRoomFeature() instanceof DepartmentRoomFeature) i.remove();
	                                } else if (p instanceof RoomGroupPref) {
	                                    RoomGroupPref rgp = (RoomGroupPref)p;
	                                    if (!rgp.getRoomGroup().isGlobal()) i.remove();
	                                }
	                            }
	                            hibSession.merge(ss);
	                        }
	                        for (Class_ c: ss.getClasses()) {
	                            if (!c.getManagingDept().isExternalManager()) {
	                            	for (Iterator<Preference> i = c.getPreferences().iterator(); i.hasNext(); ) {
		                        		Preference p = i.next();
		                                if (p instanceof TimePref) continue;
		                                if (p instanceof RoomPref) {
		                                    RoomPref rp = (RoomPref)p;
		                                    if (!availableRooms.contains(rp.getRoom())) i.remove();
	                                    } else if (p instanceof BuildingPref) {
	                                        BuildingPref bp = (BuildingPref)p;
	                                        if (!availableBuildings.contains(bp.getBuilding())) i.remove();
		                                } else if (p instanceof RoomFeaturePref) {
		                                    RoomFeaturePref rfp = (RoomFeaturePref)p;
		                                    if (rfp.getRoomFeature() instanceof DepartmentRoomFeature) i.remove();
		                                } else if (p instanceof RoomGroupPref) {
		                                    RoomGroupPref rgp = (RoomGroupPref)p;
		                                    if (!rgp.getRoomGroup().isGlobal()) i.remove();
		                                }
		                            }
	                            	c.setManagingDept(dept, context.getUser(), hibSession);
	                            }
	                            for (Iterator<ClassInstructor> i = c.getClassInstructors().iterator(); i.hasNext(); ) {
	                            	ClassInstructor ci = i.next();
	                                DepartmentalInstructor newInstructor = null;
	                                if (ci.getInstructor().getExternalUniqueId() != null) {
	                                	newInstructor = DepartmentalInstructor.findByPuidDepartmentId(ci.getInstructor().getExternalUniqueId(), dept.getUniqueId());
	                                }
	                                ci.getInstructor().getClasses().remove(ci);
	                                hibSession.merge(ci.getInstructor());
                                    if (newInstructor != null) {
                                        ci.setInstructor(newInstructor);
                                        newInstructor.getClasses().add(ci);
                                        hibSession.merge(newInstructor);
                                    } else {
                                        i.remove();
                                        hibSession.remove(ci);
                                    }
	                            }
	                            hibSession.merge(c);
                                updatedClasses.add(c);
	                        }
	                    }
	            	}

	            }
	            for (Preference p: area.getDepartment().getPreferences()) {
	                if (p instanceof DistributionPref) {
	                    DistributionPref dp = (DistributionPref)p;
	                    boolean change = true;
	                    for (DistributionObject dobj: dp.getOrderedSetOfDistributionObjects()) {
	                        if (dobj.getPrefGroup() instanceof SchedulingSubpart) {
	                            SchedulingSubpart ss = (SchedulingSubpart)dobj.getPrefGroup();
	                            if (!ss.getControllingCourseOffering().getSubjectArea().equals(area)) change=false;
	                             break;
	                        } else if (dobj.getPrefGroup() instanceof Class_) {
	                            Class_ c = (Class_)dobj.getPrefGroup();
	                            if (!c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().equals(area)) change=false;
                                break;
	                        }
	                    }
	                    if (change) {
                            dp.setOwner(dept);
                            hibSession.merge(dp);
	                    }
	                }
	            }
				area.getDepartment().getSubjectAreas().remove(area);
				area.setDepartment(dept);
				dept.getSubjectAreas().add(area);
				
				String instrNameFormat = UserProperty.NameFormat.get(context.getUser());
				String managers = "";
				for (TimetableManager mgr: area.getManagers())
					managers += (managers.isEmpty() ? "" : "\n") + mgr.getName(instrNameFormat);
	            record.setField(5, managers);
			}
			if (fundingEnabled) {
				area.setFundingDept(fundDept);
			}
			hibSession.merge(area);
			ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.UPDATE,
				area,
				area.getDepartment());
			if (!updatedClasses.isEmpty()) {
				String className = ApplicationProperty.ExternalActionClassEdit.value();
				if (className != null && !className.isEmpty()) {
					try {
						ExternalClassEditAction editAction = (ExternalClassEditAction)Class.forName(className).getDeclaredConstructor().newInstance();
		            	for(Class_ c : updatedClasses) {
		            		editAction.performExternalClassEditAction(c, hibSession);
		            	}
					} catch (Exception e) {
						sLog.error(e.getMessage(), e);
					}
	        	}
			}
			record.setField(6, MESSAGES.lastChange(ChangeLog.sDFdate.format(new Date()), context.getUser().getName()));
		}
	}
	
	@Override
	@PreAuthorize("checkPermission(#record.uniqueId, 'SubjectAreaEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(SubjectArea area, SessionContext context, Session hibSession) {
		if (area==null) return;
		
		String className = ApplicationProperty.ExternalActionCourseOfferingRemove.value();
		if (className != null && !className.isEmpty()){
			try {
				ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction)Class.forName(className).getDeclaredConstructor().newInstance();
				for (CourseOffering co: area.getCourseOfferings()) {
					removeAction.performExternalCourseOfferingRemoveAction(co, hibSession);
				}
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
			}
    	}

		for (CourseOffering co: area.getCourseOfferings()) {
			if (!co.isIsControl()) continue; 
			InstructionalOffering io = co.getInstructionalOffering();
			io.deleteAllDistributionPreferences(hibSession);
			io.deleteAllClasses(hibSession);
			io.deleteAllCourses(hibSession);
			hibSession.remove(io);
		}
		
		for (CourseOffering co: area.getCourseOfferings()) {
        	hibSession.remove(co);
        }
		
		area.getDepartment().getSubjectAreas().remove(area);
		
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.DELETE,
				null,
			    area.getDepartment());
		hibSession.remove(area);
	}
	
	@Override
	@PreAuthorize("checkPermission(#record.uniqueId, 'SubjectAreaDelete')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
