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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.Operation;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.SubpartLine;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.ClassDurationTypeDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.duration.DurationModel;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstrOfferingConfigInterface.class)
public class InstrOfferingConfigBackend implements GwtRpcImplementation<InstrOfferingConfigInterface, InstrOfferingConfigInterface>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired PermissionDepartment permissionDepartment;

	@Override
	public InstrOfferingConfigInterface execute(InstrOfferingConfigInterface request, SessionContext context) {
		switch (request.getOperation()) {
		case LOAD:
			if (request.getConfigId() == null)
				context.checkPermission(request.getOfferingId(), "InstructionalOffering", Right.InstrOfferingConfigAdd);
			else
				context.checkPermission(request.getConfigId(), "InstrOfferingConfig", Right.InstrOfferingConfigEdit);
			return load(request, context);
		case SAVE:
			if (request.getConfigId() == null)
				context.checkPermission(request.getOfferingId(), "InstructionalOffering", Right.InstrOfferingConfigAdd);
			else
				context.checkPermission(request.getConfigId(), "InstrOfferingConfig", Right.InstrOfferingConfigEdit);
			return save(request, context);
		case DELETE:
			context.checkPermission(request.getConfigId(), "InstrOfferingConfig", Right.InstrOfferingConfigDelete);
			return delete(request, context);
		default:
			throw new GwtRpcException("Operation " + request.getOperation() + " not supported.");
		}
	}
	
	protected InstrOfferingConfigInterface load(InstrOfferingConfigInterface request, SessionContext context) {
		InstrOfferingConfig ioc = (request.getConfigId() == null ? null : InstrOfferingConfigDAO.getInstance().get(request.getConfigId()));
		InstructionalOffering io = (ioc != null ? ioc.getInstructionalOffering() : InstructionalOfferingDAO.getInstance().get(request.getOfferingId()));
		
		if (MSG.actionMakeOffered().equals(request.getOp())) {
    	    if (!io.getInstrOfferingConfigs().isEmpty())
    	    	ioc = io.getInstrOfferingConfigs().iterator().next();
		}
		
		InstrOfferingConfigInterface form = new InstrOfferingConfigInterface();
		form.setDisplayOptionForMaxLimit(CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.VariableClassLimits)));
		form.setDisplayMaxLimit(false);
		
		form.setConfigId(ioc == null ? null : ioc.getUniqueId());
        form.setLimit(ioc == null ? 0 : ioc.getLimit());
        form.setUnlimited(ioc == null ? false : ioc.isUnlimitedEnrollment());
        form.setOfferingId(io.getUniqueId());
        form.setInstructionalMethodId(ioc == null || ioc.getInstructionalMethod() == null ? -1l : ioc.getInstructionalMethod().getUniqueId());
        form.setInstructionalMethodEditable(ioc == null ? true : ApplicationProperty.WaitListCanChangeInstructionalMethod.isTrue() || !ioc.getInstructionalOffering().effectiveReScheduleNow() || ioc.getEnrollment() == 0);
        form.setDurationTypeId(ioc == null || ioc.getClassDurationType() == null ? -1l: ioc.getClassDurationType().getUniqueId());
        form.setCanDelete(ioc != null && context.hasPermission(ioc, Right.InstrOfferingConfigDelete));
        form.setCourseId(io.getControllingCourseOffering().getUniqueId());
        form.setCourseName(io.getCourseNameWithTitle());
        form.setConfigName(ioc == null ? InstrOfferingConfig.getGeneratedName(io) : ioc.getName());
        form.setDisplayCourseLink(ApplicationProperty.CustomizationCourseLink.value() != null && !ApplicationProperty.CustomizationCourseLink.value().isEmpty());
        form.setCheckLimits(ApplicationProperty.ConfigEditCheckLimits.isTrue());
        form.setMaxNumberOfClasses(ApplicationProperty.SubpartMaxNumClasses.intValue());
        
        Department contrDept = io.getControllingCourseOffering().getSubjectArea().getDepartment();
        form.addDepartment(-1l, "-", MSG.subpartMultipleManagers(), false);
        form.addDepartment(contrDept.getUniqueId(), contrDept.getDeptCode(), MSG.dropDeptDepartment());
		for (Department d: Department.findAllExternal(io.getSessionId())) {
			form.addDepartment(d.getUniqueId(), d.getDeptCode(), d.getExternalMgrLabel(),
					context.hasPermission(d, Right.InstrOfferingConfigEditDepartment) &&
					permissionDepartment.check(context.getUser(), contrDept, DepartmentStatusType.Status.OwnerEdit, d, DepartmentStatusType.Status.ManagerEdit)
					);
		}
		
		List<InstructionalMethod> ims = InstructionalMethod.findAll();
		if (!ims.isEmpty()) {
			InstructionalMethod defaultType = io.getSession().getDefaultInstructionalMethod();
			if (defaultType == null)
				form.addInstructionalMethod(-1l, "", MSG.selectNoInstructionalMethod());
			else
				form.addInstructionalMethod(-1l, defaultType.getReference(), MSG.defaultInstructionalMethod(defaultType.getLabel()));
	    	for (InstructionalMethod type: ims)
	    		if (type.isVisible() || (ioc != null && type.equals(ioc.getInstructionalMethod())))
	    			form.addInstructionalMethod(type.getUniqueId(), type.getReference(), type.getLabel());
		}
		
		List<ClassDurationType> cdts = ClassDurationType.findAll();
		if (!cdts.isEmpty()) {
			ClassDurationType defaultType = io.getSession().getDefaultClassDurationType();
			if (defaultType == null)
				form.addDurationType(-1l, "", MSG.systemDefaultDurationType());
			else
				form.addDurationType(-1l, defaultType.getReference(), MSG.sessionDefault(defaultType.getLabel()));
	    	for (ClassDurationType type: cdts)
	    		if (type.isVisible() || (ioc != null && type.equals(ioc.getClassDurationType())))
	    			form.addDurationType(type.getUniqueId(), type.getReference(), type.getLabel());
		}
		
		for (InstrOfferingConfig c: io.getInstrOfferingConfigs())
			form.addConfig(c.getUniqueId(), c.getName());
		
		for (ItypeDesc itype: InstrOfferingConfigDAO.getInstance().getSession().createQuery(
				"from ItypeDesc order by itype", ItypeDesc.class).setCacheable(true).list()) {
			form.addInstructionalType(itype.getItype().longValue(), itype.getAbbv(), itype.getDesc().trim(), itype.getBasic());
		}
		if (form.getDurationTypes().size() <= 1) {
        	ClassDurationType dtype = (ioc == null ? io.getSession().getDefaultClassDurationType() : ioc.getEffectiveDurationType());
        	if (dtype != null && dtype.isVisible())
        		form.setDurationTypeEditable(false);
        	else
        		form.setDurationTypeEditable(true);
        } else {
        	form.setDurationTypeEditable(true);
        }
		
		if (ioc != null) {
	        List<SchedulingSubpart> subparts = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
	        Collections.sort(subparts, new SchedulingSubpartComparator());
			for (SchedulingSubpart subpart: subparts)
				if (subpart.getParentSubpart() == null)
					addSubpartLines(form, subpart, context, 0);
		}

		return form;
	}
	
	protected void addSubpartLines(InstrOfferingConfigInterface form, SchedulingSubpart subpart, SessionContext context, int indent) {
		SubpartLine line = toLine(subpart, context);
		line.setIndent(indent);
		form.addSubpartLine(line);
		if (!ToolBox.equals(line.getMinClassLimit(), line.getMaxClassLimit())) {
			form.setDisplayOptionForMaxLimit(true);
			form.setDisplayMaxLimit(true);
		}
		if (!line.isEditable()) {
			line.setLocked(true);
			SubpartLine parent = form.getSubpartLine(line.getParentId());
			while (parent != null) {
				parent.setEditable(false);
				parent = form.getSubpartLine(parent.getParentId());
			}
		}
		if (!line.isCanDelete()) {
			SubpartLine parent = form.getSubpartLine(line.getParentId());
			while (parent != null) {
				parent.setCanDelete(false);
				parent = form.getSubpartLine(parent.getParentId());
			}
		}
		
        List<SchedulingSubpart> children = new ArrayList<SchedulingSubpart>(subpart.getChildSubparts());
        Collections.sort(children, new SchedulingSubpartComparator());
        for (SchedulingSubpart child: children)
        	addSubpartLines(form, child, context, indent + 1);
	}
	
	protected SubpartLine toLine(SchedulingSubpart subpart, SessionContext context) {
		SubpartLine line = new SubpartLine();
		line.setSubpartId(subpart.getUniqueId());
		line.setIType(subpart.getItype().getItype());
		line.setLabel(subpart.getItype().getDesc().trim());
        if (subpart.getClasses() != null && !subpart.getClasses().isEmpty()) {
            line.setMinClassLimit(subpart.getMinClassLimit());
            line.setMaxClassLimit(subpart.getMaxClassLimit());
            if (line.getMinClassLimit() < 0)
            	line.setMinClassLimit(subpart.getInstrOfferingConfig().getLimit());
            if (line.getMaxClassLimit() < 0)
            	line.setMaxClassLimit(subpart.getInstrOfferingConfig().getLimit());
            line.setNumberOfClasses(subpart.getClasses() == null ? 0 : subpart.getClasses().size());
            line.setNumberOfRooms(subpart.getMaxRooms());
            line.setRoomRatio(subpart.getMaxRoomRatio());
            line.setSplitAttendance(subpart.isRoomSplitAttendance());
        } else {
        	line.setNumberOfRooms(1);
        	line.setRoomRatio(1f);
        	line.setSplitAttendance(false);
        }
        line.setMinutesPerWeek(subpart.getMinutesPerWk());
        boolean mixedManaged = subpart.hasMixedManagedClasses(); 
        line.setDepartmentId(mixedManaged ? null : subpart.getManagingDept().getUniqueId());
        line.setParentId(subpart.getParentSubpart() == null ? null : subpart.getParentSubpart().getUniqueId());
        if (!context.hasPermission(subpart, Right.InstrOfferingConfigEditSubpart) || mixedManaged) {
        	line.setEditable(false);
        	line.setCanDelete(false);
        	if (mixedManaged)
        		line.setDepartmentId(-1l);
        } else {
        	for (Class_ c: subpart.getClasses())
        		if (!context.hasPermission(c, Right.ClassDelete)) {
        			line.setCanDelete(false);
        			break;
        		}
        }
        return line;
	}
	
	protected InstrOfferingConfigInterface save(InstrOfferingConfigInterface form, SessionContext context) {
		org.hibernate.Session hibSession = InstrOfferingConfigDAO.getInstance().getSession();
		Transaction tx = null;
		try {
	        tx = hibSession.beginTransaction();
	        InstructionalOffering io = null;
	        InstrOfferingConfig ioc = null;
	        
	        if (form.getConfigId() != null) {
	        	ioc = InstrOfferingConfigDAO.getInstance().get(form.getConfigId(), hibSession);
	        	io = ioc.getInstructionalOffering();
	        } else {
	        	io = InstructionalOfferingDAO.getInstance().get(form.getOfferingId(), hibSession);
	        	ioc = new InstrOfferingConfig();
	        	ioc.setInstructionalOffering(io); io.addToInstrOfferingConfigs(ioc);
	        	ioc.setSchedulingSubparts(new HashSet<SchedulingSubpart>());
	        }
	        ioc.setName(form.getConfigName());
	        ioc.setLimit(form.isUnlimited() ? 0 : form.getLimit());
	        ioc.setUnlimitedEnrollment(form.isUnlimited());
	        if (form.isDurationTypeEditable())
	        	ioc.setClassDurationType(form.getDurationTypeId() == null || form.getDurationTypeId() < 0l ? null : ClassDurationTypeDAO.getInstance().get(form.getDurationTypeId(), hibSession));
	        if (form.isInstructionalMethodEditable())
	        	ioc.setInstructionalMethod(form.getInstructionalMethodId() == null || form.getInstructionalMethodId() < 0l ? null : InstructionalMethodDAO.getInstance().get(form.getInstructionalMethodId(), hibSession));
	        
	        if (ioc.getUniqueId() == null)
	        	hibSession.persist(ioc);
	        else
	        	hibSession.merge(ioc);
	        
	        addOrUpdateSubparts(form, ioc, hibSession, context);
	        
	        String className = ApplicationProperty.ExternalActionInstrOffrConfigChange.value();
	        ExternalInstrOffrConfigChangeAction configChangeAction = null;
        	if (className != null && className.trim().length() > 0){
	        	configChangeAction = (ExternalInstrOffrConfigChangeAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	        	if (!configChangeAction.validateConfigChangeCanOccur(ioc.getInstructionalOffering(), hibSession)){
	        		throw new Exception("Configuration change violates rules for Add On, rolling back the change.");
	        	}
        	}
        	
        	hibSession.merge(ioc);

        	ioc.getInstructionalOffering().computeLabels(hibSession);

            ChangeLog.addChange(
                    hibSession,
                    context,
                    ioc,
                    ChangeLog.Source.INSTR_CFG_EDIT,
                    (form.getConfigId() == null ? ChangeLog.Operation.CREATE: ChangeLog.Operation.UPDATE),
                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);
            
        	tx.commit();

	        if (configChangeAction != null){
	        	configChangeAction.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), hibSession);
        	}
	        return new InstrOfferingConfigInterface(Operation.SAVE, ioc.getInstructionalOffering().getUniqueId(), ioc.getUniqueId());
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
	
	private static boolean isParent(Class_ clazz, Class_ parent) {
		SchedulingSubpart parentSubpart = parent.getSchedulingSubpart();
		while (parentSubpart != null) {
			if (parentSubpart.isParentOf(clazz.getSchedulingSubpart()))
				return parent.isParentOf(clazz);
			parentSubpart = parentSubpart.getParentSubpart();
			parent = parent.getParentClass();
		}
		return false;
	}
	
	protected static Department getDepartment(DistributionPref dp, SessionContext context) {
		Department owningDept = null;
		List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>(dp.getDistributionObjects());
		Collections.sort(distributionObjects);
    	for (DistributionObject relatedObject: distributionObjects) {
    		if (relatedObject.getPrefGroup() instanceof SchedulingSubpart) {
    			SchedulingSubpart subpart = (SchedulingSubpart)relatedObject.getPrefGroup();
            	if (owningDept==null) owningDept = subpart.getManagingDept();
	        	else if (!owningDept.equals(subpart.getManagingDept())) {
	        		if (owningDept.getDistributionPrefPriority() < subpart.getManagingDept().getDistributionPrefPriority())
	        			owningDept = subpart.getManagingDept();
	        		else if (owningDept.getDistributionPrefPriority() == subpart.getManagingDept().getDistributionPrefPriority()) {
	        			if (!context.getUser().getCurrentAuthority().hasQualifier(owningDept) && context.getUser().getCurrentAuthority().hasQualifier(subpart.getManagingDept()))
	        				owningDept = subpart.getManagingDept();
	        		}
	        	}
    		} else if (relatedObject.getPrefGroup() instanceof Class_) {
    			Class_ clazz = (Class_)relatedObject.getPrefGroup();
            	if (owningDept == null) owningDept = clazz.getManagingDept();
	        	else if (!owningDept.equals(clazz.getManagingDept())) {
	        		if (owningDept.getDistributionPrefPriority() < clazz.getManagingDept().getDistributionPrefPriority())
	        			owningDept = clazz.getManagingDept();
	        		else if (owningDept.getDistributionPrefPriority() == clazz.getManagingDept().getDistributionPrefPriority()) {
	        			if (!context.getUser().getCurrentAuthority().hasQualifier(owningDept) && context.getUser().getCurrentAuthority().hasQualifier(clazz.getManagingDept()))
	        				owningDept = clazz.getManagingDept();
	        		}
	        	}
    		}
    	}
    	return owningDept;
	}
	
	protected static boolean hasPreference(Preference p, Department currentManagingDept) {
		if (p instanceof TimePref) {
			return true;
		} else if (p instanceof RoomPref) {
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
	
	protected static boolean toBeWeakened(Preference p, boolean weaken) {
		if (!weaken) return false;
		if (p instanceof TimePref) {
			TimePref tp = (TimePref)p;
			if (tp.getTimePattern().isExactTime()) return false;
			TimePatternModel m = tp.getTimePatternModel();
			return m.hasProgibitedPreferences() || m.hasRequiredPreferences();
		} else {
			return PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(p.getPrefLevel().getPrefProlog());
		}
	}
	
	protected static void updatePreferences(PreferenceGroup gr, Department controllingDept, Department currentManagingDept, Session hibSession, SessionContext context) {
		if (ApplicationProperty.ClearPreferencesWhenManagingDepartmentIsChanged.isTrue()) {
			Set<TimePref> timePrefs = gr.getTimePreferences();
			gr.getPreferences().clear();

            boolean weaken = true;
            if (!currentManagingDept.isExternalManager()) weaken = false;
            if (weaken && Boolean.TRUE.equals(currentManagingDept.isAllowReqTime()))
                weaken = false;
            if (weaken && Boolean.TRUE.equals(controllingDept.isAllowReqTime()))
                weaken = false;

			for(Iterator it = timePrefs.iterator(); it.hasNext();){
                TimePref timePref = (TimePref)it.next();
				TimePattern timePattern = timePref.getTimePattern();
				if (timePattern.isExactTime()) continue;
				TimePref tp = new TimePref();
				tp.setOwner(gr);
				tp.setPrefLevel(timePref.getPrefLevel());
				tp.setTimePattern(timePattern);
				tp.setPreference(timePref.getPreference());
                if (weaken) tp.weakenHardPreferences();
                gr.addToPreferences(tp);
			}
			if (gr instanceof Class_) {
				((Class_)gr).deleteAllDistributionPreferences(hibSession);
			} else if (gr instanceof SchedulingSubpart) {
				SchedulingSubpart subpart = (SchedulingSubpart)gr;
				subpart.deleteAllDistributionPreferences(hibSession);
				if (currentManagingDept.equals(controllingDept) && !subpart.getInstrOfferingConfig().isUnlimitedEnrollment() && subpart.getMaxRooms() > 0) {
					RoomGroup rg = RoomGroup.getGlobalDefaultRoomGroup(controllingDept.getSession());
					if (rg!=null) {
	    				RoomGroupPref rgp = new RoomGroupPref();
						rgp.setOwner(subpart);
						rgp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
						rgp.setRoomGroup(rg);
						subpart.addToPreferences(rgp);
					}
    			}
			}
			return;
		}
		
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

        for (Iterator<Preference> i = gr.getPreferences().iterator(); i.hasNext(); ) {
        	Preference p = i.next();
        	if (p instanceof TimePref) {
        		TimePref tp = (TimePref)p;
        		if (weakenTime && !tp.getTimePattern().isExactTime()) {
        			tp.weakenHardPreferences();
        			hibSession.merge(tp);
        		}
        	} else {
        		if (!hasPreference(p, currentManagingDept)) {
        			i.remove();
        		} else if (toBeWeakened(p, weakenRoom)) {
        			p.weakenHardPreferences();
    				hibSession.merge(p);
        		}
        	}
        }
        
    	for (DistributionObject distObj: gr.getDistributionObjects()) {
    		DistributionPref dp = distObj.getDistributionPref();
    		Department oldDepartment = (Department)dp.getOwner();
    		Department newDepartment = getDepartment(dp, context);
    		if (newDepartment != null && !newDepartment.equals(oldDepartment)) {
    			dp.setOwner(newDepartment);
    	        boolean weakenDist = true;
    	        if (!newDepartment.isExternalManager()) weakenDist = false;
    	        if (weakenDist && Boolean.TRUE.equals(newDepartment.isAllowReqDistribution()))
    	        	weakenDist = false;
    	        if (weakenDist)
    	        	dp.weakenHardPreferences();
    			hibSession.merge(dp);
    		}
    	}
	}
	
	private void addOrUpdateSubparts(InstrOfferingConfigInterface form, InstrOfferingConfig ioc, Session hibSession, SessionContext context) {
		Map<Long, SchedulingSubpart> tmpSubpartToRealSubpart = new HashMap<>();
		Map<Long, SchedulingSubpart> deletedSubparts = new HashMap<>();
        Map<Long, Department> origSubpartManagingDept = new HashMap<>();

		Map<Long, List<Class_>> oldClasses = new HashMap<Long, List<Class_>>();
		for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
			deletedSubparts.put(ss.getUniqueId(), ss);
			List<Class_> classes = new ArrayList<Class_>(ss.getClasses());
			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
			oldClasses.put(ss.getUniqueId(), classes);
			origSubpartManagingDept.put(ss.getUniqueId(), ss.getManagingDept());
		}
		
		// create new subparts, update subpart properties
		for (SubpartLine line: form.getSubpartLines()) {
			if (line.getSubpartId() < 0l) {
				SchedulingSubpart ss = new SchedulingSubpart();
				ss.setChildSubparts(new HashSet<SchedulingSubpart>());
				ss.setClasses(new HashSet<Class_>());
				
				ss.setItype(ItypeDescDAO.getInstance().get(line.getIType(), hibSession));
	            ss.setAutoSpreadInTime(ApplicationProperty.SchedulingSubpartAutoSpreadInTimeDefault.isTrue());
	            ss.setStudentAllowOverlap(ApplicationProperty.SchedulingSubpartStudentOverlapsDefault.isTrue());
				ss.setMinutesPerWk(line.getMinutesPerWeek());
				
				if (line.getParentId() != null) {
					SchedulingSubpart parent = tmpSubpartToRealSubpart.get(line.getParentId());
					ss.setParentSubpart(parent); parent.addToChildSubparts(ss);
				}
				
				ss.setInstrOfferingConfig(ioc);
				ioc.addToSchedulingSubparts(ss);
				
				hibSession.persist(ss);
				tmpSubpartToRealSubpart.put(line.getSubpartId(), ss);
			} else if (line.isEditable()) {
				SchedulingSubpart ss = deletedSubparts.remove(line.getSubpartId());
				ss.setMinutesPerWk(line.getMinutesPerWeek());
				
				SchedulingSubpart origParent = ss.getParentSubpart();
				SchedulingSubpart newParent = (line.getParentId() == null ? null : tmpSubpartToRealSubpart.get(line.getParentId()));
				if (!ToolBox.equals(origParent, newParent)) {
					ss.setParentSubpart(newParent);
					if (newParent != null)
						newParent.addToChildSubparts(ss);
					if (origParent != null)
						origParent.getChildSubparts().remove(ss);
				}
				
				if (ApplicationProperty.ConfigEditDeleteTimePrefs.isTrue()) {
		            DurationModel model = ioc.getDurationModel();
		            for (Iterator<Preference> i = ss.getPreferences().iterator(); i.hasNext(); ) {
		                Preference pref = i.next();
		                if (pref instanceof TimePref && !model.isValidCombination(ss.getMinutesPerWk(), ss.effectiveDatePattern(), ((TimePref)pref).getTimePattern()))
			                i.remove();
		            }
	            }

				hibSession.merge(ss);
				tmpSubpartToRealSubpart.put(line.getSubpartId(), ss);
			} else {
				SchedulingSubpart ss = deletedSubparts.remove(line.getSubpartId());
				tmpSubpartToRealSubpart.put(line.getSubpartId(), ss);
			}
		}
		
		Map<Long, List<Class_>> tmpSubpartToClasses = new HashMap<>();
		
		// Update classes
		for (SubpartLine line: form.getSubpartLines()) {
			SchedulingSubpart ss = tmpSubpartToRealSubpart.get(line.getSubpartId());
			
			if (line.isEditable()) {
				Long managingDeptId = line.getDepartmentId();
				if (managingDeptId != null && managingDeptId < 0) managingDeptId = null;
				
				List<Class_> adepts = oldClasses.get(ss.getUniqueId());
				if (adepts == null) adepts = new ArrayList<Class_>();
				
				// add classes
				if (ss.getClasses().size() < line.getNumberOfClasses()) {
					for (int i = ss.getClasses().size(); i < line.getNumberOfClasses(); i++) {
						Class_ newClass = new Class_();
						newClass.setSchedulingSubpart(ss);
						ss.addToClasses(newClass);
						newClass.setControllingDept(ss.getControllingDept());
						newClass.setExpectedCapacity(line.getMinClassLimit());
						newClass.setNbrRooms(line.getNumberOfRooms());
						newClass.setMaxExpectedCapacity(line.getMaxClassLimit());
						newClass.setRoomRatio(line.getRoomRatio());
						newClass.setDisplayInstructor(true);
						newClass.setEnabledForStudentScheduling(true);
						newClass.setCancelled(false);
						newClass.setRoomsSplitAttendance(line.getSplitAttendance());
						newClass.setEnrollment(0);
						newClass.setAssignments(new HashSet<Assignment>());
						newClass.setPreferences(new HashSet<Preference>());
						if (managingDeptId != null)
							newClass.setManagingDept(DepartmentDAO.getInstance().get(managingDeptId, hibSession), context.getUser(), hibSession);
						else
							newClass.setManagingDept(ioc.getDepartment(), context.getUser(), hibSession);

						hibSession.persist(newClass);
						adepts.add(newClass);
					}
				}
				
				if (managingDeptId == null)
					managingDeptId = ioc.getDepartment().getUniqueId();
				
				List<Class_> classes = new ArrayList<Class_>(line.getNumberOfClasses());
				tmpSubpartToClasses.put(line.getSubpartId(), classes);
				
				// pick classes to keep
				SubpartLine parent = form.getSubpartLine(line.getParentId());
				List<Class_> parentClasses = (parent == null ? null : tmpSubpartToClasses.get(parent.getSubpartId()));
				int childrenPerParent = -1;
				if (parent != null)
					childrenPerParent = line.getNumberOfClasses() / parent.getNumberOfClasses();
				
				for (int i = 0; i < line.getNumberOfClasses(); i++) {
					Class_ parentClass = (parentClasses == null ? null : parentClasses.get(i / childrenPerParent));
					Class_ adept = null;
					
					AdeptComparator ac = new AdeptComparator(parentClasses, i / childrenPerParent);
					for (Class_ c: adepts) {
						if (ac.isBetter(c, adept))
							adept = c;
					}
					
					if (ApplicationProperty.ConfigEditDeleteTimePrefs.isTrue()) {
			            DurationModel model = ioc.getDurationModel();
			            for (Iterator<Preference> j = adept.getPreferences().iterator(); j.hasNext(); ) {
			                Preference pref = j.next();
			                if (pref instanceof TimePref && !model.isValidCombination(ss.getMinutesPerWk(), ss.effectiveDatePattern(), ((TimePref)pref).getTimePattern()))
				                j.remove();
			            }
		            }

					if (!adept.getManagingDept().getUniqueId().equals(managingDeptId)){
						Department managingDept = DepartmentDAO.getInstance().get(managingDeptId, hibSession);
						adept.setManagingDept(managingDept, context.getUser(), hibSession);
						updatePreferences(adept, ioc.getDepartment(), managingDept, hibSession, context);
					}
					
					if (managingDeptId != null)
						adept.setManagingDept(DepartmentDAO.getInstance().get(managingDeptId, hibSession), context.getUser(), hibSession);
					else
						adept.setManagingDept(ioc.getDepartment(), context.getUser(), hibSession);
					adept.setExpectedCapacity(line.getMinClassLimit());
					adept.setMaxExpectedCapacity(line.getMaxClassLimit());
					adept.setNbrRooms(line.getNumberOfRooms());
					adept.setRoomRatio(line.getRoomRatio());
					adept.setRoomsSplitAttendance(line.getSplitAttendance());
					
					if (adept.getParentClass() != null)
						adept.getParentClass().getChildClasses().remove(adept);
					adept.setParentClass(parentClass);
					if (parentClass != null) parentClass.addToChildClasses(adept);

					hibSession.merge(adept);
					adepts.remove(adept);
					classes.add(adept);
				}
				
				// delete remacontrollingDeptsses
				for (Class_ c: adepts) {
					if (c.getParentClass() != null)
						c.getParentClass().getChildClasses().remove(c);
					
					if (c.getPreferences() != null)
					    c.getPreferences().removeAll(c.getPreferences());
					
					c.deleteAllDependentObjects(hibSession, false);
					
					ss.getClasses().remove(c);
					hibSession.remove(c);
				}
				
	    		Department currentManagingDept = ss.getManagingDept();
	    		Department origManagingDept = origSubpartManagingDept.get(ss.getUniqueId());

	    		if (origManagingDept != null && !currentManagingDept.equals(origManagingDept)) {
	    			updatePreferences(ss, ioc.getDepartment(), currentManagingDept, hibSession, context);
	    			hibSession.merge(ss);
	    		} else if (line.getSubpartId() < 0 && !ioc.isUnlimitedEnrollment() && !ss.getManagingDept().isExternalManager() && line.getNumberOfRooms() > 0) {
					RoomGroup rg = RoomGroup.getGlobalDefaultRoomGroup(ioc.getSession());
					if (rg!=null) {
	    				RoomGroupPref rgp = new RoomGroupPref();
						rgp.setOwner(ss);
						rgp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
						rgp.setRoomGroup(rg);
						ss.addToPreferences(rgp);
						hibSession.merge(ss);
					}
	    		}
			} else {
				List<Class_> classes = new ArrayList<Class_>(ss.getClasses());
				Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
				tmpSubpartToClasses.put(line.getSubpartId(), classes);
			}
		}
		
		// delete old subparts
		for (SchedulingSubpart ss: deletedSubparts.values()) {
			for (Class_ c: new ArrayList<Class_>(ss.getClasses())) {
				if (c.getParentClass() != null && !deletedSubparts.containsKey(c.getParentClass().getSchedulingSubpart().getUniqueId())) {
					Class_ parent = c.getParentClass();
					parent.getChildClasses().remove(c);
				}
				if (c.getPreferences() != null)
				    c.getPreferences().removeAll(c.getPreferences());
				
				c.deleteAllDependentObjects(hibSession, false);
				
				c.getSchedulingSubpart().getClasses().remove(c);
				hibSession.remove(c);
			}

			if (ss.getParentSubpart() != null && !deletedSubparts.containsKey(ss.getParentSubpart().getUniqueId()))
				ss.getParentSubpart().getChildSubparts().remove(ss);
			
			ss.deleteAllDistributionPreferences(hibSession);
			
			ioc.getSchedulingSubparts().remove(ss);

			hibSession.remove(ss);
    	}
	}
	
	protected InstrOfferingConfigInterface delete(InstrOfferingConfigInterface form, SessionContext context) {
		org.hibernate.Session hibSession = InstrOfferingConfigDAO.getInstance().getSession();
		Transaction tx = null;
		try {
	        tx = hibSession.beginTransaction();
	        InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(form.getConfigId(), hibSession);
	        InstructionalOffering io = ioc.getInstructionalOffering();
	        
            Event.deleteFromEvents(hibSession, ioc);
            Exam.deleteFromExams(hibSession, ioc);
            
            Set<DistributionPref> distPrefs = new HashSet<DistributionPref>();
            for (SchedulingSubpart s: ioc.getSchedulingSubparts()) {
            	for (Class_ c: s.getClasses()) {
            		for (DistributionObject distObj: c.getDistributionObjects()) {
            			DistributionPref dp = distObj.getDistributionPref();
            			dp.getDistributionObjects().remove(distObj);
            			hibSession.remove(distObj);
            			distPrefs.add(dp);
            		}
            	    c.deleteTeachingRequests(hibSession);
            	    c.deleteClassInstructors(hibSession);
            	    c.deleteAssignments(hibSession);
            		Exam.deleteFromExams(hibSession, c);
            		Event.deleteFromEvents(hibSession, c);
            	}
            	for (DistributionObject distObj: s.getDistributionObjects()) {
        			DistributionPref dp = distObj.getDistributionPref();
        			dp.getDistributionObjects().remove(distObj);
        			hibSession.remove(distObj);
        			distPrefs.add(dp);
        		}
            }
            
            for (SchedulingSubpart s: ioc.getSchedulingSubparts()) {
            	for (Class_ c: s.getClasses()) {
            		hibSession.remove(c);
            	}
            	hibSession.remove(s);
            }
            
            for (DistributionPref dp: distPrefs) {
            	if (dp.getDistributionObjects().isEmpty())  {
            		hibSession.remove(dp);	
            	} else {
            		int sequenceNumber = 1;
            		for (DistributionObject distObj: new TreeSet<DistributionObject>(dp.getDistributionObjects())) {
            			distObj.setSequenceNumber(sequenceNumber ++);
            			hibSession.merge(distObj);
            		}
            		hibSession.merge(dp);
            	}
            }

            io.getInstrOfferingConfigs().remove(ioc);
            hibSession.remove(ioc);

	        io.computeLabels(hibSession);

            ChangeLog.addChange(
                    hibSession,
                    context,
                    io,
                    io.getCourseName()+" ["+ioc.getName()+"]",
                    ChangeLog.Source.INSTR_CFG_EDIT,
                    ChangeLog.Operation.DELETE,
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);
            
	        hibSession.merge(io);
	        
	        
	        String className = ApplicationProperty.ExternalActionInstrOffrConfigChange.value();
	        ExternalInstrOffrConfigChangeAction configChangeAction = null;
        	if (className != null && className.trim().length() > 0){
	        	configChangeAction = (ExternalInstrOffrConfigChangeAction) Class.forName(className).getDeclaredConstructor().newInstance();
	        	if (!configChangeAction.validateConfigChangeCanOccur(io, hibSession))
	        		throw new Exception("Configuration change violates rules for Add On, rolling back the change.");
        	}
	        
        	tx.commit();

        	if (configChangeAction != null)
	        	configChangeAction.performExternalInstrOffrConfigChangeAction(io, hibSession);
	        return new InstrOfferingConfigInterface(Operation.DELETE, io.getUniqueId(), null);
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
	
	private static class AdeptComparator {
		Class_ iParentClass;
		List<Class_> iParentClasses;
		int iIndex;
		
		private AdeptComparator(List<Class_> parentClasses, int index) {
			iParentClasses = parentClasses;
			iIndex = index;
			iParentClass = (iParentClasses == null ? null : iParentClasses.get(index));
		}

		public boolean isBetter(Class_ c, Class_ adept) {
			if (adept == null)
				return true;
			
			if (iParentClass != null) {
				boolean pc = isParent(c, iParentClass);
				boolean pa = isParent(adept, iParentClass);
				if (pc != pa) {
					// one is parent, the other not -> take c if parent
					return pc;
				}
				if (!pc && !pa) { // not a parent >> prefer classes that are not potential children for later parents 
					boolean ic = iParentClasses.indexOf(c.getParentClass()) < iIndex;
					boolean ia = iParentClasses.indexOf(adept.getParentClass()) < iIndex;
					if (ia != ic) {
						// c has no parent, or a parent that has been already exhausted
						// adept has a parent that comes later
						// 
						return ic;
					}
				}
			}
			
			if (c.isCancelled() != adept.isCancelled()) { // one is cancelled
				// prefer cancelled class
				return c.isCancelled();
			}
			
			if (c.getEnrollment() != adept.getEnrollment()) { // different number of enrolled students
				// prefer class with more enrollments
				return c.getEnrollment() > adept.getEnrollment();
			}
			
			boolean ac = (c.getCommittedAssignment() != null);
			boolean aa = (adept.getCommittedAssignment() != null);
			if (ac != aa) { // one is assigned, the other not
				// prefer assigned class
				return ac;
			}
			
			// prefer earlier selection
			return false;
		}
		
	}
}
