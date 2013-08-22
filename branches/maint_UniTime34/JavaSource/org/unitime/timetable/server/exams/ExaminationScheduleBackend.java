/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.server.exams;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable.ExaminationScheduleRpcRequest;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable.ExaminationScheduleRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.service.SolverService;

@GwtRpcImplements(ExaminationScheduleRpcRequest.class)
public class ExaminationScheduleBackend implements GwtRpcImplementation<ExaminationScheduleRpcRequest, ExaminationScheduleRpcResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Override
	public ExaminationScheduleRpcResponse execute(ExaminationScheduleRpcRequest request, SessionContext context) {
		org.hibernate.Session hibSession = ExamDAO.getInstance().getSession();
		
		Exam exam = ExamDAO.getInstance().get(request.getExamId());
		
		context.checkPermission(exam, Right.ExaminationDetail);
		ExamSolverProxy proxy = examinationSolverService.getSolver();
		if (proxy != null && !exam.getExamType().getUniqueId().equals(proxy.getExamTypeId())) proxy = null;
		
		ExaminationScheduleRpcResponse response = new ExaminationScheduleRpcResponse();
		response.setExamType(exam.getExamType().getLabel());

		List<Object[]> exams = new ArrayList<Object[]>();
		exams.addAll(hibSession.createQuery(
				"select o, enrl.courseOffering from ExamOwner o, StudentClassEnrollment enrl inner join enrl.courseOffering co " +
				"where o.ownerType = :type and o.ownerId = co.uniqueId and enrl.student.uniqueId = :studentId and o.exam.examType.uniqueId = :examTypeId")
				.setInteger("type", ExamOwner.sOwnerTypeCourse)
				.setLong("studentId", request.getStudentId())
				.setLong("examTypeId", exam.getExamType().getUniqueId())
				.setCacheable(true).list());
		exams.addAll(hibSession.createQuery(
				"select o, enrl.courseOffering from ExamOwner o, StudentClassEnrollment enrl inner join enrl.courseOffering.instructionalOffering io " +
				"where o.ownerType = :type and o.ownerId = io.uniqueId and enrl.student.uniqueId = :studentId and o.exam.examType.uniqueId = :examTypeId")
				.setInteger("type", ExamOwner.sOwnerTypeOffering)
				.setLong("studentId", request.getStudentId())
				.setLong("examTypeId", exam.getExamType().getUniqueId())
				.setCacheable(true).list());
		exams.addAll(hibSession.createQuery(
				"select o, enrl.courseOffering from ExamOwner o, StudentClassEnrollment enrl inner join enrl.clazz.schedulingSubpart.instrOfferingConfig cfg " +
				"where o.ownerType = :type and o.ownerId = cfg.uniqueId and enrl.student.uniqueId = :studentId and o.exam.examType.uniqueId = :examTypeId")
				.setInteger("type", ExamOwner.sOwnerTypeConfig)
				.setLong("studentId", request.getStudentId())
				.setLong("examTypeId", exam.getExamType().getUniqueId())
				.setCacheable(true).list());
		exams.addAll(hibSession.createQuery(
				"select o, enrl.courseOffering from ExamOwner o, StudentClassEnrollment enrl inner join enrl.clazz c " +
				"where o.ownerType = :type and o.ownerId = c.uniqueId and enrl.student.uniqueId = :studentId and o.exam.examType.uniqueId = :examTypeId")
				.setInteger("type", ExamOwner.sOwnerTypeClass)
				.setLong("studentId", request.getStudentId())
				.setLong("examTypeId", exam.getExamType().getUniqueId())
				.setCacheable(true).list());
		for (Object[] o: exams) {
			ExamOwner owner = (ExamOwner)o[0];
			CourseOffering co = (CourseOffering)o[1];
			Exam x = owner.getExam();
			
			RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Examination);
    		related.setUniqueId(x.getUniqueId());
    		related.setName(x.getName() == null ? x.generateName() : x.getName());
    		related.addCourseName(co.getCourseName());
    		related.addCourseTitle(co.getTitle());
    		related.setInstruction(x.getExamType().getLabel());
    		related.setInstructionType(x.getExamType().getType());
    		if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
    			Class_ clazz = (Class_)owner.getOwnerObject();
    			related.setSectionNumber(clazz.getSectionNumberString(hibSession));
	    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getAbbv());
        		if (clazz.getClassSuffix() != null) related.addExternalId(clazz.getClassSuffix());
    		} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
    			InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
				related.setSectionNumber("[" + config.getName() + "]");
				related.setInstruction(MESSAGES.colConfig());
    		} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeCourse) {
    			related.setInstruction(MESSAGES.colCourse());
    		} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
    			related.setInstruction(MESSAGES.colOffering());
    		}
			if (context != null && context.hasPermission(x, Right.ExaminationDetail))
				related.setDetailPage("examDetail.do?examId=" + x.getUniqueId());
    		for (DepartmentalInstructor i: x.getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setExternalId(i.getExternalUniqueId());
				instructor.setEmail(i.getEmail());
				related.addInstructor(instructor);
			}				
			
			if (proxy == null) {
	    		if (x.getAssignedPeriod() != null) {
	    			ExamPeriod period = x.getAssignedPeriod();
	    			related.setDate(period.getStartDateLabel());
	    			related.setDayOfYear(period.getDateOffset());
	    			related.setStartSlot(period.getStartSlot());
	    			related.setEndSlot(period.getEndSlot());
	    			int printOffset = (x.getPrintOffset() == null ? 0 : x.getPrintOffset());
	    			related.setTime(period.getStartTimeLabel(printOffset) + " - " + period.getEndTimeLabel(x.getLength(), printOffset));
	    		}
	    		for (Location r: x.getAssignedRooms()) {
					ResourceInterface location = new ResourceInterface();
					location.setType(ResourceType.ROOM);
					location.setId(r.getUniqueId());
					location.setName(r.getLabel());
					location.setSize(r.getCapacity());
					location.setRoomType(r.getRoomTypeLabel());
					location.setBreakTime(r.getEffectiveBreakTime());
					location.setMessage(r.getEventMessage());
					related.addLocation(location);
	    		}
			} else {
				ExamAssignment assignment = proxy.getAssignment(x.getUniqueId());
				if (assignment != null && assignment.getPeriod() != null) {
	    			ExamPeriod period = assignment.getPeriod();
	    			related.setDate(period.getStartDateLabel());
	    			related.setDayOfYear(period.getDateOffset());
	    			related.setStartSlot(period.getStartSlot());
	    			related.setEndSlot(period.getEndSlot());
	    			int printOffset = (x.getPrintOffset() == null ? 0 : x.getPrintOffset());
	    			related.setTime(period.getStartTimeLabel(printOffset) + " - " + period.getEndTimeLabel(x.getLength(), printOffset));
	    		}
				if (assignment != null && assignment.getRooms() != null) {
					for (ExamRoomInfo r: assignment.getRooms()) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(r.getLocationId());
						location.setName(r.getName());
						location.setSize(r.getCapacity());
						location.setRoomType(r.getLocation().getRoomTypeLabel());
						location.setBreakTime(r.getLocation().getEffectiveBreakTime());
						location.setMessage(r.getLocation().getEventMessage());
						related.addLocation(location);
					}
				}
			}
			
    		response.addExam(related);
		}
		
		return response;
	}

}
