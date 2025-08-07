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
package org.unitime.timetable.server.courses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ClassSetupInterface;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.ClassLine;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
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
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.model.dao.LearningManagementSystemInfoDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ClassSetupInterface.class)
public class ClassSetupBackend implements GwtRpcImplementation<ClassSetupInterface, ClassSetupInterface>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired PermissionDepartment permissionDepartment;

	@Override
	public ClassSetupInterface execute(ClassSetupInterface request, SessionContext context) {
		context.checkPermission(request.getConfigId(), "InstrOfferingConfig", Right.MultipleClassSetup);
		switch (request.getOperation()) {
		case LOAD:
			return load(request, context);
		case SAVE:
			return save(request, context);
		default:
			throw new GwtRpcException("Operation " + request.getOperation() + " not supported.");
		}
	}
	
	protected ClassSetupInterface load(ClassSetupInterface request, SessionContext context) {
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(request.getConfigId());
		InstructionalOffering io = ioc.getInstructionalOffering();
		ClassSetupInterface form = new ClassSetupInterface();
		form.setDisplayOptionForMaxLimit(CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.VariableClassLimits)));
		form.setDisplayMaxLimit(false);
		
		form.setConfigId(ioc.getUniqueId());
        form.setLimit(ioc.getLimit());
        form.setUnlimited(ioc.isUnlimitedEnrollment());
        form.setOfferingId(io.getUniqueId());
        form.setDisplayInstructors(ApplicationProperty.ClassSetupDisplayInstructorFlags.isTrue());
        form.setDisplayEnabledForStudentScheduling(ApplicationProperty.ClassSetupEnabledForStudentScheduling.isTrue());
        form.setDisplayExternalId(ApplicationProperty.ClassSetupShowExternalIds.isTrue() && !ApplicationProperty.ClassSetupEditExternalIds.isTrue());
        form.setEditExternalId(ApplicationProperty.ClassSetupEditExternalIds.isTrue());
        form.setEditSnapshotLimits(ApplicationProperty.ClassSetupEditSnapshotLimits.isTrue() && io.getSnapshotLimitDate() != null && context.hasPermission(Right.MultipleClassSetupSnapshotLimits));
        form.setInstructionalMethodId(ioc.getInstructionalMethod() == null ? -1l : ioc.getInstructionalMethod().getUniqueId());
        form.setInstructionalMethodEditable(ApplicationProperty.WaitListCanChangeInstructionalMethod.isTrue() || !ioc.getInstructionalOffering().effectiveReScheduleNow() || ioc.getEnrollment() == 0);
		form.setDisplayLms(Boolean.valueOf(LearningManagementSystemInfo.isLmsInfoDefinedForSession(context.getUser().getCurrentAcademicSessionId())));
		form.setDisplaySnapshotLimit(io.getSnapshotLimitDate() != null);
		form.setValidateLimits(ApplicationProperty.ConfigEditCheckLimits.isTrue());
		
		String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        form.setName(name);
        
        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().isEmpty())
        	throw new GwtRpcException(MSG.errorIOConfigNotDefined());
        
        ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
        
        List<SchedulingSubpart> subparts = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
        Collections.sort(subparts, new SchedulingSubpartComparator());
        form.setEditUnlimited(true);
        form.setDisplayEnrollments(StudentClassEnrollment.sessionHasEnrollments(io.getSessionId()));
        
        for (SchedulingSubpart ss: subparts) {
        	if (ss.getClasses() == null || ss.getClasses().isEmpty())
    			throw new GwtRpcException(MSG.errorInitialIOSetupIncomplete());
        	DatePattern dp = ss.effectiveDatePattern();
    		form.addSubpart(ss.getUniqueId(), ss.getItype().getAbbv(), ss.getItype().getDesc(), dp == null ? null : dp.getName());
    		if (ss.getParentSubpart() == null)
    			loadClasses(form, ss.getClasses(), true, 0, proxy, context);
        }
        
        Department contrDept = io.getControllingCourseOffering().getSubjectArea().getDepartment();
        form.addDepartment(contrDept.getUniqueId(), contrDept.getDeptCode(), MSG.dropDeptDepartment());
		for (Department d: Department.findAllExternal(io.getSessionId())) {
			form.addDepartment(d.getUniqueId(), d.getDeptCode(), d.getExternalMgrLabel(),
					context.hasPermission(d, Right.MultipleClassSetupDepartment) &&
					permissionDepartment.check(context.getUser(), contrDept, DepartmentStatusType.Status.OwnerEdit, d, DepartmentStatusType.Status.ManagerEdit)
					);
		}
		
		LearningManagementSystemInfo lmsDefault = LearningManagementSystemInfo.getDefaultIfExists(io.getSessionId());
		form.addLMS(-1l, MSG.dropDefaultLearningManagementSystem(), MSG.dropDefaultLearningManagementSystem() + (lmsDefault == null ? "" : " (" + lmsDefault.getLabel() + ")"));
		for (LearningManagementSystemInfo lms: LearningManagementSystemInfo.findAll(io.getSessionId()))
			form.addLMS(lms.getUniqueId(), lms.getReference(), lms.getLabel());
		
		List<InstructionalMethod> ims = InstructionalMethod.findAll();
		if (!ims.isEmpty()) {
			InstructionalMethod defaultType = io.getSession().getDefaultInstructionalMethod();
			if (defaultType == null)
				form.addInstructionalMethod(-1l, "", MSG.selectNoInstructionalMethod());
			else
				form.addInstructionalMethod(-1l, defaultType.getReference(), MSG.defaultInstructionalMethod(defaultType.getLabel()));
	    	for (InstructionalMethod type: InstructionalMethod.findAll())
	    		if (type.isVisible() || type.equals(ioc.getInstructionalMethod()))
	    			form.addInstructionalMethod(type.getUniqueId(), type.getReference(), type.getLabel());
		}
    	
    	try {
    		for (DatePattern dp: DatePattern.findAll(context.getUser(), io.getDepartment(), io.getSession().getDefaultDatePatternNotNull()))
    			form.addDatePattern(dp.getUniqueId(), dp.getName(), dp.getName(), !dp.isExtended());
    	} catch (Exception e) {}
		
		return form;
	}
	
	private void loadClasses(ClassSetupInterface form, Set<Class_> classes, boolean editable, int indent, ClassAssignmentProxy proxy, SessionContext context) {
    	if (classes != null && classes.size() > 0){
    		List<Class_> classList = new ArrayList<Class_>(classes);
            Collections.sort(classList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE));
            
	    	for (Class_ cls: classList) {
	    		boolean editableClass = editable && context.hasPermission(cls, Right.MultipleClassSetupClass);
	    		if (!editableClass) form.setEditUnlimited(false);
	    		if (cls.getExpectedCapacity() != null && cls.getMaxExpectedCapacity() != null && !cls.getExpectedCapacity().equals(cls.getMaxExpectedCapacity())) {
	    			form.setDisplayOptionForMaxLimit(true);
	    			form.setDisplayMaxLimit(true);
	    		}
	    		
	    		ClassLine line = toLine(cls, proxy, UserProperty.NameFormat.get(context.getUser()));
	    		line.setEditable(editableClass);
	    		line.setEditableDatePattern(line.isEditable() && !(
						ApplicationProperty.WaitListCanChangeDatePattern.isFalse() && cls.getEnrollment() != null && cls.getEnrollment() > 0 && cls.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().effectiveReScheduleNow())
	    		);
	    		line.setCanDelete(context.hasPermission(cls, Right.ClassDelete));
	    		line.setCanCancel(context.hasPermission(cls, Right.ClassCancel));
	    		line.setIndent(indent);
	    		if (!form.isHasTimeRooms()) {
	    			try {
	    				if (proxy.getAssignment(cls) != null) form.setHasTimeRooms(true);
	    			} catch (Exception e) {}
	    		}
	    		if (!" ".equals(line.getInstructor()))
	    			form.setHasInstructors(true);
	    		form.addClassLine(line);
	    		
	    		loadClasses(form, cls.getChildClasses(), true, indent + 1, proxy, context);
	    	}
    	}
    }
	
	private ClassLine toLine(Class_ clazz, ClassAssignmentProxy proxy, String nameFormat) {
		ClassLine line = new ClassLine();
		line.setClassId(clazz.getUniqueId());
		line.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
		line.setIType(clazz.getSchedulingSubpart().getItype().getItype());
		line.setEnrollment(clazz.getEnrollment());
		line.setSnapshotLimit(clazz.getSnapshotLimit());
		line.setMinClassLimit(clazz.getExpectedCapacity());
		line.setMaxClassLimit(clazz.getMaxExpectedCapacity());
		line.setNumberOfRooms(clazz.getNbrRooms());
		line.setDisplayInstructors(clazz.isDisplayInstructor());
		line.setEnabledForStudentScheduling(clazz.isEnabledForStudentScheduling());
		line.setRoomRatio(clazz.getRoomRatio());
		line.setParentId(clazz.getParentClass() == null ? null : clazz.getParentClass().getUniqueId());
		line.setDepartmentId(clazz.getManagingDept() == null ? null : clazz.getManagingDept().getUniqueId());
		line.setDatePatternId(clazz.getDatePattern() == null ? null : clazz.getDatePattern().getUniqueId());
		line.setCancelled(clazz.isCancelled());
		line.setLMS(clazz.getLmsInfo() == null ? -1l : clazz.getLmsInfo().getUniqueId());
		line.setSplitAttendance(clazz.getNbrRooms() > 1 && Boolean.TRUE.equals(clazz.isRoomsSplitAttendance()));
		line.setExternalId(clazz.getClassSuffix() == null ? "" : clazz.getClassSuffix());
		line.setLabel(clazz.htmlLabel());
		String suffix = clazz.getSchedulingSubpart().getSchedulingSubpartSuffix();
		line.setSubpartLabel(clazz.getSchedulingSubpart().getItypeDesc() + (suffix.isEmpty() ? "" : " (" + suffix + ")"));
		line.setTime(clazz.buildAssignedTimeHtml(proxy));
		line.setDate(clazz.buildAssignedDateHtml(proxy));
		ClassAssignmentProxy.AssignmentInfo a = null;
		try {
			a = proxy.getAssignment(clazz);
		} catch (Exception e) {
			Debug.error(e);
		}
		if (a != null) {
			for (Location room : a.getRooms())
				line.addRoom(room.getUniqueId(), room.getLabel(), room.getCapacity());
		} else {
			if (clazz.getEffectiveTimePreferences().isEmpty()) {
	            for (RoomPref rp : (Set<RoomPref>)clazz.getEffectiveRoomPreferences()) {
	            	if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED))
	            		line.addRoom(rp.getRoom().getUniqueId(), rp.getRoom().getLabel(), rp.getRoom().getCapacity());
	            }
			}
		}
		line.setInstructor(clazz.buildInstructorHtml(nameFormat));
		return line;
	}
	
	protected ClassSetupInterface save(ClassSetupInterface form, SessionContext context) {
		org.hibernate.Session hibSession = InstrOfferingConfigDAO.getInstance().getSession();
		Transaction tx = null;
		try {
	        tx = hibSession.beginTransaction();
	        InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(form.getConfigId(), hibSession);
	        
	        // If the instructional offering config limit or unlimited flag has changed update it.
	        if (form.isUnlimited() != ioc.isUnlimitedEnrollment()) {
	        	ioc.setUnlimitedEnrollment(form.isUnlimited());
	        	ioc.setLimit(form.isUnlimited() ? 0 : form.getLimit());
	        	hibSession.merge(ioc);
	        } else if (!form.getLimit().equals(ioc.getLimit())) {
	        	ioc.setLimit(form.getLimit());
	        	hibSession.merge(ioc);
	        }

	        InstructionalMethod imeth = (form.getInstructionalMethodId() == null || form.getInstructionalMethodId() < 0 ? null : InstructionalMethodDAO.getInstance().get(form.getInstructionalMethodId(), hibSession));
	        if (!ToolBox.equals(ioc.getInstructionalMethod(), imeth)) {
	        	ioc.setInstructionalMethod(imeth);
	        	hibSession.merge(ioc);
	        }
	        
	        // Get map of subpart ownership so that after the classes have changed it is possible to see if the ownership for a subparts has changed
	        Map<Long, Department> origSubpartManagingDept = new HashMap<>();
        	for (SchedulingSubpart ss: ioc.getSchedulingSubparts())
        		origSubpartManagingDept.put(ss.getUniqueId(), ss.getManagingDept());

	        // For all added classes, create the classes and save them, get back a map of the temp ids to the new classes
	        // For all changed classes, update them
	        // Delete all classes in the original classes that are no longer in the modified classes
        	addOrUpdateClasses(form, ioc, hibSession, context);

        	// Update subpart ownership
	        modifySubparts(form, ioc, origSubpartManagingDept, hibSession, context);
	        
	        String className = ApplicationProperty.ExternalActionInstrOffrConfigChange.value();
	        ExternalInstrOffrConfigChangeAction configChangeAction = null;
        	if (className != null && className.trim().length() > 0){
	        	configChangeAction = (ExternalInstrOffrConfigChangeAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	        	if (!configChangeAction.validateConfigChangeCanOccur(ioc.getInstructionalOffering(), hibSession)){
	        		throw new Exception("Configuration change violates rules for Add On, rolling back the change.");
	        	}
        	}
        	
        	ioc.getInstructionalOffering().computeLabels(hibSession);

            ChangeLog.addChange(
                    hibSession,
                    context,
                    ioc,
                    ChangeLog.Source.CLASS_SETUP,
                    ChangeLog.Operation.UPDATE,
                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);
            
        	tx.commit();

	        if (configChangeAction != null){
	        	configChangeAction.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), hibSession);
        	}
	        return null;
        } catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw new GwtRpcException(e.getMessage(), e);
        }
	}
	
	protected static boolean hasPreference(Preference p, Department currentManagingDept) {
		if (p instanceof RoomPref) {
			Location loc = ((RoomPref)p).getRoom();
			for (RoomDept rd: loc.getRoomDepts())
				if (rd.getDepartment().equals(currentManagingDept))
					return true;
		} else if (p instanceof BuildingPref) {
			Building b = ((BuildingPref)p).getBuilding();
			for (RoomDept rd: currentManagingDept.getRoomDepts())
				if (rd.getRoom() instanceof Room && ((Room)rd.getRoom()).getBuilding().equals(b)) return true;
		} else if (p instanceof RoomFeaturePref) {
			RoomFeature rf = ((RoomFeaturePref)p).getRoomFeature();
			return !(rf instanceof DepartmentRoomFeature) || ((DepartmentRoomFeature)rf).getDepartment().equals(currentManagingDept);
		} else if (p instanceof RoomGroupPref) {
			RoomGroup rg = ((RoomGroupPref)p).getRoomGroup();
			return rg.isGlobal() || currentManagingDept.equals(rg.getDepartment());
		}
		return false;
	}
	
	protected static boolean wouldGetWeakened(Preference p, boolean weaken) {
		if (!weaken) return false;
		return PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(p.getPrefLevel().getPrefProlog());
	}
	
	protected void updatePreferences(SchedulingSubpart ss, Department controllingDept, Department origManagingDept, Department currentManagingDept, Session hibSession, SessionContext context) {
		if (ApplicationProperty.ClearPreferencesWhenManagingDepartmentIsChanged.isTrue()) {
			// Push preferences up
			if (!origManagingDept.equals(controllingDept)) {
				for (Class_ c: ss.getClasses()) {
					boolean classChanged = false;
					if (c.getManagingDept().equals(origManagingDept)) {
						Set<TimePattern> timePatterns = c.getTimePatterns();
						for (TimePref tp: ss.getTimePreferences()) {
							if (timePatterns != null && !timePatterns.contains(tp.getTimePattern())) {
								TimePref ntp = new TimePref();
								ntp.setOwner(c);
								ntp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
								ntp.setTimePattern(tp.getTimePattern());
								ntp.setPreference(tp.getPreference());
								c.addToPreferences(ntp);
								classChanged = true;
							}
						}
						Set<Building> buildings = new HashSet<>();
						for (BuildingPref bp: c.getBuildingPreferences())
							buildings.add(bp.getBuilding());
						for (BuildingPref bp: ss.getBuildingPreferences()) {
							if (!buildings.contains(bp.getBuilding())){
								BuildingPref nbp = new BuildingPref();
								nbp.setOwner(c);
								nbp.setPrefLevel(bp.getPrefLevel());
								nbp.setBuilding(bp.getBuilding());
								nbp.setDistanceFrom(bp.getDistanceFrom());
								c.addToPreferences(nbp);
								classChanged = true;
							}
						}
						Set<Location> rooms = new HashSet<>();
						for (RoomPref rp: c.getRoomPreferences())
							rooms.add(rp.getRoom());
						for (RoomPref rp: ss.getRoomPreferences()) {
							if (!rooms.contains(rp.getRoom())){
								RoomPref nrp = new RoomPref();
								nrp.setOwner(c);
								nrp.setPrefLevel(rp.getPrefLevel());
								nrp.setRoom(rp.getRoom());
								c.addToPreferences(nrp);
								classChanged = true;
							}
						}
						Set<RoomFeature> roomFeatures = new HashSet<>();
						for (RoomFeaturePref rfp: c.getRoomFeaturePreferences())
							roomFeatures.add(rfp.getRoomFeature());
						for (RoomFeaturePref rfp: ss.getRoomFeaturePreferences()) {
							if (!roomFeatures.contains(rfp.getRoomFeature())){
								RoomFeaturePref nrfp = new RoomFeaturePref();
								nrfp.setOwner(c);
								nrfp.setPrefLevel(rfp.getPrefLevel());
								nrfp.setRoomFeature(rfp.getRoomFeature());
								c.addToPreferences(nrfp);
								classChanged = true;
							}
						}
						Set<RoomGroup> roomGroups = new HashSet<>();
						for (RoomGroupPref rgp: c.getRoomGroupPreferences())
							roomGroups.add(rgp.getRoomGroup());
						for (RoomGroupPref rgp: ss.getRoomGroupPreferences()) {
							if (!roomGroups.contains(rgp.getRoomGroup())){
								RoomGroupPref nrgp = new RoomGroupPref();
								nrgp.setOwner(c);
								nrgp.setPrefLevel(rgp.getPrefLevel());
								nrgp.setRoomGroup(rgp.getRoomGroup());
								c.addToPreferences(nrgp);
								classChanged = true;
							}
						}
						if (classChanged)
							hibSession.merge(c);
					}
				}
			}
		} else {
			if (!origManagingDept.equals(controllingDept)) {
		        boolean weakenTime = true;
		        boolean weakenRoom = true;
		        if (!currentManagingDept.isExternalManager()) {
		        	weakenTime = false; 
		        	weakenRoom = false;
		        }
		        if (weakenTime && (Boolean.TRUE.equals(currentManagingDept.isAllowReqTime()) || Boolean.TRUE.equals(controllingDept.isAllowReqTime())))
		            weakenTime = false;
		        if (weakenRoom && (Boolean.TRUE.equals(currentManagingDept.isAllowReqRoom()) || Boolean.TRUE.equals(controllingDept.isAllowReqRoom())))
		            weakenRoom = false;
		        for (Class_ c: ss.getClasses()) {
					boolean classChanged = false;
					if (c.getManagingDept().equals(origManagingDept)) {
						Set<TimePattern> timePatterns = c.getTimePatterns();
						for (TimePref tp: ss.getTimePreferences()) {
							if (InstrOfferingConfigBackend.hasPreference(tp, currentManagingDept) && !InstrOfferingConfigBackend.toBeWeakened(tp, weakenRoom))
								continue;
							if (timePatterns != null && !timePatterns.contains(tp.getTimePattern())) {
								TimePref ntp = new TimePref();
								ntp.setOwner(c);
								ntp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
								ntp.setTimePattern(tp.getTimePattern());
								ntp.setPreference(tp.getPreference());
								c.addToPreferences(ntp);
								classChanged = true;
							}
						}
						Set<Building> buildings = new HashSet<>();
						for (BuildingPref bp: c.getBuildingPreferences())
							buildings.add(bp.getBuilding());
						for (BuildingPref bp: ss.getBuildingPreferences()) {
							if (InstrOfferingConfigBackend.hasPreference(bp, currentManagingDept) && !InstrOfferingConfigBackend.toBeWeakened(bp, weakenRoom))
								continue;
							if (!buildings.contains(bp.getBuilding())){
								BuildingPref nbp = new BuildingPref();
								nbp.setOwner(c);
								nbp.setPrefLevel(bp.getPrefLevel());
								nbp.setBuilding(bp.getBuilding());
								nbp.setDistanceFrom(bp.getDistanceFrom());
								c.addToPreferences(nbp);
								classChanged = true;
							}
						}
						Set<Location> rooms = new HashSet<>();
						for (RoomPref rp: c.getRoomPreferences())
							rooms.add(rp.getRoom());
						for (RoomPref rp: ss.getRoomPreferences()) {
							if (InstrOfferingConfigBackend.hasPreference(rp, currentManagingDept) && !InstrOfferingConfigBackend.toBeWeakened(rp, weakenRoom))
								continue;
							if (!rooms.contains(rp.getRoom()) && !ss.getAvailableRooms().contains(rp.getRoom())){
								RoomPref nrp = new RoomPref();
								nrp.setOwner(c);
								nrp.setPrefLevel(rp.getPrefLevel());
								nrp.setRoom(rp.getRoom());
								c.addToPreferences(nrp);
								classChanged = true;
							}
						}
						Set<RoomFeature> roomFeatures = new HashSet<>();
						for (RoomFeaturePref rfp: c.getRoomFeaturePreferences())
							roomFeatures.add(rfp.getRoomFeature());
						for (RoomFeaturePref rfp: ss.getRoomFeaturePreferences()) {
							if (InstrOfferingConfigBackend.hasPreference(rfp, currentManagingDept) && !InstrOfferingConfigBackend.toBeWeakened(rfp, weakenRoom))
								continue;
							if (!roomFeatures.contains(rfp.getRoomFeature()) && !ss.getAvailableRoomFeatures().contains(rfp.getRoomFeature())) {
								RoomFeaturePref nrfp = new RoomFeaturePref();
								nrfp.setOwner(c);
								nrfp.setPrefLevel(rfp.getPrefLevel());
								nrfp.setRoomFeature(rfp.getRoomFeature());
								c.addToPreferences(nrfp);
								classChanged = true;
							}
						}
						Set<RoomGroup> roomGroups = new HashSet<>();
						for (RoomGroupPref rgp: c.getRoomGroupPreferences())
							roomGroups.add(rgp.getRoomGroup());
						for (RoomGroupPref rgp: ss.getRoomGroupPreferences()) {
							if (InstrOfferingConfigBackend.hasPreference(rgp, currentManagingDept) && !InstrOfferingConfigBackend.toBeWeakened(rgp, weakenRoom))
								continue;
							if (!roomGroups.contains(rgp.getRoomGroup()) && !ss.getAvailableRoomGroups().contains(rgp.getRoomGroup())) {
								RoomGroupPref nrgp = new RoomGroupPref();
								nrgp.setOwner(c);
								nrgp.setPrefLevel(rgp.getPrefLevel());
								nrgp.setRoomGroup(rgp.getRoomGroup());
								c.addToPreferences(nrgp);
								classChanged = true;
							}
						}
						if (classChanged)
							hibSession.merge(c);
					}
				}
			}
		}
		InstrOfferingConfigBackend.updatePreferences(ss, controllingDept, currentManagingDept, hibSession, context);
	}	
	
	private void addOrUpdateClasses(ClassSetupInterface form, InstrOfferingConfig ioc, Session hibSession, SessionContext context) {
		Map<Long, Class_> tmpClsToRealClass = new HashMap<>();
		Map<Long, Class_> deleteClasses = new HashMap<>();
		for (SchedulingSubpart ss: ioc.getSchedulingSubparts())
			for (Class_ c: ss.getClasses())
				deleteClasses.put(c.getUniqueId(), c);
		Date timeStamp = new Date();
		for (ClassLine cl: form.getClassLines()) {
			Long classId = cl.getClassId();
			Long subpartId = cl.getSubpartId();
			Long parentClassId = cl.getParentId();
			Integer minClassLimit = cl.getMinClassLimit();
			Long managingDeptId = cl.getDepartmentId();
			if (managingDeptId != null && managingDeptId < 0) managingDeptId = null;
			Long datePatternId = cl.getDatePatternId();
			if (datePatternId != null && datePatternId < 0) datePatternId = null;
			Integer numberOfRooms = cl.getNumberOfRooms();
			Integer maxClassLimit = cl.getMaxClassLimit();
			Float roomRatio = cl.getRoomRatio();
			if (form.isUnlimited()) {
				roomRatio = 1.0f;
				minClassLimit = 0;
				maxClassLimit = 0;
				numberOfRooms = 0;
			}
			Boolean displayInstructor = cl.getDisplayInstructors();
			Boolean enabledForStudentScheduling = cl.getEnabledForStudentScheduling();
			String suffix = cl.getExternalId();
			if (suffix != null && suffix.isEmpty()) suffix = null;
			Integer snapshotLimit = cl.getSnapshotLimit();
			Long lmsId = cl.getLMS();
			if (lmsId != null && lmsId < 0) lmsId = null;
			Boolean splitAttendance = cl.getSplitAttendance();
			Boolean cancelled = cl.getCancelled();

			if (classId <= 0) {
				Class_ newClass = new Class_();
				SchedulingSubpart ss = SchedulingSubpartDAO.getInstance().get(subpartId, hibSession);
				newClass.setSchedulingSubpart(ss);
				ss.addToClasses(newClass);
				if (parentClassId != null) {
					Class_ parentClass;
					if (parentClassId > 0)
						parentClass = Class_DAO.getInstance().get(parentClassId, hibSession);
					else
						parentClass = tmpClsToRealClass.get(parentClassId);
					newClass.setParentClass(parentClass);
					parentClass.addToChildClasses(newClass);
				}
				newClass.setControllingDept(ss.getControllingDept());
				if (managingDeptId != null)
					newClass.setManagingDept(
							DepartmentDAO.getInstance().get(managingDeptId, hibSession),
							context.getUser(), hibSession);
				if (datePatternId != null)
					newClass.setDatePattern(DatePatternDAO.getInstance().get(datePatternId, hibSession));
				newClass.setExpectedCapacity(minClassLimit);
				newClass.setNbrRooms(numberOfRooms);
				newClass.setMaxExpectedCapacity(maxClassLimit);
				newClass.setRoomRatio(roomRatio);
				newClass.setDisplayInstructor(displayInstructor);
				newClass.setEnabledForStudentScheduling(enabledForStudentScheduling);
				newClass.setClassSuffix(suffix);
				newClass.setSnapshotLimit(snapshotLimit);
				newClass.setSnapshotLimitDate(timeStamp);
				newClass.setCancelled(false);
				if (lmsId != null)
					newClass.setLmsInfo(LearningManagementSystemInfoDAO.getInstance().get(lmsId, hibSession));
				newClass.setRoomsSplitAttendance(splitAttendance);
				hibSession.persist(newClass);
				hibSession.merge(ss);
				tmpClsToRealClass.put(classId, newClass);
			} else {
				boolean changed = false;
				Class_ modifiedClass = deleteClasses.remove(classId); 
				if (modifiedClass.getParentClass() != null && parentClassId != null) {
					if (!modifiedClass.getParentClass().getUniqueId().equals(parentClassId)) {
						Class_ origParent = modifiedClass.getParentClass();
						if (parentClassId < 0){
							modifiedClass.setParentClass(tmpClsToRealClass.get(parentClassId));
						} else {
							modifiedClass.setParentClass(Class_DAO.getInstance().get(parentClassId, hibSession));
						}
						origParent.getChildClasses().remove(modifiedClass);
						modifiedClass.getParentClass().addToChildClasses(modifiedClass);
						hibSession.merge(modifiedClass.getParentClass());
						hibSession.merge(origParent);
					}
				}
				if (managingDeptId == null)
					managingDeptId = modifiedClass.getControllingDept().getUniqueId();
				if (!modifiedClass.getManagingDept().getUniqueId().equals(managingDeptId)){
					changed = true;
					Department managingDept = DepartmentDAO.getInstance().get(managingDeptId, hibSession);
					modifiedClass.setManagingDept(managingDept, context.getUser(), hibSession);
					InstrOfferingConfigBackend.updatePreferences(modifiedClass, managingDept, modifiedClass.getControllingDept(), hibSession, context);
				}
				if (!ToolBox.equals(modifiedClass.getDatePattern() == null ? null : modifiedClass.getDatePattern().getUniqueId(), datePatternId)) {
					changed = true;
					modifiedClass.setDatePattern(datePatternId == null ? null : DatePatternDAO.getInstance().get(datePatternId, hibSession));
				}
				if (!ToolBox.equals(modifiedClass.getLmsInfo() == null ? null : modifiedClass.getLmsInfo().getUniqueId(), lmsId)) {
					changed = true;
					modifiedClass.setLms(lmsId == null ? null : LearningManagementSystemInfoDAO.getInstance().get(lmsId, hibSession));
				}
				if (!modifiedClass.getExpectedCapacity().equals(minClassLimit)) {
					changed = true;
					modifiedClass.setExpectedCapacity(minClassLimit);
				}
				if (!modifiedClass.getNbrRooms().equals(numberOfRooms)) {
					changed = true;
					modifiedClass.setNbrRooms(numberOfRooms);
				}
				if (!modifiedClass.getMaxExpectedCapacity().equals(maxClassLimit)) {
					changed = true;
					modifiedClass.setMaxExpectedCapacity(maxClassLimit);
				}
				if (!modifiedClass.getRoomRatio().equals(roomRatio)) {
					changed = true;
					modifiedClass.setRoomRatio(roomRatio);
				}
				if (form.isDisplayInstructors() && !modifiedClass.isDisplayInstructor().equals(displayInstructor)) {
					changed = true;
					modifiedClass.setDisplayInstructor(displayInstructor);
				}
				if (form.isDisplayEnabledForStudentScheduling() && !modifiedClass.isEnabledForStudentScheduling().equals(enabledForStudentScheduling)){
					changed = true;
					modifiedClass.setEnabledForStudentScheduling(enabledForStudentScheduling);
				}
				if (form.isEditExternalId()) {
					if (suffix == null ? modifiedClass.getClassSuffix() != null : !suffix.equals(modifiedClass.getClassSuffix())) {
						modifiedClass.setClassSuffix(suffix);
						changed = true;
					}
				}
				if (form.isEditSnapshotLimits()) {
					if (snapshotLimit == null ? modifiedClass.getSnapshotLimit() != null : !snapshotLimit.equals(modifiedClass.getSnapshotLimit())) {
						modifiedClass.setSnapshotLimit(snapshotLimit);
						modifiedClass.setSnapshotLimitDate(timeStamp);
						changed = true;
					}
				}
				if (!modifiedClass.isCancelled().equals(cancelled)) {
					modifiedClass.setCancelled(cancelled);
					modifiedClass.cancelEvent(context.getUser(), hibSession, cancelled);
					changed = true;
				}
				if (modifiedClass.isRoomsSplitAttendance() == null || !modifiedClass.isRoomsSplitAttendance().equals(splitAttendance)) {
					modifiedClass.setRoomsSplitAttendance(splitAttendance);
					changed = true;
				}
				
				if (changed)
					hibSession.merge(modifiedClass);
			}
		}
		for (Class_ c: deleteClasses.values()) {
			if (c.getParentClass() != null && !deleteClasses.containsKey(c.getParentClass().getUniqueId())) {
				Class_ parent = c.getParentClass();
				parent.getChildClasses().remove(c);
				hibSession.merge(parent);
			}
			if (c.getPreferences() != null)
			    c.getPreferences().removeAll(c.getPreferences());
			
			c.getSchedulingSubpart().getClasses().remove(c);
			hibSession.remove(c);
    	}
		for (Class_ c: deleteClasses.values()) {
			c.deleteAllDependentObjects(hibSession, false);
		}
	}
	
    private void modifySubparts(ClassSetupInterface form, InstrOfferingConfig ioc, Map<Long, Department> origSubpartManagingDept, Session hibSession, SessionContext context) {
    	for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
    		Department controllingDept = ss.getControllingDept();
    		Department currentManagingDept = ss.getManagingDept();
    		Department origManagingDept = origSubpartManagingDept.get(ss.getUniqueId());
    		if (origManagingDept != null && !currentManagingDept.equals(origManagingDept))
    			updatePreferences(ss, controllingDept, origManagingDept, currentManagingDept, hibSession, context);
    	}
	}
}
