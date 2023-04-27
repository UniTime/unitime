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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindOnlineSectioningLogAction;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class DbFindOnlineSectioningLogAction extends FindOnlineSectioningLogAction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<SectioningAction> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			List<SectioningAction> ret = new ArrayList<SectioningAction>();
			AcademicSessionInfo session = server.getAcademicSession();
			
			SectioningLogQueryFormatter formatter = new SectioningLogQueryFormatter(session, helper);
			String join = "";
			for (String t: formatter.getGroupTypes())
				if (getQuery().hasAttribute(t))
					join += "left outer join s.groups G_" + t + " ";
			
			org.hibernate.query.Query<Object[]> q = helper.getHibSession().createQuery(
					"select l, s from OnlineSectioningLog l, Student s " +
					(getQuery().hasAttribute("area", "clasf", "classification", "major", "concentration", "campus", "program") ? "left outer join s.areaClasfMajors m " : "") +
					(getQuery().hasAttribute("minor") ? "left outer join s.areaClasfMinors n " : "") + 
					(getQuery().hasAttribute("group") ? "left outer join s.groups g " : "") + 
					(getQuery().hasAttribute("accommodation") ? "left outer join s.accomodations a " : "") + 
					(getQuery().hasAttribute("course") || getQuery().hasAttribute("lookup") || getQuery().hasAttribute("im") ? "left outer join s.courseDemands cd left outer join cd.courseRequests cr " : "") +
					(getQuery().hasAttribute("im") ? "left outer join cr.courseOffering.instructionalOffering.instrOfferingConfigs cfg left outer join cfg.instructionalMethod im " : "") +
					join +
					"where l.session.uniqueId = :sessionId and l.session = s.session and l.student = s.externalUniqueId " +
					"and (" + getQuery().toString(formatter) + ") " +
					(getQuery().hasAttribute("operation") ? "" : 
						"and (l.result is not null or l.operation not in ('reload-offering', 'check-offering', 'reload-student')) " +
						"and (l.result != 3 or l.operation not in ('validate-overrides', 'critical-courses', 'banner-update')) "
					) + "order by l.uniqueId desc", Object[].class);

			q.setParameter("sessionId", session.getUniqueId());
			if (getLimit() != null)
				q.setMaxResults(getLimit());
			
			Set<Long> processedLogIds = new HashSet<Long>();
			for (Object[] o: q.list()) {
				org.unitime.timetable.model.OnlineSectioningLog log = (org.unitime.timetable.model.OnlineSectioningLog)o[0];
				
				Student student = (Student)o[1];
				if (student == null) continue;
				if (!processedLogIds.add(log.getUniqueId())) continue;
				ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
				st.setId(student.getUniqueId());
				st.setSessionId(session.getUniqueId());
				st.setExternalId(student.getExternalUniqueId());
				st.setCanShowExternalId(iCanShowExtIds);
				st.setName(helper.getStudentNameFormat().format(student));
				for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
					st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
					st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
					st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
					st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
					st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
					st.addProgram(acm.getProgram() == null ? null : acm.getProgram().getReference(), acm.getProgram() == null ? null : acm.getProgram().getLabel());
					st.addCampus(acm.getCampus() == null ? null : acm.getCampus().getReference(), acm.getCampus() == null ? null : acm.getCampus().getLabel());
				}
				st.setDefaultCampus(server.getAcademicSession().getCampus());
				for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(student.getAreaClasfMinors())) {
					st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
				}
				for (StudentAccomodation acc: student.getAccomodations()) {
					st.addAccommodation(acc.getAbbreviation(), acc.getName());
				}
				for (StudentGroup gr: student.getGroups()) {
					if (gr.getType() == null)
						st.addGroup(gr.getGroupAbbreviation(), gr.getGroupName());
					else
						st.addGroup(gr.getType().getReference(), gr.getGroupAbbreviation(), gr.getGroupName());
				}
    			for (Advisor a: student.getAdvisors()) {
    				if (a.getLastName() != null)
    					st.addAdvisor(helper.getInstructorNameFormat().format(a));
    			}
				
				SectioningAction a = new SectioningAction();
				a.setLogId(log.getUniqueId());
				a.setStudent(st);
				a.setStudent(st);
				a.setTimeStamp(log.getTimeStamp());
				a.setOperation(Constants.toInitialCase(log.getOperation().replace('-', ' ')));
				if (log.getUser() != null && log.getUser().equals(st.getExternalId())) {
					a.setUser(helper.getStudentNameFormat().format(student));
				} else if (log.getUser() != null) {
					Advisor advisor = Advisor.findByExternalId(log.getUser(), server.getAcademicSession().getUniqueId());
					if (advisor != null) {
						a.setUser(helper.getInstructorNameFormat().format(advisor));
					} else {
						TimetableManager mgr = TimetableManager.findByExternalId(log.getUser());
						if (mgr != null)
							a.setUser(helper.getInstructorNameFormat().format(mgr));
						else
							a.setUser(log.getUser());
					}
				}
				if (log.getResult() != null) {
					OnlineSectioningLog.Action.ResultType res = OnlineSectioningLog.Action.ResultType.forNumber(log.getResult());
					if (res != null)
						a.setResult(Constants.toInitialCase(res.name()));
				}
				a.setMessage(log.getMessage());
				a.setCpuTime(log.getCpuTime());
				a.setWallTime(log.getWallTime());
				
				ret.add(a);
			}
			helper.commitTransaction();
			Collections.sort(ret);
			return ret;
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
}
