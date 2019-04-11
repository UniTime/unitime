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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindOnlineSectioningLogAction;
import org.unitime.timetable.util.Constants;

import com.google.protobuf.InvalidProtocolBufferException;

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
			DateFormat df = Localization.getDateFormat(CONST.timeStampFormat());
			NumberFormat nf = Localization.getNumberFormat(CONST.executionTimeFormat());
			AcademicSessionInfo session = server.getAcademicSession();
			
			SectioningLogQueryFormatter formatter = new SectioningLogQueryFormatter(session, helper);
			String join = "";
			for (String t: formatter.getGroupTypes())
				if (getQuery().hasAttribute(t))
					join += "left outer join s.groups G_" + t + " ";
			
			org.hibernate.Query q = helper.getHibSession().createQuery(
					"select l, s from OnlineSectioningLog l, Student s " +
					(getQuery().hasAttribute("area", "clasf", "classification", "major") ? "left outer join s.areaClasfMajors m " : "") +
					(getQuery().hasAttribute("minor") ? "left outer join s.areaClasfMinors n " : "") + 
					(getQuery().hasAttribute("group") ? "left outer join s.groups g " : "") + 
					(getQuery().hasAttribute("accommodation") ? "left outer join s.accomodations a " : "") + 
					(getQuery().hasAttribute("course") || getQuery().hasAttribute("lookup") ? "left outer join s.courseDemands cd left outer join cd.courseRequests cr " : "") + join +
					"where l.session.uniqueId = :sessionId and l.session = s.session and l.student = s.externalUniqueId " +
					"and (" + getQuery().toString(formatter) + ") " +
					"and (l.result is not null or l.operation not in ('reload-offering', 'check-offering')) order by l.timeStamp desc, l.uniqueId desc");

			q.setLong("sessionId", session.getUniqueId());
			if (getLimit() != null)
				q.setMaxResults(getLimit());
			
			Set<Long> processedLogIds = new HashSet<Long>();
			for (Object[] o: (List<Object[]>)q.list()) {
				try {
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
						st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation());
						st.addClassification(acm.getAcademicClassification().getCode());
						st.addMajor(acm.getMajor().getCode());
					}
					for (StudentAccomodation acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation());
					}
					for (StudentGroup gr: student.getGroups()) {
						if (gr.getType() == null)
							st.addGroup(gr.getGroupAbbreviation(), gr.getGroupName());
						else
							st.addGroup(gr.getType().getReference(), gr.getGroupAbbreviation(), gr.getGroupName());
					}
					
					SectioningAction a = new SectioningAction();
					a.setStudent(st);
					a.setTimeStamp(log.getTimeStamp());
					a.setOperation(Constants.toInitialCase(log.getOperation().replace('-', ' ')));
					OnlineSectioningLog.Action action = OnlineSectioningLog.Action.parseFrom(log.getAction());
					if (action.hasCpuTime())
						a.setCpuTime(action.getCpuTime());
					if (action.hasStartTime() && action.hasEndTime())
						a.setWallTime(action.getEndTime() - action.getStartTime());
					if (action.hasUser())
						a.setUser(action.getUser().getName());
					if (action.hasResult())
						a.setResult(Constants.toInitialCase(action.getResult().name()));
					// a.setProto(action.toString().replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;").replace("\n", "<br>"));
					String html = "<table class='unitime-ChangeLog'>";
					html += "<tr><td class='unitime-MainTableHeader' colspan='2'>General</td></tr>";
					html += "<tr><td><b>" + MSG.colOperation() + ":</b></td><td>" + Constants.toInitialCase(log.getOperation().replace('-', ' ')) + "</td></tr>";
					if (action.hasResult())
						html += "<tr><td><b>" + MSG.colResult() + ":</b></td><td>" + Constants.toInitialCase(action.getResult().name()) + "</td></tr>";
					html += "<tr><td><b>" + MSG.colStudent() + ":</b></td><td>" + helper.getStudentNameFormat().format(student) + "</td></tr>";
					for (OnlineSectioningLog.Entity other: action.getOtherList()) {
						html += "<tr><td><b>" + Constants.toInitialCase(other.getType().name()) + ":</b></td><td>" + other.getName() + "</td></tr>";
					}
					html += "<tr><td><b>Time Stamp:</b></td><td>" + df.format(log.getTimeStamp()) + "</td></tr>";
					for (OnlineSectioningLog.Property p: action.getOptionList()) {
						html += "<tr><td><b>" + Constants.toInitialCase(p.getKey()) + ":</b></td><td><div class='property'>" + (p.hasValue() ? p.getValue() : "") + "</div></td></tr>";
					}
					if (action.hasCpuTime()) {
						html += "<tr><td><b>" + MSG.colCpuTime() + ":</b></td><td>" + nf.format(0.000000001 * action.getCpuTime()) + "</td></tr>";
					}
					if (action.hasStartTime() && action.hasEndTime()) {
						html += "<tr><td nowrap><b>" + MSG.colWallTime() + ":</b></td><td>" + nf.format(0.001 * (action.getEndTime() - action.getStartTime())) + "</td></tr>";
					}
					if (action.hasApiGetTime()) {
						html += "<tr><td nowrap><b>" + MSG.colApiGetTime() + ":</b></td><td>" + nf.format(0.001 * action.getApiGetTime()) + "</td></tr>";
					}
					if (action.hasApiPostTime()) {
						html += "<tr><td nowrap><b>" + MSG.colApiPostTime() + ":</b></td><td>" + nf.format(0.001 * action.getApiPostTime()) + "</td></tr>";
					}
					if (action.hasApiException()) {
						html += "<tr><td nowrap><b>" + MSG.colApiException() + ":</b></td><td>" + action.getApiException() + "</td></tr>";
					}
					
					if (!action.getRequestList().isEmpty()) {
						html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.courseRequestsCourses() + "</td></tr>";
						html += "<tr><td colspan='2'><table cellspacing='0' cellpadding='2'>" +
								"<td class='unitime-TableHeader'>" + MSG.colPriority() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colCourse() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colPreferences() + "</td></tr>";
					}
					String request = "";
					String selected = "";
					int notAlt = 0, lastFT = -1;
					for (OnlineSectioningLog.Request r: action.getRequestList()) {
						if (!r.getAlternative()) notAlt = r.getPriority() + 1;
						int idx = 0;
						for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
							if (idx == 0) {
								html += (r.getPriority() > 0 && lastFT != r.getPriority() ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (lastFT == r.getPriority() ? "" : !r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternative(1 + r.getPriority() - notAlt)) + "</td>";
								html += (r.getPriority() > 0 && lastFT != r.getPriority() ?"<td class='top-border-dashed' colspan='2'>":"<td colspan='2'>") + CONST.freePrefix() + " ";
								request += (lastFT == r.getPriority() ? ", " : (request.isEmpty() ? "" : "<br>") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". " + CONST.freePrefix() + " ");
							} else {
								html += ", ";
								request += ", ";
							}
							idx++;
							html += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
							request += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
							html += "</td></tr>";
							lastFT = r.getPriority();
						}
						if (r.getFreeTimeList().isEmpty())
							for (OnlineSectioningLog.Entity e: r.getCourseList()) {
								if (idx == 0) {
									html += (r.getPriority() > 0 ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (!r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternative(1 + r.getPriority() - notAlt)) + "</td>";
									html += (r.getPriority() > 0 ?"<td class='top-border-dashed'>":"<td>");
									request += (request.isEmpty() ? "" : "<br>") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". ";
								} else {
									html += "<tr><td></td><td>";
									request += ", ";
								}
								idx++;
								html += e.getName();
								request += e.getName();
								html += (r.getPriority() > 0 && idx == 1 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
								for (int i = 0; i < e.getParameterCount(); i++)
									html += (i > 0 ? ", " : "") + e.getParameter(i).getValue();
								html += "</td></tr>";
							}
						for (OnlineSectioningLog.Section s: r.getSectionList()) {
							if (s.getPreference() == OnlineSectioningLog.Section.Preference.SELECTED) {
								if (!selected.isEmpty()) selected += "<br>";
								String loc = "";
								for (OnlineSectioningLog.Entity e: s.getLocationList()) {
									if (!loc.isEmpty()) loc += ", ";
									loc += e.getName();
								}
								String instr = "";
								for (OnlineSectioningLog.Entity e: s.getInstructorList()) {
									if (!instr.isEmpty()) instr += ", ";
									instr += e.getName();
								}
								selected += s.getCourse().getName() + " " + s.getSubpart().getName() + " " + s.getClazz().getName() + " " +
									(s.hasTime() ? DayCode.toString(s.getTime().getDays()) + " " + time(s.getTime().getStart()) + " - " + time(s.getTime().getStart() + s.getTime().getLength()) : "") + " " + loc;
							}
						}
					}
					if (!action.getRequestList().isEmpty()) {
						html += "</table></td></tr>";
					}
					String enrollment = "";
					Map<OnlineSectioningLog.Enrollment.EnrollmentType, String> enrollmentByType = new HashMap<OnlineSectioningLog.Enrollment.EnrollmentType, String>();
					for (OnlineSectioningLog.Enrollment e: action.getEnrollmentList()) {
						enrollment = "";
						html += "<tr><td class='unitime-MainTableHeader' colspan='2'>"+ (e.hasType() ? Constants.toInitialCase(e.getType().name()) + " ": "") + MSG.enrollmentsTable() + "</td></tr>";
						html += "<tr><td colspan='2'><table cellspacing='0' cellpadding='2'>" +
								"<td class='unitime-TableHeader'>" + MSG.colCourse() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colSubject() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colClass() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colDays() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colStart() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colEnd() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colDate() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colRoom() + "</td>" +
								"<td class='unitime-TableHeader'>" + MSG.colInstructor() + "</td></tr>";
						for (OnlineSectioningLog.Section s: e.getSectionList()) {
							if (!s.hasCourse()) continue;
							if (!enrollment.isEmpty()) enrollment += "<br>";
							String loc = "";
							for (OnlineSectioningLog.Entity r: s.getLocationList()) {
								if (!loc.isEmpty()) loc += ", ";
								loc += r.getName();
							}
							String instr = "";
							for (OnlineSectioningLog.Entity r: s.getInstructorList()) {
								if (!instr.isEmpty()) instr += ", ";
								instr += r.getName();
							}
							html += "<tr>" +
									"<td>" + s.getCourse().getName() + "</td>" +
									"<td>" + s.getSubpart().getName() + "</td>" +
									"<td>" + s.getClazz().getName() + "</td>" +
									"<td>" + (s.hasTime() ? DayCode.toString(s.getTime().getDays()) : "") + "</td>" +
									"<td>" + (s.hasTime() ? time(s.getTime().getStart()) : "") + "</td>" +
									"<td>" + (s.hasTime() ? time(s.getTime().getStart() + s.getTime().getLength()) : "") + "</td>" +
									"<td>" + (s.hasTime() && s.getTime().hasPattern() ? s.getTime().getPattern() : "") + "</td>" +
									"<td>" + loc + "</td>" +
									"<td>" + instr + "</td>" +
									"</tr>";
							enrollment += s.getCourse().getName() + " " + s.getSubpart().getName() + " " + s.getClazz().getName() + " " +
								(s.hasTime() ? DayCode.toString(s.getTime().getDays()) + " " + time(s.getTime().getStart()) : "") + " " + loc;
						}
						html += "</table></td></tr>";
						enrollmentByType.put(e.getType(), enrollment);
					}
					String message = "";
					if (!action.getMessageList().isEmpty()) {
						html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.tableMessages() + "</td></tr>";
						for (OnlineSectioningLog.Message m: action.getMessageList()) {
							if (m.hasText()) {
								html += "<tr><td><b>" + m.getLevel().name() + ":</b></td><td>" + m.getText() + "</td></tr>"; message = m.getText();
							}
							if (m.hasException()) {
								html += "<tr><td><b>Exception:</b></td><td>" + m.getException() + "</td></tr>"; message = m.getException();
							}
						}
					}
					html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.tableProto() + "</td></tr>";
					html += "<tr><td colspan='2' class='proto'>" + action.toString().replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;").replace("\n", "<br>") + "</td></tr>";
					html += "</table>";
					if ("student-email".equals(log.getOperation())) {
						for (OnlineSectioningLog.Property p: action.getOptionList())
							if ("email".equals(p.getKey()) && p.hasValue())
								html = p.getValue();
					}
					if (action.hasResult() && OnlineSectioningLog.Action.ResultType.FAILURE.equals(action.getResult()) && !message.isEmpty()) {
						a.setMessage(message);
					} else if ("suggestions".equals(log.getOperation())) {
						a.setMessage(selected.isEmpty() ? message : selected);
					} else if ("section".equals(log.getOperation())) {
						a.setMessage(request.isEmpty() ? message : request);
					} else if (enrollmentByType.containsKey(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED)) {
						a.setMessage(enrollmentByType.get(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED));
					} else {
						a.setMessage(enrollment.isEmpty() ? request.isEmpty() ? message : request : enrollment);
					}
					a.setProto(html);
					
					ret.add(a);
				} catch (InvalidProtocolBufferException e) {}
			}
			helper.commitTransaction();
			return ret;
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
}
