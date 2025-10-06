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
package org.unitime.timetable.server.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamEditRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamEditResponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupClasses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupCourses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupSubparts;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamObjectInterface;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefGroupEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PreferenceType;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Preferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassEditBackend;
import org.unitime.timetable.server.rooms.PeriodPreferencesBackend;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(ExamEditRequest.class)
public class ExamEditBackend implements GwtRpcImplementation<ExamEditRequest, ExamEditResponse> {
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public ExamEditResponse execute(ExamEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
		Exam exam = (request.getId() == null ? null : ExamDAO.getInstance().get(request.getId()));
		if (exam == null)
			context.checkPermission(Right.ExaminationAdd);
		else
			context.checkPermission(exam, Right.ExaminationEdit);

		if (request.getOperation() != null) {
			ExamEditResponse ret;
			switch (request.getOperation()) {
			case CLEAR_EXAM_PREFS:
				context.checkPermission(exam, Right.ExaminationEditClearPreferences);

				ClassEditBackend.doClear(exam.getPreferences(),
						Preference.Type.PERIOD, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
				hibSession.merge(exam);
				hibSession.flush();

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    exam,
	                    ChangeLog.Source.EXAM_EDIT,
	                    ChangeLog.Operation.CLEAR_PREF,
	                    exam.firstSubjectArea(), 
	                    exam.firstDepartment());
				
				ret = new ExamEditResponse();
				ret.setUrl("examination?id=" + exam.getUniqueId());
				return ret;
			case UPDATE:
			case NEXT:
			case PREVIOUS:
				Transaction tx = hibSession.beginTransaction();
				try {
					ExamEditResponse data = request.getPayLoad();
					if (exam == null) {
						exam = new Exam();
						exam.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
						exam.setExamType(ExamTypeDAO.getInstance().get(data.getExamTypeId(), hibSession));
						exam.setInstructors(new HashSet<DepartmentalInstructor>());
						exam.setPreferences(new HashSet<Preference>());
						exam.setOwners(new HashSet<ExamOwner>());
					}
					exam.setName(data.getName());
					exam.setSeatingType(data.isExamSeating() ? Exam.sSeatingTypeExam : Exam.sSeatingTypeNormal);
					exam.setLength(data.getLength());
					exam.setMaxNbrRooms(data.getMaxRooms() == null ? 0 : data.getMaxRooms());
					exam.setPrintOffset(data.getPrintOffset());
					exam.setNote(data.getNotes());
					exam.setExamSize(data.getSize());
					
					if (request.getId() == null)
						hibSession.persist(exam);
					
					List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(exam.getInstructors());
					if (data.hasExamInstructors()) {
						instr: for (IdLabel instr: data.getExamInstructors()) {
							if (instr == null || instr.getId() == null) continue;
							for (Iterator<DepartmentalInstructor> i = instructors.iterator(); i.hasNext(); ) {
								DepartmentalInstructor deptInstr = i.next();
								if (deptInstr.getUniqueId().equals(instr.getId())) {
									i.remove();
									continue instr;
								}
							}
							DepartmentalInstructor deptInstr = DepartmentalInstructorDAO.getInstance().get(instr.getId());
							if (deptInstr != null) {
								exam.getInstructors().add(deptInstr);
								deptInstr.getExams().add(exam);
							}
						}
					}
					for (DepartmentalInstructor deptInstr: instructors) {
						deptInstr.getExams().remove(exam);
						exam.getInstructors().remove(deptInstr);
					}
					
					exam.getOwners().clear();
					if (data.hasExamObjects()) {
						for (ExamObjectInterface eo: data.getExamObjects()) {
							if (eo.getCourseId() == null || !eo.isValid()) continue;
							CourseOffering course = CourseOfferingDAO.getInstance().get(eo.getCourseId(), hibSession);
							if (course == null) continue;
							ExamOwner owner = new ExamOwner();
							if (eo.getSubpartId() == Long.MIN_VALUE) { //course
					            owner.setOwner(course);
							} else if (eo.getSubpartId() == Long.MIN_VALUE + 1) { //offering
					            owner.setOwner(course.getInstructionalOffering());
					        } else if (eo.getSubpartId() < 0) { //config
					            InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(-eo.getSubpartId());
					            if (config==null) continue;
					            owner.setOwner(config);
					        } else if (eo.getClassId() != null) { //class
					            Class_ clazz = Class_DAO.getInstance().get(eo.getClassId());
					            if (clazz==null) continue;
					            owner.setOwner(clazz);
					        } else {
					        	continue;
					        }
			                owner.setExam(exam);
							exam.getOwners().add(owner);
						}
					}
			        
					ClassEditBackend.doUpdate(exam, exam.getPreferences(), data,
							Preference.Type.PERIOD, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING);
					
					hibSession.merge(exam);
			        
		            ChangeLog.addChange(
		                    null,
		                    context,
		                    exam,
		                    ChangeLog.Source.EXAM_EDIT,
		                    (request.getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
		                    exam.firstSubjectArea(), 
		                    exam.firstDepartment());

                	tx.commit();
					tx = null;
				} catch (Exception e) {
					if (tx != null) { tx.rollback(); }
					throw new GwtRpcException(e.getMessage(), e);
				}
				
				ret = new ExamEditResponse();
				if (request.getOperation() == Operation.PREVIOUS && request.getPayLoad().getPreviousId() != null)
					ret.setUrl("examEdit?id=" + request.getPayLoad().getPreviousId());
				else if (request.getOperation() == Operation.NEXT  && request.getPayLoad().getNextId() != null)
					ret.setUrl("examEdit?id=" + request.getPayLoad().getNextId());
				else
					ret.setUrl("examination?id=" + exam.getUniqueId());
				return ret;
			case EXAM_TYPE: // exam type change -- update preferences
				ret = request.getPayLoad();
				ExamType examType = ExamTypeDAO.getInstance().get(ret.getExamTypeId());
				fillInPreferences(ret, null, examType, ret.isExamSeating(), context);
				return ret;
			case EXAM_SEATING:
				ret = request.getPayLoad();
				Preferences roomPrefs = ret.getRoomPreference(PreferenceType.ROOM);
				if (roomPrefs != null) {
					Set<Location> rooms = Location.findAllExamLocations(context.getUser().getCurrentAcademicSessionId(), ExamTypeDAO.getInstance().get(ret.getExamTypeId()));
					if (!rooms.isEmpty()) {
						roomPrefs.getItems().clear();
						for (Location location: rooms) {
							roomPrefs.addItem(location.getUniqueId(), ret.isExamSeating() ? location.getLabelWithExamCapacity() : location.getLabelWithCapacity(), location.getDisplayName());
						}
					}
				}
				return ret;
			case EXAM_OWNERS: // exam owners changes -- update instructors
				ret = request.getPayLoad();
				Set<DepartmentalInstructor> instructors = null;
				NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		        if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
		        	instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator(nameFormat));
		        else
		        	instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator());
		        if (ret.hasExamObjects()) {
		        	Set<Long> deptIds = new HashSet<Long>();
					for (ExamObjectInterface eo: ret.getExamObjects()) {
						if (eo.getCourseId() == null) continue;
						CourseOffering course = CourseOfferingDAO.getInstance().get(eo.getCourseId(), hibSession);
						if (deptIds.add(course.getSubjectArea().getDepartment().getUniqueId()))
			            	instructors.addAll(hibSession.createQuery(
			            			"from DepartmentalInstructor where department.uniqueId = :deptId", DepartmentalInstructor.class
			            			).setParameter("deptId", course.getSubjectArea().getDepartment().getUniqueId()).list());
					}
					if (ret.hasInstructors()) ret.getInstructors().clear();
					for (DepartmentalInstructor instr: instructors)
			        	ret.addInstructor(instr.getUniqueId(), nameFormat.format(instr), instr.getDepartment().getDeptCode());
					if (ret.hasExamInstructors()) {
						for (Iterator<IdLabel> it = ret.getExamInstructors().iterator(); it.hasNext();) {
							if (ret.getInstructor(it.next().getId()) == null)
								it.remove();
						}
					}
		        }
				return ret;
			default:
				break;
			}
		}
		
