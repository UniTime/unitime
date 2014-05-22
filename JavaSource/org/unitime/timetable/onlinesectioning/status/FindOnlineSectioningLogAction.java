/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.status;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.QueryFormatter;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XAcademicAreaCode;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.util.Constants;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author Tomas Muller
 */
public class FindOnlineSectioningLogAction implements OnlineSectioningAction<List<SectioningAction>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	
	private Query iQuery;
	private Integer iLimit = 100;
	
	public FindOnlineSectioningLogAction forQuery(String query) {
		iQuery = new Query(query.isEmpty() ? "limit:100" : query);
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
		return this;
	}
	
	public Query getQuery() { return iQuery; }
	
	public Integer getLimit() { return iLimit; }

	@Override
	public List<SectioningAction> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			List<SectioningAction> ret = new ArrayList<SectioningAction>();
			DateFormat df = Localization.getDateFormat(CONST.timeStampFormat());
			NumberFormat nf = Localization.getNumberFormat("0.00");
			
			org.hibernate.Query q = helper.getHibSession().createQuery(
					"select l, s.uniqueId from OnlineSectioningLog l, Student s " +
					(getQuery().hasAttribute("area", "clasf", "classification") ? "left outer join s.academicAreaClassifications a " : "") +
					(getQuery().hasAttribute("major") ? "left outer join s.posMajors m " : "") + 
					(getQuery().hasAttribute("minor") ? "left outer join s.posMinors n " : "") + 
					(getQuery().hasAttribute("group") ? "left outer join s.groups g " : "") + 
					(getQuery().hasAttribute("accommodation") ? "left outer join s.accomodations a " : "") + 
					"where l.session.uniqueId = :sessionId and l.session = s.session and l.student = s.externalUniqueId " +
					"and (" + getQuery().toString(new SectioningLogQueryFormatter()) + ") " +
					"and (l.result is not null or l.operation not in ('reload-offering', 'check-offering')) order by l.timeStamp desc, l.uniqueId desc");

			q.setLong("sessionId", server.getAcademicSession().getUniqueId());
			if (getLimit() != null)
				q.setMaxResults(getLimit());
			
			for (Object[] o: (List<Object[]>)q.list()) {
				try {
					org.unitime.timetable.model.OnlineSectioningLog log = (org.unitime.timetable.model.OnlineSectioningLog)o[0];
					
					XStudent student = server.getStudent((Long)o[1]);
					if (student == null) continue;
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
					st.setId(student.getStudentId());
					st.setExternalId(student.getExternalId());
					st.setName(student.getName());
					for (XAcademicAreaCode ac: student.getAcademicAreaClasiffications()) {
						st.addArea(ac.getArea());
						st.addClassification(ac.getCode());
					}
					for (XAcademicAreaCode ac: student.getMajors()) {
						st.addMajor(ac.getCode());
					}
					for (String acc: student.getAccomodations()) {
						st.addAccommodation(acc);
					}
					for (String gr: student.getGroups()) {
						st.addGroup(gr);
					}

					SectioningAction a = new SectioningAction();
					a.setStudent(st);
					a.setTimeStamp(log.getTimeStamp());
					a.setOperation(Constants.toInitialCase(log.getOperation().replace('-', ' ')));
					OnlineSectioningLog.Action action = OnlineSectioningLog.Action.parseFrom(log.getAction());
					if (action.hasCpuTime())
						a.setCpuTime(action.getCpuTime());
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
					if (action.hasStudent()) {
						XStudent s = server.getStudent(action.getStudent().getUniqueId());
						if (s != null) {
							html += "<tr><td><b>" + MSG.colStudent() + ":</b></td><td>" + s.getName() + "</td></tr>";
						}
					}
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
					
					if (!action.getRequestList().isEmpty()) {
						html += "<tr><td class='unitime-MainTableHeader' colspan='2'>" + MSG.courseRequestsCourses() + "</td></tr>";
						
					}
					String request = "";
					String selected = "";
					for (OnlineSectioningLog.Request r: action.getRequestList()) {
						if (!request.isEmpty()) request += "<br>";
						request += (r.getAlternative() ? "A" : "") + (1 + r.getPriority()) + ". ";
						html += "<tr><td colspan='2'>" + (r.getAlternative() ? "A" : "") + (1 + r.getPriority()) + ". ";
						int idx = 0;
						for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
							if (idx ++ > 0) { html += ", "; request += ", "; }
							else { html += CONST.freePrefix() + " "; request += CONST.freePrefix() + " "; }
							html += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
							request += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
						}
						if (r.getFreeTimeList().isEmpty())
							for (OnlineSectioningLog.Entity e: r.getCourseList()) {
								if (idx ++ > 0) { html += ", "; request += ", "; }
								html += e.getName();
								request += e.getName();
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

	private String time(int slot) {
        int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONST.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}

	public static class SectioningLogQueryFormatter implements QueryFormatter {

		@Override
		public String format(String attr, String body) {
			if ("id".equalsIgnoreCase(attr) || "student".equalsIgnoreCase(attr)) {
				return "s.externalUniqueId = '" + body + "'";
			} else if ("operation".equalsIgnoreCase(attr) || "op".equalsIgnoreCase(attr)) {
				return "lower(l.operation) = '" + body.toLowerCase() + "'";
			} else if ("max-age".equalsIgnoreCase(attr) || "age".equalsIgnoreCase(attr)) {
				return HibernateUtil.addDate("l.timeStamp", body) + " > current_date()";
			} else if ("limit".equalsIgnoreCase(attr)) {
				return "1 = 1";
			} else if ("area".equalsIgnoreCase(attr)) {
				return "lower(a.academicArea.academicAreaAbbreviation) = '" + body.toLowerCase() + "'";
			} else if ("clasf".equalsIgnoreCase(attr) || "classification".equalsIgnoreCase(attr)) {
				return "lower(a.academicClassification.code) = '" + body.toLowerCase() + "'";
			} else if ("major".equalsIgnoreCase(attr)) {
				return "lower(m.code) = '" + body.toLowerCase() + "'";
			} else if ("minor".equalsIgnoreCase(attr)) {
				return "lower(n.code) = '" + body.toLowerCase() + "'";
			} else if ("group".equalsIgnoreCase(attr)) {
				return "lower(g.groupAbbreviation) = '" + body.toLowerCase() + "'";
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
					return "s.sectioningStatus.reference = '" + body.toLowerCase() + "'";
			} else if (!body.isEmpty()) {
				return "lower(s.firstName || ' ' || s.middleName || ' ' || s.lastName) like '%" + body.toLowerCase() + "%'";
			} else {
				return "1 = 1";
			}
		}
		
	}

	@Override
	public String name() {
		return "sectioning-log";
	}
}
