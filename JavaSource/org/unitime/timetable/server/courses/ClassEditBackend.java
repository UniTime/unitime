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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassInstr;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InheritInstructorPrefs;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefGroupEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefLevel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PreferenceType;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Preferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePreferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimeSelection;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.StudentAccomodation.AccommodationCounter;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.RequiredTimeTable;

@GwtRpcImplements(ClassEditRequest.class)
public class ClassEditBackend implements GwtRpcImplementation<ClassEditRequest, ClassEditResponse> {
	protected static final CourseMessages CMSG = Localization.create(CourseMessages.class);
	
	@Override
	public ClassEditResponse execute(ClassEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
		Class_ clazz = Class_DAO.getInstance().get(request.getId());
		context.checkPermission(clazz, Right.ClassEdit);
        TeachingResponsibility defaultTR = TeachingResponsibility.getDefaultInstructorTeachingResponsibility();

		if (request.getOperation() != null) {
			ClassEditResponse ret;
			switch (request.getOperation()) {
			case CLEAR_CLASS_PREFS:
				context.checkPermission(clazz, Right.ClassEditClearPreferences);

				doClear(clazz.getPreferences(),
						Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
				hibSession.merge(clazz);
				hibSession.flush();

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    clazz,
	                    ChangeLog.Source.CLASS_EDIT,
	                    ChangeLog.Operation.CLEAR_PREF,
	                    clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
	                    clazz.getManagingDept());
				
				ret = new ClassEditResponse();
				ret.setUrl("clazz?id=" + clazz.getUniqueId());
				return ret;
			case UPDATE:
			case NEXT:
			case PREVIOUS:
				
				Transaction tx = hibSession.beginTransaction();
				try {
					ClassEditResponse data = request.getPayLoad();
					clazz.setDisplayInstructor(data.isDisplayInstructors());
					clazz.setEnabledForStudentScheduling(data.isStudentScheduling());
					clazz.setSchedulePrintNote(data.getScheduleNote());
					clazz.setNotes(data.getRequestNote());
					clazz.setDatePattern(data.getDatePatternId() == null ? null : DatePatternDAO.getInstance().get(data.getDatePatternId()));
					
					boolean assignTeachingRequest = Department.isInstructorSchedulingCommitted(clazz.getControllingDept().getUniqueId());
					Set<ClassInstructor> classInstrs = new HashSet<ClassInstructor>(clazz.getClassInstructors());
					if (data.hasClassInstructors()) {
						for (ClassInstr ci: data.getClassInstructors()) {
							if (ci.getInstructorId() == null) continue;
							DepartmentalInstructor deptInstr = DepartmentalInstructorDAO.getInstance().get(ci.getInstructorId());
							
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
				                classInstr.setClassInstructing(clazz);
				                classInstr.setInstructor(deptInstr);
				                deptInstr.getClasses().add(classInstr);
				                clazz.getClassInstructors().add(classInstr);
				                if (assignTeachingRequest) {
				                	for (TeachingClassRequest tcr: clazz.getTeachingRequests()) {
				                		if (tcr.getAssignInstructor() && tcr.getTeachingRequest().getAssignedInstructors().contains(deptInstr)) {
				                			classInstr.setTeachingRequest(tcr.getTeachingRequest());
				                			break;
				                		}
				                	}
				                }
				            }
				            classInstr.setLead(ci.isCheckConflicts());
				            classInstr.setPercentShare(ci.getPercentShare());
				            classInstr.setResponsibility(ci.getResponsibilityId() == null ? defaultTR : TeachingResponsibilityDAO.getInstance().get(ci.getResponsibilityId()));
				        }
					}
			        for (Iterator<ClassInstructor> iter = classInstrs.iterator(); iter.hasNext() ;) {
			            ClassInstructor ci = iter.next();
			            DepartmentalInstructor instr = ci.getInstructor();
			            instr.getClasses().remove(ci);
			            clazz.getClassInstructors().remove(ci);
			            hibSession.merge(instr);
			            hibSession.remove(ci);
			        }
			        
					doUpdate(clazz, clazz.getPreferences(), data,
							Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);

			        hibSession.merge(clazz);
			        
		            ChangeLog.addChange(
		                    null,
		                    context,
		                    clazz,
		                    ChangeLog.Source.CLASS_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
		                    clazz.getManagingDept());
					
                    String className = ApplicationProperty.ExternalActionClassEdit.value();
                	if (className != null && className.trim().length() > 0){
                    	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
                   		editAction.performExternalClassEditAction(clazz, hibSession);
                	}

                	tx.commit();
					tx = null;
				} catch (Exception e) {
					if (tx != null) { tx.rollback(); }
					throw new GwtRpcException(e.getMessage(), e);
				}
				
				ret = new ClassEditResponse();
				if (request.getOperation() == Operation.PREVIOUS && request.getPayLoad().getPreviousId() != null)
					ret.setUrl("classEdit?id=" + request.getPayLoad().getPreviousId());
				else if (request.getOperation() == Operation.NEXT  && request.getPayLoad().getNextId() != null)
					ret.setUrl("classEdit?id=" + request.getPayLoad().getNextId());
				else
					ret.setUrl("clazz?id=" + clazz.getUniqueId());
				return ret;
			case DATE_PATTERN:
				// update date and time preferences using the provided date pattern id
				ret = request.getPayLoad();
				DatePattern datePattern = DatePatternDAO.getInstance().get(ret.getDatePatternId());
				if (datePattern == null)
					datePattern = clazz.getSchedulingSubpart().effectiveDatePattern();
				
				List<Selection> dateSelections = (ret.getDatePreferences() == null ? null : ret.getDatePreferences().getSelections());
				fillInDatePreferences(ret, clazz, null, datePattern, context, false);
				if (ret.getDatePreferences() != null && dateSelections != null) {
					for (Selection selection: dateSelections)
						if (ret.getDatePreferences().getItem(selection.getItem()) != null)
							ret.getDatePreferences().addSelection(selection);
				}
				
				List<TimeSelection> timeSelections = (ret.getTimePreferences() == null ? null : ret.getTimePreferences().getSelections());
				fillInTimePreferences(ret, clazz, null, datePattern, context, false);
				if (ret.getTimePreferences() != null && timeSelections != null) {
					for (TimeSelection selection: timeSelections) {
						TimePatternModel m = ret.getTimePreferences().getItem(selection.getItem());
						if (m == null) {
							TimePattern tp = TimePatternDAO.getInstance().get(selection.getItem());
							if (tp != null) {
								m = createTimePatternModel(clazz, tp, context);
								m.setValid(false);
								ret.getTimePreferences().addItem(m);
							}
						}
						if (m != null) ret.getTimePreferences().addSelection(selection);
					}
				}
				return ret;
			case INSTRUCTORS:
				ret = request.getPayLoad();
				datePattern = clazz.getSchedulingSubpart().effectiveDatePattern();
				if (ret.getDatePatternId() != null && ret.getDatePatternId() >= 0)
					datePattern = DatePatternDAO.getInstance().get(ret.getDatePatternId());
				Vector<DepartmentalInstructor> leadInstructors = new Vector<DepartmentalInstructor>();
				if (ret.hasClassInstructors())
					for (ClassInstr ci: ret.getClassInstructors()) {
						if (ci.getInstructorId() == null || !ci.isCheckConflicts()) continue;
						DepartmentalInstructor di = DepartmentalInstructorDAO.getInstance().get(ci.getInstructorId());
						if (di != null) leadInstructors.add(di);
					}
				fillInTimePreferences(ret, clazz, leadInstructors, datePattern, context, true);
				// fillInDatePreferences(ret, clazz, leadInstructors, datePattern, context, true);
				fillInRoomPreferences(ret, clazz, leadInstructors, context, true);
				return ret;
			default:
				break;
			}
		}
		
