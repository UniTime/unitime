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
package org.unitime.timetable.onlinesectioning.status;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.QueryFormatter;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.StudentGroupTypeDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class FindOnlineSectioningLogAction implements OnlineSectioningAction<List<SectioningAction>> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	
	private Query iQuery;
	private Integer iLimit = 100;
	protected boolean iCanShowExtIds = false;
	
	public FindOnlineSectioningLogAction forQuery(String query, boolean canShowExtIds) {
		iQuery = new Query(query.isEmpty() ? "limit:100" : query);
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
		iCanShowExtIds = canShowExtIds;
		return this;
	}
	
	public Query getQuery() { return iQuery; }
	
	public Integer getLimit() { return iLimit; }

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
			
			org.hibernate.Query q = helper.getHibSession().createQuery(
					"select l, s.uniqueId from OnlineSectioningLog l, Student s " +
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
					) + "order by l.uniqueId desc");

			q.setLong("sessionId", session.getUniqueId());
			if (getLimit() != null)
				q.setMaxResults(getLimit());
			
			Set<Long> processedLogIds = new HashSet<Long>();
			for (Object[] o: (List<Object[]>)q.list()) {
				org.unitime.timetable.model.OnlineSectioningLog log = (org.unitime.timetable.model.OnlineSectioningLog)o[0];
				
				XStudent student = server.getStudent((Long)o[1]);
				if (student == null) continue;
				if (!processedLogIds.add(log.getUniqueId())) continue;
				ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
				st.setId(student.getStudentId());
				st.setSessionId(session.getUniqueId());
				st.setExternalId(student.getExternalId());
				st.setCanShowExternalId(iCanShowExtIds);
				st.setName(student.getName());
				for (XAreaClassificationMajor acm: student.getMajors()) {
					st.addArea(acm.getArea(), acm.getAreaLabel());
					st.addClassification(acm.getClassification(), acm.getClassificationLabel());
					st.addMajor(acm.getMajor(), acm.getMajorLabel());
					st.addConcentration(acm.getConcentration(), acm.getConcentrationLabel());
					st.addDegree(acm.getDegree(), acm.getDegreeLabel());
					st.addProgram(acm.getProgram(), acm.getProgramLabel());
					st.addCampus(acm.getCampus(), acm.getCampusLabel());
				}
				st.setDefaultCampus(server.getAcademicSession().getCampus());
				for (XAreaClassificationMajor acm: student.getMinors()) {
					st.addMinor(acm.getMajor(), acm.getMajorLabel());
				}
				for (XStudent.XGroup gr: student.getGroups()) {
					st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
				}
				for (XStudent.XGroup acc: student.getAccomodations()) {
					st.addAccommodation(acc.getAbbreviation(), acc.getTitle());
				}
				for (XStudent.XAdvisor a: student.getAdvisors()) {
					if (a.getName() != null) st.addAdvisor(a.getName());
				}

				SectioningAction a = new SectioningAction();
				a.setLogId(log.getUniqueId());
				a.setStudent(st);
				a.setTimeStamp(log.getTimeStamp());
				a.setOperation(Constants.toInitialCase(log.getOperation().replace('-', ' ')));
				if (log.getUser() != null && log.getUser().equals(st.getExternalId())) {
					a.setUser(student.getName());
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
					OnlineSectioningLog.Action.ResultType res = OnlineSectioningLog.Action.ResultType.valueOf(log.getResult());
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
	
	public static String getHTML(OnlineSectioningLog.Action action) {
		DateFormat df = Localization.getDateFormat(CONST.timeStampFormat());
		NumberFormat nf = Localization.getNumberFormat(CONST.executionTimeFormat());
		DateFormat rf = Localization.getDateFormat(CONST.requestDateFormat());
		String html = "<table class='unitime-ChangeLog'>";
		html += "<tr><td class='unitime-MainTableHeader' colspan='2'>General</td></tr>";
		html += "<tr><td><b>" + MSG.colOperation() + ":</b></td><td>" + Constants.toInitialCase(action.getOperation().replace('-', ' ')) + "</td></tr>";
		if (action.hasResult())
			html += "<tr><td><b>" + MSG.colResult() + ":</b></td><td>" + Constants.toInitialCase(action.getResult().name()) + "</td></tr>";
		if (action.hasStudent() && action.getStudent().hasName()) {
			html += "<tr><td><b>" + MSG.colStudent() + ":</b></td><td>" + action.getStudent().getName() + "</td></tr>";
		}
		for (OnlineSectioningLog.Entity other: action.getOtherList()) {
			html += "<tr><td><b>" + Constants.toInitialCase(other.getType().name()) + ":</b></td><td>" + other.getName() + "</td></tr>";
		}
		html += "<tr><td><b>Time Stamp:</b></td><td>" + df.format(action.getStartTime()) + "</td></tr>";
		for (OnlineSectioningLog.Property p: action.getOptionList()) {
			if ("student-email".equals(action.getOperation()) && p.getKey().equalsIgnoreCase("email")) continue;
			html += "<tr><td><b>" + Constants.toInitialCase(p.getKey()) + ":</b></td><td><div class='property' onclick='gwtPropertyClick(this);' title='" + MSG.changeLogClickToCopyToClipboard() + "'>" + (p.hasValue() ? StringEscapeUtils.escapeHtml(p.getValue()) : "") + "</div></td></tr>";
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
		if ("student-email".equals(action.getOperation())) {
			for (OnlineSectioningLog.Property p: action.getOptionList())
				if ("email".equals(p.getKey()) && p.hasValue()) {
					html += "<tr><td class='unitime-MainTableHeader' colspan='2'>Email</td></tr>";
					html += "<tr><td colspan='2'>" + p.getValue() + "</td></tr>";
				}
		}

		if (!action.getRequestList().isEmpty()) {
			html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.courseRequestsCourses() + "</td></tr>";
			html += "<tr><td colspan='2'><table cellspacing='0' cellpadding='2'>" +
					"<td class='unitime-TableHeader'>" + MSG.colPriority() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colCourse() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colPreferences() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colCritical() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colWaitList() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colRequestTimeStamp() + "</td></tr>";
		}
		int notAlt = 0, lastFT = -1;
		String creditNote = null;
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			if (!r.getAlternative()) notAlt = r.getPriority() + 1;
			int idx = 0;
			for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
				if (idx == 0) {
					html += (r.getPriority() > 0 && lastFT != r.getPriority() ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (lastFT == r.getPriority() ? "" : !r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternate(1 + r.getPriority() - notAlt)) + "</td>";
					html += (r.getPriority() > 0 && lastFT != r.getPriority() ?"<td class='top-border-dashed' colspan='4'>":"<td colspan='4'>") + CONST.freePrefix() + " ";
				} else {
					html += ", ";
				}
				idx++;
				html += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
				lastFT = r.getPriority();
			}
			if (r.getFreeTimeCount() > 0) {
				html += (r.getPriority() > 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
				html += (r.hasTimeStamp() ? rf.format(new Date(r.getTimeStamp())) : "");
				html += "</td></tr>";
			}
			if (r.getFreeTimeList().isEmpty())
				for (OnlineSectioningLog.Entity e: r.getCourseList()) {
					if (idx == 0) {
						html += (r.getPriority() > 0 ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (!r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternate(1 + r.getPriority() - notAlt)) + "</td>";
						html += (r.getPriority() > 0 ?"<td class='top-border-dashed'>":"<td>");
					} else {
						html += "<tr><td></td><td>";
					}
					html += e.getName();
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					for (int i = 0; i < e.getParameterCount(); i++) {
						if ("credit_note".equals(e.getParameter(i).getKey())) {
							creditNote = e.getParameter(i).getValue();
							continue;
						}
						html += (i > 0 ? ", " : "") + e.getParameter(i).getValue();
					}
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					html += (idx == 0 && r.getCritical() ? MSG.opSetCritical() : idx == 0 && r.getImportant() ? MSG.opSetImportant() : 
						idx == 0 && r.getVital() ? MSG.opSetVital() : "");
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					html += (idx == 0 && r.getWaitList() && r.hasWaitlistedTimeStamp() ? df.format(new Date(r.getWaitlistedTimeStamp())) :
						idx == 0 && r.getWaitList() ? MSG.courseWaitListed() : idx == 0 && r.getNoSubs() ? MSG.courseNoSubs() : "");
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					html += (idx == 0 && r.hasTimeStamp() ? rf.format(new Date(r.getTimeStamp())) : "");
					html += "</td></tr>";
					idx++;
				}
		}
		if (creditNote != null && !creditNote.isEmpty()) {
			html += "<tt><td class='top-border-dashed' colspan='6'>" + creditNote + "</td></tr>";	
		}
		if (!action.getRequestList().isEmpty()) {
			html += "</table></td></tr>";
		}
		
		if (!action.getRecommendationList().isEmpty()) {
			html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.advisorRequestsCourses() + "</td></tr>";
			html += "<tr><td colspan='2'><table cellspacing='0' cellpadding='2'>" +
					"<td class='unitime-TableHeader'>" + MSG.colPriority() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colCourse() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colPreferences() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colCredit() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colNote() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colCritical() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colWaitList() + "</td></tr>";
		}
		notAlt = 0; lastFT = -1;
		creditNote = null;
		for (OnlineSectioningLog.Request r: action.getRecommendationList()) {
			if (!r.getAlternative()) notAlt = r.getPriority() + 1;
			int idx = 0;
			for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
				if (idx == 0) {
					html += (r.getPriority() > 0 && lastFT != r.getPriority() ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (lastFT == r.getPriority() ? "" : !r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternate(1 + r.getPriority() - notAlt)) + "</td>";
					html += (r.getPriority() > 0 && lastFT != r.getPriority() ?"<td class='top-border-dashed' colspan='4'>":"<td colspan='4'>") + CONST.freePrefix() + " ";
				} else {
					html += ", ";
				}
				idx++;
				html += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
				lastFT = r.getPriority();
			}
			if (r.getFreeTimeCount() > 0) {
				html += "</td></tr>";
			}
			if (r.getFreeTimeList().isEmpty())
				for (OnlineSectioningLog.Entity e: r.getCourseList()) {
					if (idx == 0) {
						html += (r.getPriority() > 0 ? "<tr><td class='top-border-dashed'>" : "<tr><td>") + (!r.getAlternative() ? MSG.courseRequestsPriority(1 + r.getPriority()) : MSG.courseRequestsAlternate(1 + r.getPriority() - notAlt)) + "</td>";
						html += (r.getPriority() > 0 ?"<td class='top-border-dashed'>":"<td>");
					} else {
						html += "<tr><td></td><td>";
					}
					html += e.getName();
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					String credit = null, note = null;
					int j = 0;
					for (int i = 0; i < e.getParameterCount(); i++) {
						if ("credit_note".equals(e.getParameter(i).getKey())) creditNote = e.getParameter(i).getValue();
						else if ("credit".equals(e.getParameter(i).getKey())) credit = e.getParameter(i).getValue();
						else if ("note".equals(e.getParameter(i).getKey())) note = e.getParameter(i).getValue();
						else {
							html += (j > 0 ? ", " : "") + e.getParameter(i).getValue();
							j++;
						}
					}
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					if (credit != null) html += credit;
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					if (note != null) html += note;
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					html += (idx == 0 && r.getCritical() ? MSG.opSetCritical() : idx == 0 && r.getImportant() ? MSG.opSetImportant() :
						idx == 0 && r.getVital() ? MSG.opSetVital() : "");
					html += (r.getPriority() > 0 && idx == 0 ? "</td><td class='top-border-dashed'>" : "</td><td>" );
					html += (idx == 0 && r.getWaitList() ? MSG.courseWaitListed() : idx == 0 && r.getNoSubs() ? MSG.courseNoSubs() : "");
					html += "</td></tr>";
					idx++;
				}
		}
		if (creditNote != null && !creditNote.isEmpty()) {
			html += "<tr><td class='top-border-dashed' colspan='7'>" + creditNote + "</td></tr>";	
		}
		if (!action.getRecommendationList().isEmpty()) {
			html += "</table></td></tr>";
		}
		
		for (OnlineSectioningLog.Enrollment e: action.getEnrollmentList()) {
			html += "<tr><td class='unitime-MainTableHeader' colspan='2'>"+ (e.hasType() ? Constants.toInitialCase(e.getType().name()) + " ": "") + MSG.enrollmentsTable() + "</td></tr>";
			html += "<tr><td colspan='2'><table cellspacing='0' cellpadding='2'>" +
					"<td class='unitime-TableHeader'>" + MSG.colCourse() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colSubpart() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colClass() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colDays() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colStart() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colEnd() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colDate() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colRoom() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colInstructor() + "</td>" +
					"<td class='unitime-TableHeader'>" + MSG.colEnrollmentTimeStamp() + "</td></tr>";
			for (OnlineSectioningLog.Section s: e.getSectionList()) {
				if (!s.hasCourse()) continue;
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
						"<td>" + s.getClazz().getName() + (s.getClazz().hasExternalId() && !s.getClazz().getExternalId().equals(s.getClazz().getName()) ? " - " + s.getClazz().getExternalId() : "") + "</td>" +
						(!s.hasTime() || s.getTime().getDays() == 0 ? "<td colspan='3'>" + MSG.arrangeHours() + "</td>"
						: "<td>" + (s.hasTime() ? DayCode.toString(s.getTime().getDays()) : MSG.arrangeHours()) + "</td>" +
						"<td>" + (s.hasTime() ? time(s.getTime().getStart()) : "") + "</td>" +
						"<td>" + (s.hasTime() ? time(s.getTime().getStart() + s.getTime().getLength()) : "") + "</td>") +
						"<td>" + (s.hasTime() && s.getTime().hasPattern() ? s.getTime().getPattern() : "") + "</td>" +
						"<td>" + loc + "</td>" +
						"<td>" + instr + "</td>" +
						"<td>" + (s.hasTimeStamp() ? rf.format(new Date(s.getTimeStamp())) : "") + "</td>" + 
						"</tr>";
			}
			html += "</table></td></tr>";
		}
		if (!action.getMessageList().isEmpty()) {
			html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.tableMessages() + "</td></tr>";
			for (OnlineSectioningLog.Message m: action.getMessageList()) {
				if (m.hasText()) {
					html += "<tr><td><b>" + m.getLevel().name() + ":</b></td><td>" + m.getText() + "</td></tr>";
				}
				if (m.hasException()) {
					html += "<tr><td><b>Exception:</b></td><td>" + m.getException() + "</td></tr>";
				}
			}
		}
		html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.tableProto() + "</td></tr>";
		html += "<tr><td colspan='2' class='proto'>" + action.toString().replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;").replace("\n", "<br>") + "</td></tr>";
		html += "</table>";
		return html;
	}
	
	protected static String getRequestMessage(OnlineSectioningLog.Action action) {
		String request = "";
		int notAlt = 0, lastFT = -1;
		for (OnlineSectioningLog.Request r: (action.getRequestCount() > 0 ? action.getRequestList() : action.getRecommendationList())) {
			if (!r.getAlternative()) notAlt = r.getPriority() + 1;
			int idx = 0;
			for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
				if (idx == 0) {
					request += (lastFT == r.getPriority() ? ", " : (request.isEmpty() ? "" : "\n") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". " + CONST.freePrefix() + " ");
				} else {
					request += ", ";
				}
				idx++;
				request += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
				lastFT = r.getPriority();
			}
			if (r.getFreeTimeList().isEmpty()) {
				for (OnlineSectioningLog.Entity e: r.getCourseList()) {
					if (idx == 0) {
						request += (request.isEmpty() ? "" : "\n") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". ";
					} else {
						request += ", ";
					}
					idx++;
					request += e.getName();
				}
				if (r.getWaitList()) request += " (w)";
				else if (r.getNoSubs()) request += " (s)";
			}
		}
		return request;
	}
	
	protected static String getSelectedMessage(OnlineSectioningLog.Action action) {
		String selected = "";
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			for (OnlineSectioningLog.Section s: r.getSectionList()) {
				if (s.getPreference() == OnlineSectioningLog.Section.Preference.SELECTED) {
					if (!selected.isEmpty()) selected += "\n";
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
		return selected;
	}
	
	protected static String getEnrollmentMessage(OnlineSectioningLog.Action action) {
		OnlineSectioningLog.Enrollment enrl = null;
		for (OnlineSectioningLog.Enrollment e: action.getEnrollmentList()) {
			enrl = e;
			if (e.getType() == OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED) break;
		}
		String enrollment = "";
		if (enrl != null)
			for (OnlineSectioningLog.Section s: enrl.getSectionList()) {
				if (!s.hasCourse()) continue;
				if (!enrollment.isEmpty()) enrollment += "\n";
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
				enrollment += s.getCourse().getName() + " " + s.getSubpart().getName() + " " + s.getClazz().getName() + " " +
					(s.hasTime() ? DayCode.toString(s.getTime().getDays()) + " " + time(s.getTime().getStart()) : "") + " " + loc;
			}
		return enrollment;
	}
	
	public static String getMessage(OnlineSectioningLog.Action action) {
		String message = "";
		int level = 1;
		for (OnlineSectioningLog.Message m: action.getMessageList()) {
			if (!m.hasLevel()) continue; // skip messages with no level
			if (!message.isEmpty() && level > m.getLevel().getNumber()) continue; // if we have a message, ignore messages with lower level
			if (m.hasText()) {
				message = (level != m.getLevel().getNumber() ? "" : message + "\n") + m.getText();
				level = m.getLevel().getNumber();
			} else if (m.hasException()) {
				message = (level != m.getLevel().getNumber() ? "" : message + "\n") + m.getException();
				level = m.getLevel().getNumber();
			}
		}
		if (action.hasResult() && OnlineSectioningLog.Action.ResultType.FAILURE.equals(action.getResult()) && !message.isEmpty()) {
			return message;
		} else if ("suggestions".equals(action.getOperation())) {
			String selected = getSelectedMessage(action);
			return (selected.isEmpty() ? message : selected);
		} if ("section".equals(action.getOperation())) {
			String request = getRequestMessage(action);
			return (request.isEmpty() ? message : request);
		} else {
			String enrollment = getEnrollmentMessage(action);
			if (!enrollment.isEmpty()) return enrollment;
			String request = getRequestMessage(action);
			return (request.isEmpty() ? message : request);
		}
	}

	protected static String time(int slot) {
        int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONST.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}

	public static class SectioningLogQueryFormatter implements QueryFormatter {
		Set<String> iGroupTypes = new HashSet<String>();
		AcademicSessionInfo iSession = null;
		OnlineSectioningHelper iHelper = null;
		
		public SectioningLogQueryFormatter(AcademicSessionInfo session, OnlineSectioningHelper helper) {
			iSession = session;
			iHelper = helper;
			for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll(helper.getHibSession()))
				iGroupTypes.add(type.getReference().replace(' ', '_').toLowerCase());
		}

		@Override
		public String format(String attr, String body) {
			if (body != null && !body.isEmpty())
				body = StringEscapeUtils.escapeSql(body);
			if ("id".equalsIgnoreCase(attr) || "student".equalsIgnoreCase(attr)) {
				if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue() && body.startsWith("0")) {
					return "s.externalUniqueId = '" + body.replaceFirst("^0+(?!$)", "") + "'";
				} else {
					return "s.externalUniqueId = '" + body + "'";
				}
			} else if ("advisor".equalsIgnoreCase(attr)) {
				if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue() && body.startsWith("0")) {
					return "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = '" + body.replaceFirst("^0+(?!$)", "") + "' and adv.session.uniqueId = s.session.uniqueId)";
				} else {
					return "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = '" + body + "' and adv.session.uniqueId = s.session.uniqueId)";
				}
			} else if ("operation".equalsIgnoreCase(attr) || "op".equalsIgnoreCase(attr)) {
				return "lower(l.operation) = '" + body.toLowerCase() + "'";
			} else if ("max-age".equalsIgnoreCase(attr) || "age".equalsIgnoreCase(attr)) {
				return HibernateUtil.addDate("l.timeStamp", body) + " > current_date()";
			} else if ("limit".equalsIgnoreCase(attr)) {
				return "1 = 1";
			} else if ("area".equalsIgnoreCase(attr)) {
				return "lower(m.academicArea.academicAreaAbbreviation) = '" + body.toLowerCase() + "'";
			} else if ("clasf".equalsIgnoreCase(attr) || "classification".equalsIgnoreCase(attr)) {
				return "lower(m.academicClassification.code) = '" + body.toLowerCase() + "'";
			} else if ("major".equalsIgnoreCase(attr)) {
				return "lower(m.major.code) = '" + body.toLowerCase() + "'";
			} else if ("concentration".equalsIgnoreCase(attr)) {
				return "lower(m.concentration.code) = '" + body.toLowerCase() + "'";
			} else if ("program".equalsIgnoreCase(attr)) {
				return "lower(m.program.reference) = '" + body.toLowerCase() + "'";
			} else if ("campus".equalsIgnoreCase(attr)) {
				return "lower(m.campus.reference) = '" + body.toLowerCase() + "'";
			} else if ("minor".equalsIgnoreCase(attr)) {
				return "lower(n.minor.code) = '" + body.toLowerCase() + "'";
			} else if ("group".equalsIgnoreCase(attr)) {
				return "lower(g.groupAbbreviation) = '" + body.toLowerCase() + "'";
			} else if (attr != null && iGroupTypes.contains(attr.toLowerCase())) {
				return "lower(G_" + attr + ".groupAbbreviation) = '" + body.toLowerCase() + "'";
			} else if ("accommodation".equalsIgnoreCase(attr)) {
				return "lower(a.abbreviation) = '" + body.toLowerCase() + "'";
			} else if ("user".equalsIgnoreCase(attr)) {
				return ("none".equalsIgnoreCase(body) ? "l.user is null" : "l.user = '" + body + "'");				
			} else if ("result".equalsIgnoreCase(attr)) {
				for (OnlineSectioningLog.Action.ResultType t: OnlineSectioningLog.Action.ResultType.values())
					if (t.name().equalsIgnoreCase(body))
						return "l.result = " + t.getNumber();
				if ("none".equalsIgnoreCase(body) || "unknown".equalsIgnoreCase(body))
					return "l.result is null";
				else
					return "1 = 1";
			} else if ("status".equalsIgnoreCase(attr)) {
				if ("Not Set".equalsIgnoreCase(body))
					return "s.sectioningStatus is null";
				else
					return "lower(s.sectioningStatus.reference) = '" + body.toLowerCase() + "'";
			} else if ("over".equalsIgnoreCase(attr)) {
				try {
					return "l.wallTime >= " + 1000 * Integer.parseInt(body.trim());
				} catch (Exception e) {
					return "1 = 1";
				}
			} else if ("under".equalsIgnoreCase(attr)) {
				try {
					return "l.wallTime <= " + 1000 * Integer.parseInt(body.trim());
				} catch (Exception e) {
					return "1 = 1";
				}
			} else if ("api".equalsIgnoreCase(attr)) {
				try {
					return "l.apiGetTime >= " + 1000 * Integer.parseInt(body.trim()) + "or l.apiPostTime >= " + 1000 * Integer.parseInt(body.trim()) + " or (l.apiGetTime + l.apiPostTime) >= " + 1000 * Integer.parseInt(body.trim());
				} catch (Exception e) {
					return "l.apiException like '%" + body + "%'";
				}
			} else if ("message".equalsIgnoreCase(attr)) {
				return "l.message like '%" + body + "%' or l.apiException like '%" + body + "%'";
			} else if ("get".equalsIgnoreCase(attr)) {
				try {
					return "l.apiGetTime >= " + 1000 * Integer.parseInt(body.trim());
				} catch (Exception e) {
					return "1 = 1";
				}
			} else if ("post".equalsIgnoreCase(attr)) {
				try {
					return "l.apiPostTime >= " + 1000 * Integer.parseInt(body.trim());
				} catch (Exception e) {
					return "1 = 1";
				}
			} else if ("course".equalsIgnoreCase(attr)) {
				return "cr.courseOffering.subjectAreaAbbv = '" + body + "' or (cr.courseOffering.subjectAreaAbbv || ' ' || cr.courseOffering.courseNbr) = '" + body + "'";
			} else if ("lookup".equalsIgnoreCase(attr)) {
				if (CustomCourseLookupHolder.hasProvider()) {
					Set<Long> courseIds = CustomCourseLookupHolder.getProvider().getCourseIds(iSession, iHelper.getHibSession(), body, true);
					if (courseIds != null && !courseIds.isEmpty()) {
						String ret = "";
						for (Long courseId: courseIds) {
							ret += (ret.isEmpty() ? "" : ",") + courseId; 
						}
						return "cr.courseOffering.uniqueId in (" + ret + ")";
					}
				}
				return "1 = 1";
			} else if ("im".equalsIgnoreCase(attr)) {
				if (body != null && body.equals(iSession.getDefaultInstructionalMethod())) {
					return "im is null or im.reference = '" + body + "'";
				} else {
					return "im.reference = '" + body + "'";
				}
			} else if (attr == null && !body.isEmpty()) {
				return "lower(s.firstName || ' ' || s.middleName || ' ' || s.lastName) like '%" + body.toLowerCase() + "%'";
			} else {
				return "1 = 1";
			}
		}
		
		public Collection<String> getGroupTypes() { return iGroupTypes; }
		
	}

	@Override
	public String name() {
		return "sectioning-log";
	}
}
