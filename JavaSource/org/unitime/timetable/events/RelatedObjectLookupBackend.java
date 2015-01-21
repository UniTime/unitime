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
package org.unitime.timetable.events;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcResponse;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RelatedObjectLookupRpcRequest.class)
public class RelatedObjectLookupBackend extends EventAction<RelatedObjectLookupRpcRequest, GwtRpcResponseList<RelatedObjectLookupRpcResponse>> {

	@Override
	public GwtRpcResponseList<RelatedObjectLookupRpcResponse> execute(RelatedObjectLookupRpcRequest request, EventContext context) {
		context.checkPermission(Right.EventAddCourseRelated);

		GwtRpcResponseList<RelatedObjectLookupRpcResponse> response = new GwtRpcResponseList<RelatedObjectLookupRpcResponse>();

		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		
		switch (request.getLevel()) {
		case SESSION:
			for (Object[] object: (List<Object[]>)hibSession.createQuery(
					"select s.uniqueId, s.subjectAreaAbbreviation from SubjectArea s " +
					"where s.session.uniqueId = :sessionId " +
					"order by s.subjectAreaAbbreviation"
					).setLong("sessionId", request.getUniqueId()).setCacheable(true).list()) {
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.SUBJECT,
						(Long)object[0],
						(String)object[1]));
			}
			break;
		case SUBJECT:
			for (CourseOffering course: (List<CourseOffering>)hibSession.createQuery(
					"select co from CourseOffering co "+
                    "where co.subjectArea.uniqueId = :subjectAreaId "+
                    "and co.instructionalOffering.notOffered = false " +
                    "order by co.courseNbr"
                    ).setLong("subjectAreaId", request.getUniqueId()).setCacheable(true).list()) {
				RelatedObjectInterface related = new RelatedObjectInterface();
				if (course.getIsControl()) {
					related.setType(RelatedObjectInterface.RelatedObjectType.Offering);
					related.setUniqueId(course.getInstructionalOffering().getUniqueId());
					related.setName(course.getCourseName());
					related.addCourseName(course.getCourseName());
					related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
					related.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId()});
					related.setNote(course.getScheduleBookNote());
					if (context.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
						related.setDetailPage("instructionalOfferingDetail.do?io=" + course.getInstructionalOffering().getUniqueId());
				} else {
					related.setType(RelatedObjectInterface.RelatedObjectType.Course);
					related.setUniqueId(course.getUniqueId());
					related.setName(course.getCourseName());
					related.addCourseName(course.getCourseName());
					related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
					related.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId()});
					related.setNote(course.getScheduleBookNote());
					if (context.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
						related.setDetailPage("instructionalOfferingDetail.do?io=" + course.getInstructionalOffering().getUniqueId());
				}
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.COURSE,
						course.getUniqueId(),
						course.getCourseNbr(),
						course.getTitle(),
						related));
			}
			break;
		case COURSE:
			CourseOffering course = CourseOfferingDAO.getInstance().get(request.getUniqueId());
			if (course == null) break;
			if (course.isIsControl()) {
				RelatedObjectInterface relatedOffering = new RelatedObjectInterface();
				relatedOffering.setType(RelatedObjectInterface.RelatedObjectType.Offering);
				relatedOffering.setUniqueId(course.getInstructionalOffering().getUniqueId());
				relatedOffering.setName(course.getCourseName());
				relatedOffering.setNote(course.getScheduleBookNote());
				relatedOffering.addCourseName(course.getCourseName());
				relatedOffering.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
				relatedOffering.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId()});
				if (context.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
					relatedOffering.setDetailPage("instructionalOfferingDetail.do?io=" + course.getInstructionalOffering().getUniqueId());
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.OFFERING,
						course.getInstructionalOffering().getUniqueId(),
						"Offering",
						relatedOffering));
			}
			
			RelatedObjectInterface relatedCourse = new RelatedObjectInterface();
			relatedCourse.setType(RelatedObjectInterface.RelatedObjectType.Course);
			relatedCourse.setUniqueId(course.getUniqueId());
			relatedCourse.setName(course.getCourseName());
			relatedCourse.setNote(course.getScheduleBookNote());
			relatedCourse.addCourseName(course.getCourseName());
			relatedCourse.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
			relatedCourse.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId()});
			if (context.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
				relatedCourse.setDetailPage("instructionalOfferingDetail.do?io=" + course.getInstructionalOffering().getUniqueId());
			response.add(new RelatedObjectLookupRpcResponse(
					RelatedObjectLookupRpcRequest.Level.COURSE,
					course.getUniqueId(),
					"Course",
					relatedCourse));
			
			Set<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(null));
			configs.addAll(course.getInstructionalOffering().getInstrOfferingConfigs());
			
			Set<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator(null));
			
			if (!configs.isEmpty()) {
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.NONE,
						null,
						"-- Configurations --"));
				for (InstrOfferingConfig config: configs) {
					RelatedObjectInterface relatedConfig = new RelatedObjectInterface();
					relatedConfig.setType(RelatedObjectInterface.RelatedObjectType.Config);
					relatedConfig.setUniqueId(config.getUniqueId());
					relatedConfig.setName(config.getName());
					relatedConfig.addCourseName(course.getCourseName());
					relatedConfig.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
					relatedConfig.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId(), config.getUniqueId()});
					if (context.hasPermission(config.getInstructionalOffering(), Right.InstructionalOfferingDetail))
						relatedCourse.setDetailPage("instructionalOfferingDetail.do?io=" + config.getInstructionalOffering().getUniqueId());
					response.add(new RelatedObjectLookupRpcResponse(
							RelatedObjectLookupRpcRequest.Level.CONFIG,
							config.getUniqueId(),
							config.getName(),
							relatedConfig));
					subparts.addAll(config.getSchedulingSubparts());
				}
			}
			
			if (!subparts.isEmpty()) {
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.NONE,
						null,
						"-- Subparts --"));
				for (SchedulingSubpart subpart: subparts) {
		            String name = subpart.getItype().getAbbv();
		            String sufix = subpart.getSchedulingSubpartSuffix();
		            SchedulingSubpart parent = subpart.getParentSubpart();
		            while (parent != null) {
		                name =  "\u00A0\u00A0\u00A0\u00A0" + name;
		                parent = parent.getParentSubpart();
		            }
		            if (subpart.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size() > 1)
		                name += " [" + subpart.getInstrOfferingConfig().getName() + "]";
					response.add(new RelatedObjectLookupRpcResponse(
							RelatedObjectLookupRpcRequest.Level.SUBPART,
							subpart.getUniqueId(),
							name + (sufix == null || sufix.isEmpty() ? "" : " ("+sufix+")")));
				}
			}
			break;
		case SUBPART:
			course = CourseOfferingDAO.getInstance().get(request.getCourseId());
			SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(request.getUniqueId());
			if (subpart == null) break;
			
			Set<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
			classes.addAll(subpart.getClasses());
			
			for (Class_ clazz: classes) {
				String extId = clazz.getClassSuffix(course);
				RelatedObjectInterface relatedClass = new RelatedObjectInterface();
				relatedClass.setType(RelatedObjectInterface.RelatedObjectType.Class);
				relatedClass.setUniqueId(clazz.getUniqueId());
				relatedClass.setName(clazz.getClassLabel(course));
				String note = course.getScheduleBookNote();
	    		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty())
	    			note = (note == null || note.isEmpty() ? "" : note + "\n") + clazz.getSchedulePrintNote();
				relatedClass.setNote(note);
				relatedClass.addCourseName(course.getCourseName());
				relatedClass.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
				relatedClass.setSelection(new long[] {course.getSubjectArea().getUniqueId(), course.getUniqueId(), subpart.getUniqueId(), clazz.getUniqueId()});
				if (context.hasPermission(clazz, Right.ClassDetail))
					relatedClass.setDetailPage("classDetail.do?cid=" + clazz.getUniqueId());
				response.add(new RelatedObjectLookupRpcResponse(
						RelatedObjectLookupRpcRequest.Level.CLASS,
						clazz.getUniqueId(),
						clazz.getSectionNumberString(hibSession),
						(extId == null || extId.isEmpty() || extId.equalsIgnoreCase(clazz.getSectionNumberString(hibSession)) ? null : extId),
						relatedClass
						));
			}
			
			break;
		default:
			response.add(new RelatedObjectLookupRpcResponse(
					RelatedObjectLookupRpcRequest.Level.NONE,
					null,
					"N/A"));
			break;
		}

		
		return response;
	}
}