		ClassEditResponse ret = new ClassEditResponse();
		ret.setId(request.getId());
		ret.setName(clazz.getClassLabel());
		ret.setNbrRooms(clazz.getNbrRooms());
		ret.setDisplayInstructors(clazz.isDisplayInstructor());
		ret.setStudentScheduling(clazz.isEnabledForStudentScheduling());
		ret.setScheduleNote(clazz.getSchedulePrintNote());
		ret.setRequestNote(clazz.getNotes());
		
        Class_ next = clazz.getNextClass(context, Right.ClassEdit); 
        ret.setNextId(next==null ? null : next.getUniqueId());
        Class_ previous = clazz.getPreviousClass(context, Right.ClassEdit); 
        ret.setPreviousId(previous == null ? null : previous.getUniqueId());
        ret.setCanClearPrefs(context.hasPermission(clazz, Right.ClassEditClearPreferences));
        
        ret.setProperties(ClassDetailBackend.getProperties(clazz, context));
        
        List<AccommodationCounter> acc = StudentAccomodation.getAccommodations(clazz);
        if (acc != null && !acc.isEmpty()) {
        	CellInterface c = ret.getProperties().addProperty(CMSG.propertyAccommodations());
        	TableInterface table = new TableInterface();
        	for (AccommodationCounter ac: acc)
        		table.addProperty(ac.getAccommodation().getName() + ":").setText(String.valueOf(ac.getCount()));
        	c.setTable(table);
        }
        
