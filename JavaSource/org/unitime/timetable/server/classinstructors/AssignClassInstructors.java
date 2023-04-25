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
package org.unitime.timetable.server.classinstructors;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.DataColumn;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Field;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.FieldType;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Flag;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.ListItem;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.PageName;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Record;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TeachingResponsibility.Option;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAssignClassInstrs")
public class AssignClassInstructors implements AssignClassInstructorsTable {
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;


	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	private String defaultTeachingResponsibilityId;
	public String getDefaultTeachingResponsibilityId() {
		if (defaultTeachingResponsibilityId == null) {
			return("");
		}
		return defaultTeachingResponsibilityId;
	}

	public void setDefaultTeachingResponsibilityId(String defaultTeachingResponsibilityId) {
		this.defaultTeachingResponsibilityId = defaultTeachingResponsibilityId;
	}
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageAssignInstructors(), MESSAGES.pageAssignInstructors());
	}
	
	@SuppressWarnings("unchecked")
	private Field getFieldForColumn(DataColumn dataColumn, InstrOfferingConfig ioc, SessionContext context) {
		switch (dataColumn){
			case CLASS_UID : return new Field(MESSAGES.fieldClassUid(), FieldType.number, 40, Flag.HIDDEN);
			case CLASS_PARENT_UID: return new Field(MESSAGES.fieldClassParentUid(), FieldType.number, 40, Flag.HIDDEN);
			case IS_FIRST_RECORD_FOR_CLASS : return new Field(MESSAGES.fieldFirstRecordForClassUid(), FieldType.toggle, 40, Flag.HIDDEN);
			case HAS_ERROR : return new Field(MESSAGES.fieldError(), FieldType.hasError, 40, Flag.HIDE_LABEL);
			case CLASS_NAME : return new Field(MESSAGES.fieldClassName(), FieldType.textarea, 120, Flag.READ_ONLY, Flag.HIDE_LABEL);
			case CLASS_EXTERNAL_UID : return new Field(MESSAGES.fieldExternalId(), FieldType.textarea, 120, Flag.READ_ONLY);
			case ADD : return new Field(MESSAGES.fieldAdd(), FieldType.add, 40, Flag.HIDE_LABEL);
			case DELETE : return new Field(MESSAGES.fieldDelete(), FieldType.delete, 40, Flag.HIDE_LABEL);
			case INSTR_NAME :
					String nf = UserProperty.NameFormat.get(context.getUser());
			        ArrayList<DepartmentalInstructor> instructorList = new ArrayList<DepartmentalInstructor>(ioc.getDepartment().getInstructors());
			        if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
			        	Collections.sort(instructorList, new DepartmentalInstructorComparator(nf));
			        else
			        	Collections.sort(instructorList, new DepartmentalInstructorComparator());
			
					List<ListItem> instructors = new ArrayList<ListItem>();
					instructors.add(new ListItem("-1", MESSAGES.itemSelect()));
					for (DepartmentalInstructor di : instructorList) {
						instructors.add(new ListItem(di.getUniqueId().toString(), di.getName(nf)));
					}
					return new Field(MESSAGES.colNamePerson(), FieldType.list, 300, instructors);
			case PCT_SHARE : return new Field(MESSAGES.fieldPercentShare(), FieldType.number, 60, 40);
			case CHECK_CONFICTS : return new Field(MESSAGES.fieldCheckConflicts(), FieldType.toggle, 120, 40);
			case RESPONSIBILITY : 
					List<ListItem> responsibilities = new ArrayList<ListItem>();
					for (TeachingResponsibility tr : TeachingResponsibility.getInstructorTeachingResponsibilities()) {
						responsibilities.add(new ListItem(tr.getUniqueId().toString(), tr.getLabel()));
						if (tr.hasOption(Option.isdefault)) {
							setDefaultTeachingResponsibilityId(tr.getUniqueId().toString());
						}
					}
					Flag respFlag = null;
					if (responsibilities.isEmpty()) {
						respFlag = Flag.HIDDEN;
					} else {
						if (getDefaultTeachingResponsibilityId().equals("")){
							responsibilities.add(0, new ListItem("", ""));
						}
					}
					return new Field(MESSAGES.fieldResponsibility(), FieldType.list, 100, responsibilities, respFlag);
			case DISPLAY : return new Field(MESSAGES.fieldDisplay("&otimes;"), FieldType.toggle, 120);
			case FUNDING_DEPT: 
					List<ListItem> fundingDepts = new ArrayList<ListItem>();
					boolean fundingDeptsUsed = false;
					for (Department d : ioc.findPossibleFundingDepts(DepartmentDAO.getInstance().getSession())) {
						if (d.isExternalFundingDept() != null && d.isExternalFundingDept()) {
							fundingDeptsUsed = true;
						}
						fundingDepts.add(new ListItem(d.getUniqueId().toString(), d.getLabel()));
					}
					Flag fdFlag = null;
					if (!fundingDeptsUsed) {
						fdFlag = Flag.HIDDEN;
					}
					return new Field(MESSAGES.fieldFundingDepartment(), FieldType.list, 100, fundingDepts, fdFlag);
			case TIME :
				return new Field(MESSAGES.colTime(), FieldType.textarea, 120, Flag.READ_ONLY);
			case ROOM : 
				return new Field(MESSAGES.colRoom(), FieldType.textarea, 120, Flag.READ_ONLY);	
			default: return new Field(dataColumn.name(), FieldType.textarea, 120);
		}
		
	}

	@Override
	public AssignClassInstructorsInterface load(String cfgIdStr, SessionContext context, Session hibSession) throws Exception {
		Long cfgId = null;
		if (cfgIdStr != null && !cfgIdStr.trim().equals("")) {
			cfgId = Long.parseLong(cfgIdStr);
		} else {
			throw new Exception(MESSAGES.errorConfigurationIdNotFound());
			
		}
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(cfgId, hibSession);
		Field[] fields = new Field[DataColumn.values().length];
		for (DataColumn dc : DataColumn.values()) {
			fields[dc.ordinal()] = getFieldForColumn(dc, ioc, context);
		}
		
		AssignClassInstructorsInterface data = new AssignClassInstructorsInterface(fields);
		data.setConfigId(ioc.getUniqueId());
		data.setOfferingId(ioc.getInstructionalOffering().getUniqueId());
		data.setCourseName(ioc.getInstructionalOffering().getCourseNameWithTitle());

        String coordinators = "";
        String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
        List<OfferingCoordinator> coordinatorList = new ArrayList<OfferingCoordinator>(ioc.getInstructionalOffering().getOfferingCoordinators());
        Collections.sort(coordinatorList, new OfferingCoordinatorComparator(context));
        for (OfferingCoordinator coordinator: coordinatorList) {
        	if (!coordinators.isEmpty()) coordinators += "\n";
        	coordinators += coordinator.getInstructor().getName(instructorNameFormat) +
        			(coordinator.getResponsibility() == null ? 
        					(coordinator.getPercentShare() != 0 ? " (" + coordinator.getPercentShare() + "%)" : "") :
        					" (" + coordinator.getResponsibility().getLabel() + (coordinator.getPercentShare() > 0 ? ", " + coordinator.getPercentShare() + "%" : "") + ")");
        }
        data.setCourseCoordinators(coordinators);
		
		InstrOfferingConfig next = ioc.getNextInstrOfferingConfig(context);
		if (next != null) {
			data.setNextConfigId(next.getUniqueId());
		}
		InstrOfferingConfig prev = ioc.getPreviousInstrOfferingConfig(context);
		if (prev != null) {
			data.setPreviousConfigId(prev.getUniqueId());
		}
		
		try {
			addRecordsForConfig(ioc, data, context);
		} catch (Exception e) {
			e.printStackTrace();
		}

		data.setEditable(true);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private void addRecordsForConfig(InstrOfferingConfig ioc, AssignClassInstructorsInterface data, SessionContext context) throws Exception {
		 Debug.info("In addRecordsForConfig");
		 ArrayList<SchedulingSubpart> subpartList = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
	        Collections.sort(subpartList, new SchedulingSubpartComparator());

	        for(SchedulingSubpart ss : subpartList){
	    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
	    			throw new Exception(MESSAGES.exceptionInitialIOSetupIncomplete());
	    		if (ss.getParentSubpart() == null){
	        		loadClasses(data, ss.getClasses(), new String(), context);
	        	}
	        }
		
	}
	
    @SuppressWarnings("unchecked")
	private void loadClasses(AssignClassInstructorsInterface data, Set<Class_> classes, String indent, SessionContext context){
    	if (classes != null && classes.size() > 0){
    		ArrayList<Class_> classesList = new ArrayList<Class_>(classes);

    		if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(context.getUser()))) {
        		Collections.sort(classesList,
        			new ClassCourseComparator(
        					context.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					classAssignmentService.getAssignment(),
        					false
        			)
        		);
        	} else {
        		Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
        	}

	    	for(Class_ cls : classesList){
	    		addClassRecords(data, cls, !context.hasPermission(cls, Right.AssignInstructorsClass), indent, context);
	    		loadClasses(data, cls.getChildClasses(), indent + "&nbsp;&nbsp;&nbsp;&nbsp;", context);
	    	}
    	}
    }
	
	@SuppressWarnings("unchecked")
	public void addClassRecords(AssignClassInstructorsInterface data, Class_ cls, Boolean isReadOnly, String indent, SessionContext cx){
		ArrayList<ClassInstructor> instructors = new ArrayList<ClassInstructor>(cls.getClassInstructors());
		Collections.sort(instructors, new InstructorComparator(cx));
		ClassInstructor instructor = null;
		boolean isEditable = !isReadOnly;

		int i = 0;
		do {
			int recId = data.getRecords().size() ;
			Record rec = data.addRecord(Long.valueOf(recId));
			rec.setDeletable(isEditable);

			rec.setField(DataColumn.CLASS_UID.ordinal(), cls.getUniqueId().toString(), false);
			if (ApplicationProperty.InstructorsCopyToSubSubparts.isTrue()) {
				Class_ parent = cls;
				while (parent.getParentClass() != null && parent.getSchedulingSubpart().getItype().equals(parent.getParentClass().getSchedulingSubpart().getItype()))
					parent = parent.getParentClass();
				if (!cls.equals(parent))
					rec.setField(DataColumn.CLASS_PARENT_UID.ordinal(), parent.getUniqueId().toString(), false);
			}
			rec.setField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal(), Boolean.valueOf(i == 0).toString(), false);
			rec.setField(DataColumn.HAS_ERROR.ordinal(), Boolean.FALSE.toString(), true, true);
			if (instructors.size() <= 1) {
				rec.setField(DataColumn.DELETE.ordinal(), Boolean.FALSE.toString(), isEditable);
			} else {
				rec.setField(DataColumn.DELETE.ordinal(), Boolean.TRUE.toString(), isEditable);				
			}
			if (isEditable) {
				rec.setField(DataColumn.ADD.ordinal(), Boolean.TRUE.toString(), isEditable);
			} else {
				rec.setField(DataColumn.ADD.ordinal(), Boolean.FALSE.toString(), isEditable);				
			}
			
			if(instructors.size() > 0) {
				instructor = (ClassInstructor) instructors.get(i);
			}
			
			// Only display the class name, time, room, display flag, and funding department on the first instructor row
			rec.setField(DataColumn.CLASS_NAME.ordinal(), indent + cls.htmlLabel(), false, i == 0);
			rec.setField(DataColumn.CLASS_EXTERNAL_UID.ordinal(), cls.getClassSuffix() == null?"":cls.getClassSuffix(), false, i == 0);
			String time = cls.buildAssignedTimeHtml(classAssignmentService.getAssignment());
			if (time != null && !time.trim().equals("")) {
				data.setShowTimeAndRoom(true);
			}
			rec.setField(DataColumn.TIME.ordinal(), time, false, i == 0); 
			rec.setField(DataColumn.ROOM.ordinal(), cls.buildAssignedRoomHtml(classAssignmentService.getAssignment()), false, i == 0);
			rec.setField(DataColumn.DISPLAY.ordinal(), cls.isDisplayInstructor().toString(), isEditable, i == 0);			
			rec.setField(DataColumn.FUNDING_DEPT.ordinal(), cls.getEffectiveFundingDept() == null ? "" : cls.getEffectiveFundingDept().getUniqueId().toString(), isEditable, i == 0);
			if(instructors.size() > 0) {
				rec.setField(DataColumn.INSTR_NAME.ordinal(), instructor.getInstructor().getUniqueId().toString(), isEditable, true);
				rec.setField(DataColumn.PCT_SHARE.ordinal(), instructor.getPercentShare().toString(), isEditable, true);
				rec.setField(DataColumn.CHECK_CONFICTS.ordinal(), instructor.isLead().toString(), isEditable, true);
				rec.setField(DataColumn.RESPONSIBILITY.ordinal(), instructor.getResponsibility() == null ? getDefaultTeachingResponsibilityId() : instructor.getResponsibility().getUniqueId().toString(), isEditable, true);
			}
			else {
				rec.setField(DataColumn.INSTR_NAME.ordinal(), "", isEditable, true);
				rec.setField(DataColumn.PCT_SHARE.ordinal(), "100", isEditable);
				rec.setField(DataColumn.CHECK_CONFICTS.ordinal(), Boolean.TRUE.toString(), isEditable, true);
				rec.setField(DataColumn.RESPONSIBILITY.ordinal(), getDefaultTeachingResponsibilityId(), isEditable, true);
			}
			
		} while (++i < instructors.size());
	}

	private boolean matchesInstructorIncludingResponsibility(Record record, ClassInstructor classInstructor){
		if (matchesInstructor(record, classInstructor)) {
			if (record.getField(DataColumn.RESPONSIBILITY.ordinal()).equals("") 
					&& classInstructor.getResponsibility() == null) {
				return true;
			}
			if (!record.getField(DataColumn.RESPONSIBILITY.ordinal()).equals("") 
					&& classInstructor.getResponsibility() != null
					&& record.getField(DataColumn.RESPONSIBILITY.ordinal()).equals(classInstructor.getResponsibility().getUniqueId().toString())) {
				return true;
			}
		}
		return false;
	}

	private boolean matchesInstructor(Record record, ClassInstructor classInstructor){
		return (record.getField(DataColumn.CLASS_UID.ordinal()) != null 
				&& record.getField(DataColumn.CLASS_UID.ordinal()).equals(classInstructor.getClassInstructing().getUniqueId().toString()) 
				&& record.getField(DataColumn.INSTR_NAME.ordinal()) != null 
				&& record.getField(DataColumn.INSTR_NAME.ordinal()).equals(classInstructor.getInstructor().getUniqueId().toString()));

	}
	
	private ClassInstructor findMatchingInstructorForRecord(Record record, HashSet<ClassInstructor> classInstructors) {
		ArrayList<ClassInstructor> possibleMatches = new ArrayList<ClassInstructor>();
		for (ClassInstructor ci : classInstructors) {
			if (matchesInstructor(record, ci)) {
				possibleMatches.add(ci);
			}
		}
		if (possibleMatches.size() > 1) {
			for (ClassInstructor ci : possibleMatches) {
				if (matchesInstructorIncludingResponsibility(record, ci)) {
					return ci;
				}
			}
		} else if (possibleMatches.size() == 1) {
			return(possibleMatches.get(0));
		}
		return null;
	}

	private ClassInstructor findMatchingInstructorIncludingResponsibilityForRecord(Record record, HashSet<ClassInstructor> classInstructors) {
		ArrayList<ClassInstructor> possibleMatches = new ArrayList<ClassInstructor>();
		for (ClassInstructor ci : classInstructors) {
			if (matchesInstructor(record, ci)) {
				possibleMatches.add(ci);
			}
		}
		for (ClassInstructor ci : possibleMatches) {
			if (matchesInstructorIncludingResponsibility(record, ci)) {
				return ci;
			}
		}
		return null;
	}

	@Override
	public void save(AssignClassInstructorsInterface data, SessionContext context, org.hibernate.Session hibSession) {
        boolean somethingChanged = false;
		if (data.getConfigId() == null) {
			throw new Error(MESSAGES.errorConfigurationIdNotProvided());
		}
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(data.getConfigId());
		if (ioc == null) {
			throw new Error(MESSAGES.errorConfigurationIdNotFound());
		}
		HashMap<String, ArrayList<Record>> classIdRecordMap = new HashMap<String, ArrayList<Record>>();
		for (Record r : data.getRecords()) {
			ArrayList<Record> records = classIdRecordMap.get(r.getField(DataColumn.CLASS_UID.ordinal()));
			if (records == null) {
				records = new ArrayList<Record>();
				classIdRecordMap.put(r.getField(DataColumn.CLASS_UID.ordinal()), records);
			}
			records.add(r);
		}
		StringBuffer sbErrorsFound = new StringBuffer();
		boolean errorsFound = false;
		for (SchedulingSubpart ss : ioc.getSchedulingSubparts()) {
			for (Class_ c : ss.getClasses()) {
				ArrayList<Record> records = classIdRecordMap.get(c.getUniqueId().toString());
				if (records == null) {
					errorsFound = true;
					sbErrorsFound.append(MESSAGES.errorInstructorInputDataNotFoundForClass(c.getClassLabel(true)))
					             .append("\n<br>");
					continue;
				}
				HashSet<ClassInstructor> origInstrs = new HashSet<ClassInstructor>();
				origInstrs.addAll(c.getClassInstructors());
				HashSet<ClassInstructor> newInstrs = new HashSet<ClassInstructor>();

				for (Record r : records) {
					if (r == null) {
						continue;
					}
					if (r.getField(DataColumn.HAS_ERROR.ordinal()).equals(Boolean.TRUE.toString())) {
						r.setField(DataColumn.HAS_ERROR.ordinal(), Boolean.FALSE.toString(), true, true);
					}
					if (r.getField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal()).equals(Boolean.TRUE.toString())) {
						if (!r.getField(DataColumn.DISPLAY.ordinal()).equals(c.isDisplayInstructor().toString())) {
							c.setDisplayInstructor(Boolean.parseBoolean(r.getField(DataColumn.DISPLAY.ordinal())));
							hibSession.merge(c);
							somethingChanged = true;
						}
						if (!r.getField(DataColumn.FUNDING_DEPT.ordinal()).equals(c.getEffectiveFundingDept() == null ? "" : c.getEffectiveFundingDept().getUniqueId().toString())) {
							if (!r.getField(DataColumn.FUNDING_DEPT.ordinal()).equals("")){
								c.setFundingDept(DepartmentDAO.getInstance().get(Long.valueOf(r.getField(DataColumn.FUNDING_DEPT.ordinal()))));
							} else {
								c.setFundingDept(null);								
							}
							hibSession.merge(c);
							somethingChanged = true;							
						}
					}
					ClassInstructor ci = findMatchingInstructorForRecord(r, origInstrs);
					if (ci != null) {
						origInstrs.remove(ci);
						newInstrs.add(ci);
						boolean changed = false;
						if (!r.getField(DataColumn.CHECK_CONFICTS.ordinal()).equals(ci.getLead().toString())) {
							ci.setLead(Boolean.parseBoolean(r.getField(DataColumn.CHECK_CONFICTS.ordinal())));
							changed = true;
						}
						if (!r.getField(DataColumn.PCT_SHARE.ordinal()).equals(ci.getPercentShare().toString())) {
							ci.setPercentShare(Integer.parseInt(r.getField(DataColumn.PCT_SHARE.ordinal())));
							changed = true;
						}
						if ((ci.getResponsibility() != null && (r.getField(DataColumn.RESPONSIBILITY.ordinal()) == null || !r.getField(DataColumn.RESPONSIBILITY.ordinal()).equals(ci.getResponsibility().getUniqueId().toString())))
								|| (ci.getResponsibility() == null && !r.getField(DataColumn.RESPONSIBILITY.ordinal()).equals(""))) {
							if (r.getField(DataColumn.RESPONSIBILITY.ordinal()) == null || r.getField(DataColumn.RESPONSIBILITY.ordinal()).equals("")) {
								if (TeachingResponsibility.getDefaultInstructorTeachingResponsibility() != null) {
									ci.setResponsibility(TeachingResponsibility.getDefaultInstructorTeachingResponsibility());
								} else {
									ci.setResponsibility(null);
								}
							} else {
								ci.setResponsibility(TeachingResponsibilityDAO.getInstance().get(Long.parseLong(r.getField(DataColumn.RESPONSIBILITY.ordinal()))));
							}
							changed = true;
						}
						
						if (changed) {
							hibSession.merge(ci);
							somethingChanged = true;
						}
					} else {
						ClassInstructor dupCi = findMatchingInstructorIncludingResponsibilityForRecord(r, newInstrs);
						if (dupCi != null) {
							r.setField(DataColumn.HAS_ERROR.ordinal(), Boolean.TRUE.toString(), true, true);
							sbErrorsFound.append(MESSAGES.errorDuplicateInstructorData(dupCi.getInstructor().getName(UserProperty.NameFormat.get(context.getUser())), (dupCi.getResponsibility() == null ? "" : dupCi.getResponsibility().getLabel()), c.getClassLabel(true)))
				             .append("\n<br>");
							errorsFound = true;
							continue;
						}
						if (r.getField(DataColumn.HAS_ERROR.ordinal()).equals(Boolean.TRUE.toString())) {
							r.setField(DataColumn.HAS_ERROR.ordinal(), Boolean.FALSE.toString(), true, true);
						}
						String instrIdStr = r.getField(DataColumn.INSTR_NAME.ordinal());
						if (instrIdStr == null || instrIdStr.trim().equals("") || instrIdStr.trim().equals("-1")) {
							if (!r.getField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal()).equals(Boolean.TRUE.toString())) {
								data.getRecords().remove(r);							
							} else {
								if (records.size() > 1 && (records.indexOf(r) + 1) < records.size()) {
									Record newFirst = records.get(records.indexOf(r) + 1);
									newFirst.setField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal(), Boolean.TRUE.toString(), false, false);
									newFirst.setField(DataColumn.CLASS_NAME.ordinal(), r.getField(DataColumn.CLASS_NAME.ordinal()), false, true);
									newFirst.setField(DataColumn.CLASS_EXTERNAL_UID.ordinal(), r.getField(DataColumn.CLASS_EXTERNAL_UID.ordinal()), false, true);
									newFirst.setField(DataColumn.TIME.ordinal(), r.getField(DataColumn.TIME.ordinal()), false, true);
									newFirst.setField(DataColumn.ROOM.ordinal(), r.getField(DataColumn.ROOM.ordinal()), false, true);
									newFirst.setField(DataColumn.DISPLAY.ordinal(), r.getField(DataColumn.DISPLAY.ordinal()), r.isEditable(DataColumn.DISPLAY.ordinal()), true);
									newFirst.setField(DataColumn.DELETE.ordinal(), Boolean.valueOf(records.size() > 2).toString(), false, false);
									data.getRecords().remove(r);							
								} else {
									r.setField(DataColumn.INSTR_NAME.ordinal(), "", r.isEditable(DataColumn.INSTR_NAME.ordinal()), r.isVisible(DataColumn.INSTR_NAME.ordinal()));
									r.setField(DataColumn.RESPONSIBILITY.ordinal(), getDefaultTeachingResponsibilityId(), r.isEditable(DataColumn.RESPONSIBILITY.ordinal()), r.isVisible(DataColumn.RESPONSIBILITY.ordinal()));
									r.setField(DataColumn.PCT_SHARE.ordinal(), "100", r.isEditable(DataColumn.PCT_SHARE.ordinal()), r.isVisible(DataColumn.PCT_SHARE.ordinal()));
									r.setField(DataColumn.CHECK_CONFICTS.ordinal(), Boolean.TRUE.toString(), r.isEditable(DataColumn.CHECK_CONFICTS.ordinal()), r.isVisible(DataColumn.CHECK_CONFICTS.ordinal()));
								}
							}
							continue;
						}
						ci = new ClassInstructor();
						ci.setClassInstructing(c);
						c.addToclassInstructors(ci);
						DepartmentalInstructor di = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instrIdStr));
						if (di == null) {
							continue;
						}
						ci.setInstructor(di);
						di.addToclasses(ci);
						ci.setLead(Boolean.parseBoolean(r.getField(DataColumn.CHECK_CONFICTS.ordinal())));
						String pctShareStr = r.getField(DataColumn.PCT_SHARE.ordinal());
						if (pctShareStr == null || pctShareStr.trim().equals("")) {
							pctShareStr = "0";
						}
						ci.setPercentShare(Integer.parseInt(pctShareStr));
						if (r.getField(DataColumn.RESPONSIBILITY.ordinal()).equals("")) {
							if (TeachingResponsibility.getDefaultInstructorTeachingResponsibility() != null) {
								ci.setResponsibility(TeachingResponsibility.getDefaultInstructorTeachingResponsibility());
							} else {
								ci.setResponsibility(null);
							}
						} else {
							ci.setResponsibility(TeachingResponsibilityDAO.getInstance().get(Long.parseLong(r.getField(DataColumn.RESPONSIBILITY.ordinal()))));
						}
						ci.setUniqueId((Long)hibSession.save(ci));
						newInstrs.add(ci);
						somethingChanged = true;
					}
				}
				for (ClassInstructor ci : origInstrs) {
					c.removeClassInstructor(ci);
					ci.getInstructor().removeClassInstructor(ci);
					hibSession.remove(ci);
					somethingChanged = true;
				}
			}
		}
		
		if (!errorsFound && somethingChanged) {
            ChangeLog.addChange(
            		hibSession,
                    context,
                    ioc,
                    ChangeLog.Source.CLASS_INSTR_ASSIGN,
                    ChangeLog.Operation.UPDATE,
                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);
            
        	if (permissionOfferingLockNeeded.check(context.getUser(), ioc.getInstructionalOffering())) {
        		StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), ioc.getInstructionalOffering().getSessionId(), ioc.getInstructionalOffering().getUniqueId());
        	}
        	
        	hibSession.flush();

        	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstrOfferingConfigAssignInstructorsAction assignAction = null;
				try {
					assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).getDeclaredConstructor().newInstance());
				} catch (InstantiationException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (SecurityException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					Debug.info(MESSAGES.exceptionExternalSystemUpdateFailure(e.getClass().getSimpleName(), className));
					e.printStackTrace();
				}
				if (assignAction != null) {
					assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, InstrOfferingConfigDAO.getInstance().getSession());
				}
        	}
		}
		data.setErrors(sbErrorsFound.toString());
		data.setSaveSuccessful(!errorsFound);
				
	}
	

	@Override
	public void removeAllInstructors(AssignClassInstructorsInterface data, SessionContext context, Session hibSession) throws Exception {

		if (data.getConfigId() == null) {
			throw new Exception(MESSAGES.errorConfigurationIdNotProvided());
		}
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(data.getConfigId());
		if (ioc == null) {
			throw new Exception(MESSAGES.errorConfigurationIdNotFound());
		}

		for (SchedulingSubpart ss : ioc.getSchedulingSubparts()) {
			for (Class_ c : ss.getClasses()) {
				if (!context.hasPermission(c, Right.AssignInstructorsClass)) {
					throw (new Exception(MESSAGES.errorDeleteAllInstructorsPermission()));
				}
			}
		}
		boolean changed = false;
		for (SchedulingSubpart ss : ioc.getSchedulingSubparts()) {
			for (Class_ c : ss.getClasses()) {
				HashSet<ClassInstructor> origInstrs = new HashSet<ClassInstructor>();
				origInstrs.addAll(c.getClassInstructors());

				for (ClassInstructor ci : origInstrs) {
					c.removeClassInstructor(ci);
					ci.getInstructor().removeClassInstructor(ci);
					hibSession.remove(ci);
					changed = true;
				}
			}
		}
		ArrayList<Record> records = new ArrayList<Record>();
		records.addAll(data.getRecords());
		for (Record r : records) {
			if (r.getField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal()).equals(Boolean.TRUE.toString())) {
				r.setField(DataColumn.DELETE.ordinal(), Boolean.FALSE.toString(), false, false);
				r.setField(DataColumn.INSTR_NAME.ordinal(), "", r.isEditable(DataColumn.INSTR_NAME.ordinal()), r.isVisible(DataColumn.INSTR_NAME.ordinal()));
				r.setField(DataColumn.RESPONSIBILITY.ordinal(), getDefaultTeachingResponsibilityId(), r.isEditable(DataColumn.RESPONSIBILITY.ordinal()), r.isVisible(DataColumn.RESPONSIBILITY.ordinal()));
				r.setField(DataColumn.PCT_SHARE.ordinal(), "100", r.isEditable(DataColumn.PCT_SHARE.ordinal()), r.isVisible(DataColumn.PCT_SHARE.ordinal()));
				r.setField(DataColumn.CHECK_CONFICTS.ordinal(), Boolean.TRUE.toString(), r.isEditable(DataColumn.CHECK_CONFICTS.ordinal()), r.isVisible(DataColumn.CHECK_CONFICTS.ordinal()));
			} else {
				data.getRecords().remove(r);
			}

		}
		
		if (changed) {

	        ChangeLog.addChange(
	        		hibSession,
	                context,
	                ioc,
	                ChangeLog.Source.CLASS_INSTR_ASSIGN,
	                ChangeLog.Operation.DELETE,
	                ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
	                null);
	        
	    	if (permissionOfferingLockNeeded.check(context.getUser(), ioc.getInstructionalOffering())) {
	    		StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), ioc.getInstructionalOffering().getSessionId(), ioc.getInstructionalOffering().getUniqueId());
	    	}
	    	
	    	hibSession.flush();
	
	    	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
	    	if (className != null && className.trim().length() > 0){
	        	ExternalInstrOfferingConfigAssignInstructorsAction assignAction = null;
				try {
					assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).getDeclaredConstructor().newInstance());
				} catch (InstantiationException e) {
					Debug.info("Failed to Send Update to External System:  InstantiationException = " + className);
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					Debug.info("Failed to Send Update to External System:  IllegalAccessException = " + className);
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					Debug.info("Failed to Send Update to External System:  IllegalArgumentException = " + className);
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					Debug.info("Failed to Send Update to External System:  InvocationTargetException = " + className);
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					Debug.info("Failed to Send Update to External System:  NoSuchMethodException = " + className);
					e.printStackTrace();
				} catch (SecurityException e) {
					Debug.info("Failed to Send Update to External System:  SecurityException = " + className);
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					Debug.info("Failed to Send Update to External System:  ClassNotFoundException = " + className);
					e.printStackTrace();
				}
				if (assignAction != null) {
					assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, InstrOfferingConfigDAO.getInstance().getSession());
				}
	    	}
		}
	}
}