		ExamEditResponse ret = new ExamEditResponse();
		ExamType examType = (exam == null ? null : exam.getExamType());
		NameFormat nameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		if (examType == null) {
	        Object et = context.getAttribute(SessionAttribute.ExamType);
	        if (et != null)
	        	examType = ExamTypeDAO.getInstance().get(Long.valueOf(et.toString()));
		}
		if (examType == null) {
			for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamEdit)) {
				examType = type;
				break;
			}
		}
		
		if (exam != null) {
			ret.setId(request.getId());
			ret.setLabel(exam.getLabel());
			ret.setName(exam.getName() == null || exam.getName().isEmpty() ? null : exam.getName());
			ret.setExamSeating(exam.getSeatingType() == Exam.sSeatingTypeExam);
			ret.setSize(exam.getExamSize());
			ret.setSizeUseLimitInsteadOfEnrollment(ApplicationProperty.ExaminationSizeUseLimitInsteadOfEnrollment.isTrue(examType.getReference(), examType.getType() != ExamType.sExamTypeFinal));
			ret.setMaxRooms(exam.getMaxNbrRooms());
			ret.setPrintOffset(exam.getPrintOffset());
			ret.setNotes(exam.getNote());
			ret.setLength(exam.getLength());
			
	        for (DepartmentalInstructor instr: new TreeSet<DepartmentalInstructor>(exam.getInstructors()))
	        	ret.addExamInstructor(new IdLabel(instr.getUniqueId(), nameFormat.format(instr), instr.getDepartment().getDeptCode()));
	        
			Set<DepartmentalInstructor> instructors = null;
	        if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
	        	instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator(nameFormat));
	        else
	        	instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator());
	        
	        Set<Long> deptIds = new HashSet<Long>();
	        for (ExamOwner owner: new TreeSet<ExamOwner>(exam.getOwners())) {
	        	ExamObjectInterface obj = new ExamObjectInterface();
	        	obj.setSubject(owner.getCourse().getSubjectAreaAbbv());
	        	obj.setSubjectId(owner.getCourse().getSubjectArea().getUniqueId());
	        	obj.setCourse(owner.getCourse().getCourseNumberWithTitle());
	        	obj.setCourseId(owner.getCourse().getUniqueId());
	        	switch (owner.getOwnerType()) {
	        	case ExamOwner.sOwnerTypeOffering:
	        		obj.setSubpartId(Long.MIN_VALUE + 1);
	        		obj.setSubpart(MSG.examTypeOffering());
	        		break;
	        	case ExamOwner.sOwnerTypeCourse:
	        		obj.setSubpartId(Long.MIN_VALUE);
	        		obj.setSubpart(MSG.examTypeCourse());
	        		break;
	        	case ExamOwner.sOwnerTypeConfig:
	        		InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(owner.getOwnerId());
	        		if (config == null) continue;
	        		obj.setSubpartId(-config.getUniqueId());
	        		obj.setSubpart(getConfigLabel(config));
	        		break;
	        	case ExamOwner.sOwnerTypeClass:
	        		Class_ clazz = Class_DAO.getInstance().get(owner.getOwnerId());
	        		if (clazz == null) continue;
	        		obj.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
	        		obj.setSubpart(getSubpartLabel(clazz.getSchedulingSubpart()));
	        		obj.setClassId(clazz.getUniqueId());
	        		obj.setClazz(getClassLabel(clazz, owner.getCourse()));
	        		break;
	        	}
	        	ret.addExamObject(obj);
	        	if (deptIds.add(owner.getCourse().getSubjectArea().getDepartment().getUniqueId()))
	            	instructors.addAll(hibSession.createQuery(
	            			"from DepartmentalInstructor where department.uniqueId = :deptId", DepartmentalInstructor.class
	            			).setParameter("deptId", owner.getCourse().getSubjectArea().getDepartment().getUniqueId()).list());
	        }
	        
	        for (DepartmentalInstructor instr: instructors)
	        	ret.addInstructor(instr.getUniqueId(), nameFormat.format(instr), instr.getDepartment().getDeptCode());
		} else {
			ret.setExamSeating(true);
			if (examType != null) {
				TreeSet<ExamPeriod>  periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), examType);
				if (!periods.isEmpty())
					ret.setLength(Constants.SLOT_LENGTH_MIN * periods.first().getLength());
			}
            SolverParameterDef maxRoomsParam = SolverParameterDef.findByNameType("Exams.MaxRooms", SolverParameterGroup.SolverType.EXAM);
            if (maxRoomsParam != null && maxRoomsParam.getDefault() != null) 
                ret.setMaxRooms(Integer.valueOf(maxRoomsParam.getDefault()));
			
			if (request.getFirstType() != null && request.getFirstId() != null) {
				String firstType = request.getFirstType();
				Long firstId = Long.valueOf(request.getFirstId());
				CourseOffering course = null;
	            ExamObjectInterface obj = new ExamObjectInterface();
	            if ("Class_".equals(firstType)) {
	                Class_ clazz = new Class_DAO().get(firstId);
	                course = clazz.getSchedulingSubpart().getControllingCourseOffering();
	                obj.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
	        		obj.setSubpart(getSubpartLabel(clazz.getSchedulingSubpart()));
	        		obj.setClassId(clazz.getUniqueId());
	        		obj.setClazz(getClassLabel(clazz, course));
	            } else if ("SchedulingSubpart".equals(firstType)) {
	                SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(firstId);
	                InstrOfferingConfig config = subpart.getInstrOfferingConfig();
	                course = config.getControllingCourseOffering();
	                obj.setSubpartId(subpart.getUniqueId());
	        		obj.setSubpart(getSubpartLabel(subpart));
	            } else if ("InstrOfferingConfig".equals(firstType)) {
	                InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(firstId);
	                course = config.getControllingCourseOffering();
	                obj.setSubpartId(-config.getUniqueId());
	        		obj.setSubpart(getConfigLabel(config));
	            } else if ("InstructionalOffering".equals(firstType)) {
	            	InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(firstId);
	            	course = offering.getControllingCourseOffering();
	            	obj.setSubpartId(Long.MIN_VALUE + 1);
	            	obj.setSubpart(MSG.examTypeOffering());
	            } else if ("CourseOffering".equals(firstType)) {
	                course = CourseOfferingDAO.getInstance().get(firstId);
	                obj.setSubpartId(Long.MIN_VALUE);
	            	obj.setSubpart(MSG.examTypeCourse());
	            }
	            if (course != null) {
	            	obj.setSubject(course.getSubjectAreaAbbv());
	            	obj.setSubjectId(course.getSubjectArea().getUniqueId());
	            	obj.setCourse(course.getCourseNumberWithTitle());
	            	obj.setCourseId(course.getUniqueId());
	            	ret.addExamObject(obj);

					Set<DepartmentalInstructor> instructors = null;
					if (ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
						instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator(nameFormat));
					else
						instructors = new TreeSet<DepartmentalInstructor>(new DepartmentalInstructorComparator());

					instructors.addAll(hibSession.createQuery(
							"from DepartmentalInstructor where department.uniqueId = :deptId", DepartmentalInstructor.class)
							.setParameter("deptId", course.getSubjectArea().getDepartment().getUniqueId()).list());

					for (DepartmentalInstructor instr : instructors)
						ret.addInstructor(instr.getUniqueId(), nameFormat.format(instr), instr.getDepartment().getDeptCode());
	            }
	        }
			
		
		}
        
        if (examType != null) {
			ret.setExamType(new IdLabel(examType.getUniqueId(), examType.getLabel(), examType.getReference()));
			if (exam == null) {
				ret.setPeriodPreferences(new PeriodPreferencesBackend().loadExamPeriodPreferences(
    				examinationSolverService.getSolver(),
        			exam,
        			examType,
        			context));
				for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamEdit))
					ret.addExamType(type.getUniqueId(), type.getLabel(), type.getReference());
			}
        }
        
        fillInPreferences(ret, exam, examType, ret.isExamSeating(), context);
        
        if (request.getOperation() == Operation.CLONE_EXAM) {
        	ret.setId(null);
        	if (ret.hasPeriodPreferences())
        		ret.getPeriodPreferences().setPattern("");
        }
        
		
        if (exam != null && ret.getId() != null) {
            BackTracker.markForBack(
            		context,
            		"examination?id="+exam.getUniqueId(),
            		MSG.backExam(exam.getLabel()),
            		true, false);
            Long nextId = Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
            if (nextId != null && context.hasPermission(nextId, Right.ExaminationEdit))
            	ret.setNextId(nextId);
            Long prevId = Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
            if (prevId != null && context.hasPermission(prevId, Right.ExaminationEdit))
            	ret.setPreviousId(prevId);
        }
        
		for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser(), true))
			ret.addSubject(subject.getUniqueId(), subject.getSubjectAreaAbbreviation(), subject.getLabel());

		BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		ret.setBackTitle(back.getTitle());
    		if (exam != null)
    			ret.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + exam.getUniqueId() + "&backType=PreferenceGroup");
    		else
    			ret.setBackUrl(back.getUrl());
    	}
			
		return ret;
	}
	
	public void fillInPreferences(PrefGroupEditResponse response, Exam pg, ExamType et, boolean examSeating, SessionContext context) {
		if (pg != null) {
			response.setNbrRooms(1);
			ClassEditBackend.fillInPreferences(response, pg, context, examSeating);
			RoomInterface.PeriodPreferenceModel model = new PeriodPreferencesBackend().loadExamPeriodPreferences(
					examinationSolverService.getSolver(),
	    			pg,
	    			pg.getExamType(),
	    			context);
				response.setPeriodPreferences(model);
		} else if (et != null) {
			ClassEditBackend.fillInPreferenceLevels(response, pg, context);
			Preferences roomPrefs = response.getRoomPreference(PreferenceType.ROOM);
			if (roomPrefs == null) {
				roomPrefs = new Preferences(PreferenceType.ROOM);
				response.addRoomPreference(roomPrefs);
				roomPrefs.setAllowHard(true);
			} else if (roomPrefs.hasItems()) {
				roomPrefs.getItems().clear();
			}
			Preferences bldgPrefs = response.getRoomPreference(PreferenceType.BUILDING);
			if (bldgPrefs == null) {
				bldgPrefs = new Preferences(PreferenceType.BUILDING);
				bldgPrefs.setAllowHard(true);
				response.addRoomPreference(bldgPrefs);
			} else if (bldgPrefs.hasItems()) {
				bldgPrefs.getItems().clear();
			}
			Preferences rfPrefs = response.getRoomPreference(PreferenceType.ROOM_FEATURE);
			if (rfPrefs == null) {
				rfPrefs = new Preferences(PreferenceType.ROOM_FEATURE);
				rfPrefs.setAllowHard(true);
				response.addRoomPreference(rfPrefs);
			} else if (rfPrefs.hasItems()) {
				rfPrefs.getItems().clear();
			}
			Preferences groupPrefs = response.getRoomPreference(PreferenceType.ROOM_GROUP);
			if (groupPrefs == null) {
				groupPrefs = new Preferences(PreferenceType.ROOM_GROUP);
				groupPrefs.setAllowHard(true);
				response.addRoomPreference(groupPrefs);
			} else if (groupPrefs.hasItems()) {
				groupPrefs.getItems().clear();
			}
			
			Set<Location> rooms = Location.findAllExamLocations(context.getUser().getCurrentAcademicSessionId(), et);
			TreeSet<Building> buildings = new TreeSet<Building>();
			if (!rooms.isEmpty()) {
				for (Location location: rooms) {
					roomPrefs.addItem(location.getUniqueId(), examSeating ? location.getLabelWithExamCapacity() : location.getLabelWithCapacity(), location.getDisplayName());
					if (location instanceof Room)
						buildings.add(((Room)location).getBuilding());
				}
			}
			if (!buildings.isEmpty()) {
				for (Building building: buildings)
					bldgPrefs.addItem(building.getUniqueId(), building.getAbbrName(), null);
			}
			
			List<GlobalRoomFeature> roomFeatures = RoomFeature.getAllGlobalRoomFeatures(context.getUser().getCurrentAcademicSessionId());
			if (!roomFeatures.isEmpty()) {
				for (RoomFeature roomFeature: roomFeatures)
					rfPrefs.addItem(roomFeature.getUniqueId(), roomFeature.getLabelWithType(), roomFeature.getDescription());
			}
			List<RoomGroup> roomGroups = RoomGroup.getAllGlobalRoomGroups(context.getUser().getCurrentAcademicSessionId());
			if (!roomGroups.isEmpty()) {
				for (RoomGroup group: roomGroups)
					groupPrefs.addItem(group.getUniqueId(), group.getNameWithTitle(), group.getDescription());
			}
			
			if (roomPrefs.hasSelections())
				for (Iterator<Selection> it = roomPrefs.getSelections().iterator(); it.hasNext(); )
					if (roomPrefs.getItem(it.next().getItem()) == null) it.remove();
			if (bldgPrefs.hasSelections())
				for (Iterator<Selection> it = bldgPrefs.getSelections().iterator(); it.hasNext(); )
					if (bldgPrefs.getItem(it.next().getItem()) == null) it.remove();
			if (rfPrefs.hasSelections())
				for (Iterator<Selection> it = rfPrefs.getSelections().iterator(); it.hasNext(); )
					if (rfPrefs.getItem(it.next().getItem()) == null) it.remove();
			if (groupPrefs.hasSelections())
				for (Iterator<Selection> it = groupPrefs.getSelections().iterator(); it.hasNext(); )
					if (groupPrefs.getItem(it.next().getItem()) == null) it.remove();

			RoomInterface.PeriodPreferenceModel model = new PeriodPreferencesBackend().loadExamPeriodPreferences(
					null,
	    			null,
	    			et,
	    			context);
				response.setPeriodPreferences(model);			
		}
	}
	
	protected static String getConfigLabel(InstrOfferingConfig c) {
		return c.getName() + (c.getInstructionalMethod() == null ? "" : " (" + c.getInstructionalMethod().getLabel() + ")");
	}
	
	protected static String getSubpartLabel(SchedulingSubpart s) {
        String name = s.getItype().getAbbv();
        while (s.getParentSubpart()!=null) {
            name = "\u00A0\u00A0"+name;
            s = s.getParentSubpart();
        }
        if (s.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
            name += " ["+s.getInstrOfferingConfig().getName()+"]";
        return name;
	}
	
	protected static String getClassLabel(Class_ c, CourseOffering co) {
		String extId = c.getClassSuffix(co);
        return c.getSectionNumberString() + (extId == null || extId.isEmpty() || extId.equalsIgnoreCase(c.getSectionNumberString()) ? "" : " - " + extId);
	}
	
	@GwtRpcImplements(ExamLookupCourses.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupCoursesBackend implements GwtRpcImplementation<ExamLookupCourses, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(ExamLookupCourses request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
	        List<Object[]> courseNumbers = CourseOfferingDAO.getInstance().
	                getSession().
	                createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
	                        "where co.subjectArea.uniqueId = :subjectAreaId "+
	                        "and co.instructionalOffering.notOffered = false " +
	                        "order by co.courseNbr ", Object[].class).
	                setFetchSize(200).
	                setCacheable(true).
	                setParameter("subjectAreaId", request.getSubjectId()).
	                list();
	            for (Object[] o : courseNumbers)
	            	ret.add(new IdLabel((Long)o[0], o[1].toString() + (o[2] == null || o[2].toString().isEmpty() ? "" : " - " + o[2]), null));
			return ret;
		}
	}
	
	@GwtRpcImplements(ExamLookupSubparts.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupSubpartsBackend implements GwtRpcImplementation<ExamLookupSubparts, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(ExamLookupSubparts request, SessionContext context) {
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
			CourseOffering course = CourseOfferingDAO.getInstance().get(request.getCourseId());
			if (course.isIsControl())
				ret.add(new IdLabel(Long.MIN_VALUE + 1, MSG.examTypeOffering(), null));
			ret.add(new IdLabel(Long.MIN_VALUE, MSG.examTypeCourse(), null));
			TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(null));
			configs.addAll(SchedulingSubpartDAO.getInstance().
		            getSession().
		            createQuery("select distinct c from " +
		                    "InstrOfferingConfig c inner join c.instructionalOffering.courseOfferings co "+
		                    "where co.uniqueId = :courseOfferingId", InstrOfferingConfig.class).
		            setFetchSize(200).
		            setCacheable(true).
		            setParameter("courseOfferingId", request.getCourseId()).
		            list());
			if (!configs.isEmpty()) {
				ret.add(new IdLabel(Long.MIN_VALUE + 2, MSG.sctOwnerTypeConfigurations(), null));
				for (InstrOfferingConfig c: configs)
		        	ret.add(new IdLabel(-c.getUniqueId(), getConfigLabel(c), null));
			}
			TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator(null));
	        subparts.addAll(SchedulingSubpartDAO.getInstance().
	            getSession().
	            createQuery("select distinct s from " +
	                    "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
	                    "where co.uniqueId = :courseOfferingId", SchedulingSubpart.class).
	            setFetchSize(200).
	            setCacheable(true).
	            setParameter("courseOfferingId", request.getCourseId()).
	            list());
	        if (!subparts.isEmpty()) {
	        	ret.add(new IdLabel(Long.MIN_VALUE + 3, MSG.sctOwnerTypeSubparts(), null));
	        	for (SchedulingSubpart s: subparts)
	        		ret.add(new IdLabel(s.getUniqueId(), getSubpartLabel(s), null));
	        }
	        return ret;
		}
	}
	
	@GwtRpcImplements(ExamLookupClasses.class)
	@GwtRpcLogging(Level.DISABLED)
	public static class LookupClassesBackend implements GwtRpcImplementation<ExamLookupClasses, GwtRpcResponseList<IdLabel>> {
		@Override
		public GwtRpcResponseList<IdLabel> execute(ExamLookupClasses request, SessionContext context) {
			CourseOffering course = CourseOfferingDAO.getInstance().get(request.getCourseId());
			GwtRpcResponseList<IdLabel> ret = new GwtRpcResponseList<IdLabel>();
			TreeSet<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
	        classes.addAll(new Class_DAO().
	            getSession().
	            createQuery("select distinct c from Class_ c "+
	                    "where c.schedulingSubpart.uniqueId=:schedulingSubpartId", Class_.class).
	            setFetchSize(200).
	            setCacheable(true).
	            setParameter("schedulingSubpartId", request.getSubpartId()).
	            list());
	        for (Class_ c: classes)
	        	ret.add(new IdLabel(c.getUniqueId(), getClassLabel(c, course), null));
	        return ret;
		}
	}
}