        DatePattern dp = clazz.getDatePattern();
        DatePattern edp = clazz.getSchedulingSubpart().effectiveDatePattern();
        
        ret.setSearchableDatePattern(ApplicationProperty.ClassEditSearcheableDatePattern.isTrue());
        if (dp != null)
        	ret.setDatePatternId(dp.getUniqueId());
        else if (edp != null)
        	ret.setDatePatternId(-edp.getUniqueId());
        if (edp != null)
        	ret.addDatePattern(-edp.getUniqueId(), CMSG.dropDefaultDatePattern() + " (" + edp.getName() + ")", (edp.getDatePatternType() == DatePatternType.PatternSet ? null : edp.getPatternText()));
        for (DatePattern p: DatePattern.findAll(clazz.getSessionId(),
        		context.getUser().getCurrentAuthority().hasRight(Right.ExtendedDatePatterns),
        		clazz.getManagingDept(), dp))
        	ret.addDatePattern(p.getUniqueId(), p.getName(), (p.getDatePatternType() == DatePatternType.PatternSet ? null : p.getPatternText()));
		
		fillInPreferences(ret, clazz, context);
		
		for (ClassInstructor ci: new TreeSet<ClassInstructor>(clazz.getClassInstructors())) {
			ClassInstr i = new ClassInstr();
			i.setId(ci.getUniqueId());
			i.setInstructorId(ci.getInstructor().getUniqueId());
			i.setResponsibilityId(ci.getResponsibility() == null ? null : ci.getResponsibility().getUniqueId());
			i.setPercentShare(ci.getPercentShare());
			i.setCheckConflicts(ci.isLead());
			ret.addClassInstructor(i);
		}
		NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		List<DepartmentalInstructor> instructors = Class_DAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where department.uniqueId = :deptId", DepartmentalInstructor.class
				).setParameter("deptId", clazz.getControllingDept().getUniqueId()).list();
        if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
        	Collections.sort(instructors, new DepartmentalInstructorComparator(nameFormat));
        else
        	Collections.sort(instructors, new DepartmentalInstructorComparator());
        for (DepartmentalInstructor instructor: instructors)
        	ret.addInstructor(instructor.getUniqueId(), nameFormat.format(instructor), instructor.hasPreferences() ? "1" : "0");
        for (TeachingResponsibility tr: TeachingResponsibility.getInstructorTeachingResponsibilities())
        	ret.addResponsibility(tr.getUniqueId(), tr.getLabel(), tr.getAbbreviation());
        ret.setDefaultResponsibilityId(defaultTR == null ? null : defaultTR.getUniqueId());
        
        String inheritInstructorPrefs = UserProperty.InheritInstructorPrefs.get(context.getUser());
        if (CommonValues.Never.eq(inheritInstructorPrefs))
        	ret.setInheritInstructorPrefs(InheritInstructorPrefs.NEVER);
        else if (CommonValues.Always.eq(inheritInstructorPrefs))
        	ret.setInheritInstructorPrefs(InheritInstructorPrefs.ALWAYS);
        else if (CommonValues.Ask.eq(inheritInstructorPrefs))
        	ret.setInheritInstructorPrefs(InheritInstructorPrefs.ASK);
        
        BackTracker.markForBack(
        		context,
        		"clazz?id="+clazz.getUniqueId(),
        		CMSG.backClass(clazz.getClassLabel()),
        		true, false);
		
		return ret;
	}
	
	public static void fillInPreferences(PrefGroupEditResponse response, PreferenceGroup pg, SessionContext context) {
		fillInPreferenceLevels(response, pg, context);
		fillInTimePreferences(response, pg, null, pg.effectiveDatePattern(), context, true);
		fillInRoomPreferences(response, pg, null, context, true);
		fillInDatePreferences(response, pg, null, pg.effectiveDatePattern(), context, true);
	}
	
	public static void fillInPreferenceLevels(PrefGroupEditResponse response, PreferenceGroup pg, SessionContext context) {
		List<PreferenceLevel> preferences = PreferenceLevel.getPreferenceLevelList(false);
		for (PreferenceLevel pref: preferences) {
			response.addPrefLevel(new PrefLevel(
					pref.getUniqueId(), pref.getPrefProlog(), pref.getAbbreviation(), pref.getPrefName(), pref.prefcolor(),
					PreferenceLevel.prolog2char(pref.getPrefProlog())));
		}
	}
	
	public static TimePatternModel createTimePatternModel(PreferenceGroup pg, TimePattern tp, SessionContext context) {
		boolean allowHardTime = context.hasPermission(pg, Right.CanUseHardTimePrefs);
		final TimePatternModel model = new TimePatternModel();
		model.setId(tp.getUniqueId());
		model.setName(tp.getName());
		model.setExactTime(tp.isExactTime());
		model.setDayOffset(ApplicationProperty.TimePatternFirstDayOfWeek.intValue());
		model.setAllowHard(allowHardTime);
		if (!model.isExactTime()) {
			model.setLength(5 * tp.getSlotsPerMtg() - tp.getBreakTime());
			for (TimePatternDays days: tp.getDays())
				model.addDays(days.getDayCode());
			Collections.sort(model.getDays(), new Comparator<Integer>() {
				@Override
				public int compare(Integer d1, Integer d2) {
					if (model.getDayOffset() == 0) {
						return -d1.compareTo(d2);
					} else {
						for (int i = 0; i < Constants.DAY_CODES.length; i++) {
							int idx = (i + model.getDayOffset()) % 7;
							boolean a = (d1 & Constants.DAY_CODES[idx]) != 0;
							boolean b = (d2 & Constants.DAY_CODES[idx]) != 0;
							if (a != b)
								return (a ? -1 : 1);
						}
						return 0;
					}
				}
			});
			for (TimePatternTime time: tp.getTimes())
				model.addTime(time.getStartSlot());
			Collections.sort(model.getTimes());
		}
		return model;
	}
	
	public static TimePatternModel createTimePatternModel(TimePref tp, SessionContext context) {
		TimePatternModel model = createTimePatternModel(tp.getOwner(), tp.getTimePattern(), context);
		model.setPreference(tp.getPreference());
		List<PreferenceLevel> preferences = PreferenceLevel.getPreferenceLevelList(false);
		for (PreferenceLevel pref: preferences) {
			model.addPrefLevel(new PrefLevel(
					pref.getUniqueId(), pref.getPrefProlog(), pref.getAbbreviation(), pref.getPrefName(), pref.prefcolor(),
					PreferenceLevel.prolog2char(pref.getPrefProlog())));
		}
		model.setHorizontal(!CommonValues.VerticalGrid.eq(UserProperty.GridOrientation.get(context.getUser())));
		return model;
	}
	
	public static void fillInTimePreferences(PrefGroupEditResponse response, PreferenceGroup pg, Vector<DepartmentalInstructor> leadInstructors, DatePattern datePattern, SessionContext context, boolean fillPrefs) {
		List<TimePattern> timePatterns = null;
		if (pg instanceof Class_) {
			Class_ c = (Class_)pg;
			timePatterns = TimePattern.findApplicable(
	        		context.getUser(),
	        		c.getSchedulingSubpart().getMinutesPerWk(),
	        		datePattern,
	        		c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(),
	        		true,
	        		c.getManagingDept());
		} else if (pg instanceof SchedulingSubpart) {
			SchedulingSubpart ss = (SchedulingSubpart)pg;
			timePatterns = TimePattern.findApplicable(
	        		context.getUser(),
	        		ss.getMinutesPerWk(),
	        		datePattern,
	        		ss.getInstrOfferingConfig().getDurationModel(),
	        		false,
	        		ss.getManagingDept());
		} else if (pg instanceof DepartmentalInstructor) {
			for (TimePref tp: pg.effectivePreferences(TimePref.class)) {
				response.setInstructorTimePrefereneces(tp.getPreference());
				break;
			}
			if (!response.hasInstructorTimePrefereneces())
				response.setInstructorTimePrefereneces("2");
			return;
		}
		if (timePatterns != null) {
			TimePreferences timePrefs = new TimePreferences(-5l, CMSG.propertyTime());
			timePrefs.setHorizontal(!RequiredTimeTable.getTimeGridVertical(context.getUser()));
			for (TimePattern tp: timePatterns)
				timePrefs.addItem(createTimePatternModel(pg, tp, context));
			if (fillPrefs)
				for (TimePref tp: pg.effectivePreferences(TimePref.class, leadInstructors, false)) {
					if (tp.getTimePattern() == null) continue;
					TimePatternModel m = timePrefs.getItem(tp.getTimePattern().getUniqueId());
					if (m == null) {
						m = createTimePatternModel(pg, tp.getTimePattern(), context);
						m.setValid(false);
						timePrefs.addItem(m);
					}
					if (tp.getTimePattern().isExactTime()) {
						TimeSelection selection = timePrefs.getSelection(tp.getTimePattern().getUniqueId());
						if (selection == null) {
							timePrefs.addSelection(new TimeSelection(tp.getTimePattern().getUniqueId(), tp.getPrefLevel().getUniqueId(), tp.getPreference()));	
						} else {
							selection.setPreference(selection.getPreference() + ";" + tp.getPreference());
						}
					} else {
						timePrefs.addSelection(new TimeSelection(tp.getTimePattern().getUniqueId(), tp.getPrefLevel().getUniqueId(), tp.getPreference()));
					}
				}
			response.setTimePreferences(timePrefs);
		} else {
			response.setTimePreferences(null);
		}
	}
	
	protected static void fillInDatePreferences(PrefGroupEditResponse response, PreferenceGroup pg, Vector<DepartmentalInstructor> leadInstructors, DatePattern datePattern, SessionContext context, boolean fillPrefs) {
		boolean allowHardTime = context.hasPermission(pg, Right.CanUseHardTimePrefs);
		if (datePattern != null && datePattern.getDatePatternType() == DatePatternType.PatternSet) {
			TreeSet<DatePattern> datePatterns = new TreeSet<DatePattern>(datePattern.getChildren());
			if (!datePatterns.isEmpty()) {
				Preferences datePrefs = new Preferences(PreferenceType.DATE);
				datePrefs.setAllowHard(allowHardTime);
				for (DatePattern dp: datePatterns)
					datePrefs.addItem(dp.getUniqueId(), dp.getName(), dp.getPatternString());
				if (fillPrefs)
					for (DatePatternPref dp: pg.effectivePreferences(DatePatternPref.class, leadInstructors)) {
						Selection selection = new Selection(dp.getDatePattern().getUniqueId(), dp.getPrefLevel().getUniqueId());
						datePrefs.addSelection(selection);
					}
				response.setDatePreferences(datePrefs);
			} else {
				response.setDatePreferences(null);
			}
		} else {
			response.setDatePreferences(null);
		}
	}
	
	protected static void fillInRoomPreferences(PrefGroupEditResponse response, PreferenceGroup pg, Vector<DepartmentalInstructor> leadInstructors, SessionContext context, boolean fillPrefs) {
		boolean allowHardRoom = context.hasPermission(pg, Right.CanUseHardRoomPrefs);
		Set<Location> rooms = pg.getAvailableRooms();
		if (response.getRoomPreferences() != null)
			response.getRoomPreferences().clear();
		if (!rooms.isEmpty()) {
			Preferences roomPrefs = new Preferences(PreferenceType.ROOM);
			roomPrefs.setAllowHard(allowHardRoom);
			for (Location location: rooms)
				roomPrefs.addItem(location.getUniqueId(), location.getLabelWithCapacity(), location.getDisplayName());
			if (fillPrefs)
				for (RoomPref rp: pg.effectivePreferences(RoomPref.class, leadInstructors)) {
					Selection selection = new Selection(rp.getRoom().getUniqueId(), rp.getPrefLevel().getUniqueId());
					if (response.hasMultipleRooms())
						selection.setRoomIndex(rp.getRoomIndex());
					roomPrefs.addSelection(selection);
				}
			response.addRoomPreference(roomPrefs);
		}
		Set<Building> buildings = pg.getAvailableBuildings();
		if (!buildings.isEmpty() && response.getNbrRooms() != null && response.getNbrRooms() > 0) {
			Preferences bldgPrefs = new Preferences(PreferenceType.BUILDING);
			bldgPrefs.setAllowHard(allowHardRoom);
			for (Building building: buildings)
				bldgPrefs.addItem(building.getUniqueId(), building.getAbbrName(), null);
			if (fillPrefs)
				for (BuildingPref bp: pg.effectivePreferences(BuildingPref.class, leadInstructors)) {
					Selection selection = new Selection(bp.getBuilding().getUniqueId(), bp.getPrefLevel().getUniqueId());
					if (response.hasMultipleRooms())
						selection.setRoomIndex(bp.getRoomIndex());
					bldgPrefs.addSelection(selection);
				}
			response.addRoomPreference(bldgPrefs);
		}
		Set<RoomFeature> roomFeatures = pg.getAvailableRoomFeatures();
		if (!roomFeatures.isEmpty() && response.getNbrRooms() != null && response.getNbrRooms() > 0) {
			Preferences rfPrefs = new Preferences(PreferenceType.ROOM_FEATURE);
			rfPrefs.setAllowHard(allowHardRoom);
			for (RoomFeature roomFeature: roomFeatures)
				rfPrefs.addItem(roomFeature.getUniqueId(), roomFeature.getLabelWithType(), roomFeature.getDescription());
			if (fillPrefs)
				for (RoomFeaturePref fp: pg.effectivePreferences(RoomFeaturePref.class, leadInstructors)) {
					Selection selection = new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId());
					if (response.hasMultipleRooms())
						selection.setRoomIndex(fp.getRoomIndex());
					rfPrefs.addSelection(selection);
				}
			response.addRoomPreference(rfPrefs);
		}
		Set<RoomGroup> roomGroups = pg.getAvailableRoomGroups();
		if (!roomGroups.isEmpty() && response.getNbrRooms() != null && response.getNbrRooms() > 0) {
			Preferences groupPrefs = new Preferences(PreferenceType.ROOM_GROUP);
			groupPrefs.setAllowHard(allowHardRoom);
			for (RoomGroup group: roomGroups)
				groupPrefs.addItem(group.getUniqueId(), group.getNameWithTitle(), group.getDescription());
			if (fillPrefs)
				for (RoomGroupPref gp: pg.effectivePreferences(RoomGroupPref.class, leadInstructors)) {
					Selection selection = new Selection(gp.getRoomGroup().getUniqueId(), gp.getPrefLevel().getUniqueId());
					if (response.hasMultipleRooms())
						selection.setRoomIndex(gp.getRoomIndex());
					groupPrefs.addSelection(selection);
				}
			response.addRoomPreference(groupPrefs);
		}		
	}
	
	public static void doClear(Set<Preference> s, Preference.Type... typesArray) {
    	int types = Preference.Type.toInt(typesArray);
    	for (Iterator<Preference> i = s.iterator(); i.hasNext(); ) {
    		Preference p = i.next();
    		if (p.getType().in(types)) i.remove();
    	}
    }
	
	public static void doUpdate(PreferenceGroup pg, Set<Preference> s, PrefGroupEditResponse form, Preference.Type... typesArray) throws Exception {
    	pg.setPreferences(s);
    	
    	int types = Preference.Type.toInt(typesArray);
    	for (Iterator<Preference> i = s.iterator(); i.hasNext(); ) {
    		Preference p = i.next();
    		if (p.getType().in(types)) i.remove();
    	}

        // Time Prefs
    	if (Preference.Type.TIME.in(types)) {
            if (pg instanceof DepartmentalInstructor) {
            	if (form.hasInstructorTimePrefereneces() && !form.getInstructorTimePrefereneces().matches("2+")) {
            		TimePref tp = new TimePref();
            		tp.setOwner(pg);
            		tp.setPreference(form.getInstructorTimePrefereneces());
            		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            		tp.setTimePattern(null);
            		s.add(tp);
            	}
            } else {
            	Set<TimePref> parentTimePrefs = pg.effectivePreferences(TimePref.class, false);
            	if (form.hasTimePreferences() && form.getTimePreferences().hasSelections()) {
            		for (TimeSelection selection: form.getTimePreferences().getSelections()) {
            			TimePattern timePattern = TimePatternDAO.getInstance().get(selection.getItem());
            			if (timePattern == null) continue;
            			if (timePattern.isExactTime()) {
            				for (String preference: selection.getPreference().split(";")) {
            					if (preference.isEmpty()) continue;
                    			TimePref tp = new TimePref();
                    			tp.setOwner(pg);
                    			tp.setPreference(preference);
                    			tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                    			tp.setTimePattern(TimePatternDAO.getInstance().get(selection.getItem()));
                    			s.add(tp);
            				}
            			} else {
                			TimePref tp = new TimePref();
                			tp.setOwner(pg);
                			tp.setPreference(selection.getPreference());
                			tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                			tp.setTimePattern(TimePatternDAO.getInstance().get(selection.getItem()));
                			TimePref sameParentTimePref = null;
                			if (parentTimePrefs!=null && !parentTimePrefs.isEmpty()) {
                				for (Iterator<TimePref> i = parentTimePrefs.iterator();i.hasNext();) {
                					TimePref parentTimePref = i.next();
                					if (parentTimePref.isSame(tp, pg)) {
                						if (parentTimePref.getPreference().equals(tp.getPreference()) && parentTimePref.getPrefLevel().equals(tp.getPrefLevel()))
                							sameParentTimePref = parentTimePref; 
                						i.remove(); break;
                					}
                				}
                			}
                			if (sameParentTimePref == null)
                				s.add(tp);
            			}
            		}
            	}
                if (parentTimePrefs!=null && !parentTimePrefs.isEmpty()) {
                	for (Iterator<TimePref> i=parentTimePrefs.iterator();i.hasNext();) {
                		TimePref tp = (TimePref)i.next().clone();
                		tp.setOwner(pg);
                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
                		s.add(tp);
                	}
                }
            }
    	}
            
        // Room Prefs
    	if (Preference.Type.ROOM.in(types)) {
            Set<RoomPref> parentRoomPrefs = pg.effectivePreferences(RoomPref.class);
            
            Preferences prefs = form.getRoomPreference(PreferenceType.ROOM);
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
                    RoomPref rp = new RoomPref();
                    rp.setOwner(pg);
                    rp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    rp.setRoom(LocationDAO.getInstance().get(selection.getItem()));
                    rp.setRoomIndex(selection.getRoomIndex());
                    
                    RoomPref sameParentRp = null;
                    for (Iterator<RoomPref> j=parentRoomPrefs.iterator();j.hasNext();) {
                    	RoomPref p = j.next();
                    	if (p.isSame(rp, pg)) {
                    		if (p.getPrefLevel().equals(rp.getPrefLevel())) sameParentRp = rp;
                    		j.remove();
                    		break;
                    	}
                    }
                    if (sameParentRp==null) s.add(rp);
            	}
            
            if (parentRoomPrefs!=null && !parentRoomPrefs.isEmpty()) {
            	for (Iterator<RoomPref> i=parentRoomPrefs.iterator();i.hasNext();) {
            		RoomPref rp = (RoomPref)i.next().clone();
            		rp.setOwner(pg);
            		rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(rp);
            	}
            }
    	}
        
        // Bldg Prefs
    	if (Preference.Type.BUILDING.in(types)) {
            Set<BuildingPref> parentBuildingPrefs = pg.effectivePreferences(BuildingPref.class);
            
            Preferences prefs = form.getRoomPreference(PreferenceType.BUILDING);
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		BuildingPref bp = new BuildingPref();
                    bp.setOwner(pg);
                    bp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    bp.setBuilding(BuildingDAO.getInstance().get(selection.getItem()));
                    bp.setRoomIndex(selection.getRoomIndex());
                    
                    BuildingPref sameParentRp = null;
                    for (Iterator<BuildingPref> j=parentBuildingPrefs.iterator();j.hasNext();) {
                    	BuildingPref p = j.next();
                    	if (p.isSame(bp, pg)) {
                    		if (p.getPrefLevel().equals(bp.getPrefLevel())) sameParentRp = bp;
                    		j.remove();
                    		break;
                    	}
                    }
                    if (sameParentRp==null) s.add(bp);
            	}
            
            if (parentBuildingPrefs!=null && !parentBuildingPrefs.isEmpty()) {
            	for (Iterator<BuildingPref> i=parentBuildingPrefs.iterator();i.hasNext();) {
            		BuildingPref bp = (BuildingPref)i.next().clone();
            		bp.setOwner(pg);
            		bp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(bp);
            	}
            }
    	}
    	
        // Room Feature Prefs
    	if (Preference.Type.ROOM_FEATURE.in(types)) {
            Set<RoomFeaturePref> parentRoomFeaturePrefs = pg.effectivePreferences(RoomFeaturePref.class);
            
            Preferences prefs = form.getRoomPreference(PreferenceType.ROOM_FEATURE);
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		RoomFeaturePref fp = new RoomFeaturePref();
                    fp.setOwner(pg);
                    fp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    fp.setRoomFeature(RoomFeatureDAO.getInstance().get(selection.getItem()));
                    fp.setRoomIndex(selection.getRoomIndex());
                    
                    RoomFeaturePref sameParentFp = null;
                    for (Iterator<RoomFeaturePref> j=parentRoomFeaturePrefs.iterator();j.hasNext();) {
                    	RoomFeaturePref p = j.next();
                    	if (p.isSame(fp, pg)) {
                    		if (p.getPrefLevel().equals(fp.getPrefLevel())) sameParentFp = fp;
                    		j.remove();
                    		break;
                    	}
                    }
                    if (sameParentFp==null) s.add(fp);
            	}
            
            if (parentRoomFeaturePrefs!=null && !parentRoomFeaturePrefs.isEmpty()) {
            	for (Iterator<RoomFeaturePref> i=parentRoomFeaturePrefs.iterator();i.hasNext();) {
            		RoomFeaturePref fp = (RoomFeaturePref)i.next().clone();
            		fp.setOwner(pg);
            		fp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(fp);
            	}
            }
    	}
    	
        // Room Feature Prefs
    	if (Preference.Type.ROOM_GROUP.in(types)) {
            Set<RoomGroupPref> parentRoomGroupPrefs = pg.effectivePreferences(RoomGroupPref.class);
            
            Preferences prefs = form.getRoomPreference(PreferenceType.ROOM_GROUP);
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		RoomGroupPref gp = new RoomGroupPref();
                    gp.setOwner(pg);
                    gp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    gp.setRoomGroup(RoomGroupDAO.getInstance().get(selection.getItem()));
                    gp.setRoomIndex(selection.getRoomIndex());
                    
                    RoomGroupPref sameParentGp = null;
                    for (Iterator<RoomGroupPref> j=parentRoomGroupPrefs.iterator();j.hasNext();) {
                    	RoomGroupPref p = j.next();
                    	if (p.isSame(gp, pg)) {
                    		if (p.getPrefLevel().equals(gp.getPrefLevel())) sameParentGp = gp;
                    		j.remove();
                    		break;
                    	}
                    }
                    if (sameParentGp==null) s.add(gp);
            	}
            
            if (parentRoomGroupPrefs!=null && !parentRoomGroupPrefs.isEmpty()) {
            	for (Iterator<RoomGroupPref> i=parentRoomGroupPrefs.iterator();i.hasNext();) {
            		RoomGroupPref gp = (RoomGroupPref)i.next().clone();
            		gp.setOwner(pg);
            		gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(gp);
            	}
            }
    	}
  
        // Date pattern Prefs
    	if (Preference.Type.DATE.in(types)) {
    		Set<DatePatternPref> parentDatePatPrefs = pg.effectivePreferences(DatePatternPref.class);
            
            Preferences prefs = form.getDatePreferences();
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		DatePatternPref dp = new DatePatternPref();
                    dp.setOwner(pg);
                    dp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    dp.setDatePattern(DatePatternDAO.getInstance().get(selection.getItem()));
                    
                    DatePatternPref sameParentDp = null;
                    for (Iterator<DatePatternPref> j=parentDatePatPrefs.iterator();j.hasNext();) {
                    	DatePatternPref p = j.next();
                    	if (p.isSame(dp, pg)) {
                    		if (p.getPrefLevel().equals(dp.getPrefLevel())) sameParentDp = dp;
                    		j.remove();
                    		break;
                    	}
                    }
                    if (sameParentDp==null) s.add(dp);
            	}
            
            if (parentDatePatPrefs!=null && !parentDatePatPrefs.isEmpty()) {
            	for (Iterator<DatePatternPref> i=parentDatePatPrefs.iterator();i.hasNext();) {
            		DatePatternPref dp = (DatePatternPref)i.next().clone();
            		if(!pg.effectiveDatePattern().findChildren().contains(dp.getDatePattern()))
            			continue;
            		dp.setOwner(pg);
            		dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(dp);
            	}
            }
    	}
    	
        // Distribution Prefs
    	if (Preference.Type.DISTRIBUTION.in(types)) {
            Preferences prefs = form.getDistributionPreferences();
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		DistributionPref dp = new DistributionPref();
                    dp.setOwner(pg);
                    dp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    dp.setDistributionType(DistributionTypeDAO.getInstance().get(selection.getItem()));
                    s.add(dp);
            	}
    	}
    	
    	// Course Prefs
    	if (Preference.Type.COURSE.in(types)) {
            Preferences prefs = form.getCoursePreferences();
            if (prefs != null && prefs.hasSelections())
            	for (Selection selection: prefs.getSelections()) {
            		InstructorCoursePref dp = new InstructorCoursePref();
                    dp.setOwner(pg);
                    dp.setPrefLevel(PreferenceLevelDAO.getInstance().get(selection.getLevel()));
                    dp.setCourse(CourseOfferingDAO.getInstance().get(selection.getItem()));
                    s.add(dp);
            	}
    	}

        // Set values in subpart
        pg.setPreferences(s);
    }
}
