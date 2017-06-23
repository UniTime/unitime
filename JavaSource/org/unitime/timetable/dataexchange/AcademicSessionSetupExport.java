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
package org.unitime.timetable.dataexchange;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class AcademicSessionSetupExport extends BaseExport {
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
    protected static Formats.Format<Number> sFloatFormat = Formats.getNumberFormat("0.000");
    
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("sessionSetup");
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("campus", session.getAcademicInitiative());
			root.addAttribute("dateFormat", sDateFormat.toPattern());
	        root.addAttribute("created", new Date().toString());
	        
	        exportSession(root, session);
	        exportManagers(root, session);
	        exportDepartments(root, session);
	        exportSubjectAreas(root, session);
	        exportSolverGroups(root, session);
	        exportDatePatterns(root, session);
	        exportTimePatterns(root, session);
	        exportExaminationPeriods(root, session);
	        exportAcademicAreas(root, session);
	        exportAcademicClassifications(root, session);
	        exportMajors(root, session);
	        exportMinors(root, session);
	        exportStudentGroups(root, session);
	        exportStudentAccomodations(root, session);
	        
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected void exportSession(Element root, Session session) {
		Element sessionEl = root.addElement("session");
		sessionEl.addAttribute("startDate", sDateFormat.format(session.getSessionBeginDateTime()));
		sessionEl.addAttribute("endDate", sDateFormat.format(session.getSessionEndDateTime()));
		sessionEl.addAttribute("classEndDate", sDateFormat.format(session.getClassesEndDateTime()));
		sessionEl.addAttribute("examStartDate", sDateFormat.format(session.getExamBeginDate()));
		sessionEl.addAttribute("eventStartDate", sDateFormat.format(session.getEventBeginDate()));
		sessionEl.addAttribute("eventEndDate", sDateFormat.format(session.getEventEndDate()));
		
		Element holidaysEl = sessionEl.addElement("holidays");
		int acadYear = session.getSessionStartYear(); 
        int startMonth = DateUtils.getStartMonth(session.getEventBeginDate() != null && session.getEventBeginDate().before(session.getSessionBeginDateTime()) ? session.getEventBeginDate() : session.getSessionBeginDateTime(), acadYear, ApplicationProperty.SessionNrExcessDays.intValue());
		int endMonth = DateUtils.getEndMonth(session.getEventEndDate() != null && session.getEventEndDate().after(session.getSessionEndDateTime()) ? session.getEventEndDate() : session.getSessionEndDateTime(), acadYear, ApplicationProperty.SessionNrExcessDays.intValue()); 
		
		Date firstBreak = null;
		Date yesterday = null;
		for (int m = startMonth; m <= endMonth; m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, acadYear);
			for (int d = 1; d <= daysOfMonth; d++) {
				Date today = DateUtils.getDate(d, m, acadYear);
				int holiday = Session.getHoliday(d, m, acadYear, startMonth, session.getHolidays());
				if (holiday == Session.sHolidayTypeHoliday) {
					holidaysEl.addElement("holiday").addAttribute("date", sDateFormat.format(today));
				} else if (holiday == Session.sHolidayTypeBreak) {
					if (firstBreak == null) firstBreak = today;
				}
				if (firstBreak != null && holiday != Session.sHolidayTypeBreak) {
					holidaysEl.addElement("break").addAttribute("startDate", sDateFormat.format(firstBreak)).addAttribute("endDate", sDateFormat.format(yesterday));
					firstBreak = null;
				}
				yesterday = today;
			}
		}
		if (firstBreak != null)
			holidaysEl.addElement("break").addAttribute("startDate", sDateFormat.format(firstBreak)).addAttribute("endDate", sDateFormat.format(yesterday));
	}
	
	protected void exportManagers(Element root, Session session) {
		Element managersEl = root.addElement("managers");
		for (TimetableManager m: new TreeSet<TimetableManager>((List<TimetableManager>)getHibSession().createQuery(
				"select distinct m from TimetableManager m inner join m.departments d where d.session.uniqueId = :sessionId"
				).setLong("sessionId", session.getUniqueId()).list())) {
			Element managerEl = managersEl.addElement("manager");
			managerEl.addAttribute("externalId", m.getExternalUniqueId());
			if (m.getFirstName() != null)
				managerEl.addAttribute("firstName", m.getFirstName());
			if (m.getMiddleName() != null)
				managerEl.addAttribute("middleName", m.getMiddleName());
			if (m.getLastName() != null)
				managerEl.addAttribute("lastName", m.getLastName());
			if (m.getAcademicTitle() != null)
				managerEl.addAttribute("acadTitle", m.getAcademicTitle());
			if (m.getEmailAddress() != null)
				managerEl.addAttribute("email", m.getEmailAddress());
			if (m.getFirstName() != null)
				managerEl.addAttribute("firstName", m.getFirstName());
			for (Department d: m.getDepartments()) {
				if (session.equals(d.getSession()))
					managerEl.addElement("department").addAttribute("code", d.getDeptCode());
			}
			for (ManagerRole r: m.getManagerRoles()) {
				Element roleEl = managerEl.addElement("role");
				roleEl.addAttribute("reference", r.getRole().getReference());
				if (r.getPrimary() != null && r.getPrimary().booleanValue())
					roleEl.addAttribute("primary", "true");
				if (r.getReceiveEmails() != null)
					roleEl.addAttribute("emails", r.getReceiveEmails() ? "true" : "false");
			}
		}
	}
	
	protected void exportDepartments(Element root, Session session) {
		Element departmentsEl = root.addElement("departments");
		for (Department d: new TreeSet<Department>(session.getDepartments())) {
			Element departmentEl = departmentsEl.addElement("department");
			departmentEl.addAttribute("code", d.getDeptCode());
			if (d.getExternalUniqueId() != null)
				departmentEl.addAttribute("externalId", d.getExternalUniqueId());
			if (d.getAbbreviation() != null)
				departmentEl.addAttribute("abbreviation", d.getAbbreviation());
			if (d.getName() != null)
				departmentEl.addAttribute("name", d.getName());
			boolean external = (d.getExternalManager() != null && d.getExternalManager().booleanValue()); 
			if (external) {
				departmentEl.addElement("externalManager").addAttribute("enabled", "true").addAttribute("abbreviation", d.getExternalMgrAbbv()).addAttribute("label", d.getExternalMgrLabel());
			}
			if (d.getAllowEvents() && d.getAllowEvents().booleanValue())
				departmentEl.addElement("eventManagement").addAttribute("enabled", "true");
			if (d.getAllowStudentScheduling() && !d.getAllowStudentScheduling().booleanValue())
				departmentEl.addElement("studentScheduling").addAttribute("enabled", "false");
			boolean reqTime = d.getAllowReqTime() && d.getAllowReqTime().booleanValue();
			boolean reqRoom = d.getAllowReqRoom() && d.getAllowReqRoom().booleanValue();
			boolean reqDist = d.getAllowReqDistribution() && d.getAllowReqDistribution().booleanValue();
			if (reqTime || reqRoom || reqDist) {  
				departmentEl.addElement("required")
					.addAttribute("time", reqTime ? "true" : "false")
					.addAttribute("room", reqRoom ? "true" : "false")
					.addAttribute("distribution", reqDist ? "true" : "false");
			}
			boolean inheritInstrPref = d.getInheritInstructorPreferences() && d.getInheritInstructorPreferences().booleanValue(); 
			if (external == inheritInstrPref)
				departmentEl.addElement("instructorPreferences").addAttribute("inherit", inheritInstrPref ? "true" : "false");
			if (d.getDistributionPrefPriority() != null && d.getDistributionPrefPriority() != 0)
				departmentEl.addElement("distributionPreferences").addAttribute("priority", d.getDistributionPrefPriority().toString());
		}
	}
	
	protected void exportSubjectAreas(Element root, Session session) {
		Element subjectsEl = root.addElement("subjectAreas");
		for (SubjectArea s: new TreeSet<SubjectArea>(session.getSubjectAreas())) {
			Element subjectEl = subjectsEl.addElement("subjectArea");
			subjectEl.addAttribute("abbreviation", s.getSubjectAreaAbbreviation());
			if (s.getTitle() != null)
				subjectEl.addAttribute("title", s.getTitle());
			if (s.getExternalUniqueId() != null)
				subjectEl.addAttribute("externalId", s.getExternalUniqueId());
			subjectEl.addAttribute("department", s.getDepartment().getDeptCode());
		}
	}
	
	protected void exportSolverGroups(Element root, Session session) {
		Element groupsEl = root.addElement("solverGroups");
		for (SolverGroup g: new TreeSet<SolverGroup>((List<SolverGroup>)getHibSession().createQuery(
				"from SolverGroup where session = :sessionId").setLong("sessionId", session.getUniqueId()).list())) {
			Element groupEl = groupsEl.addElement("solverGroup");
			groupEl.addAttribute("abbreviation", g.getAbbv());
			if (g.getName() != null)
				groupEl.addAttribute("name", g.getName());
			for (TimetableManager m: g.getTimetableManagers())
				groupEl.addElement("manager").addAttribute("externalId", m.getExternalUniqueId());
			for (Department d: g.getDepartments())
				groupEl.addElement("department").addAttribute("code", d.getDeptCode());
		}
	}
	
	protected void exportDatePatterns(Element root, Session session) {
		Element patternsEl = root.addElement("datePatterns");
		for (DatePattern dp: new TreeSet<DatePattern>((List<DatePattern>)getHibSession().createQuery(
				"from DatePattern where session = :sessionId").setLong("sessionId", session.getUniqueId()).list())) {
			Element patternEl = patternsEl.addElement("datePattern");			
			patternEl.addAttribute("name", dp.getName());
			patternEl.addAttribute("type", DatePattern.sTypes[dp.getType()]);
			patternEl.addAttribute("visible", dp.getVisible() != null && dp.getVisible().booleanValue() ? "true": "false");
			patternEl.addAttribute("default", dp.isDefault() ? "true" : "false");
			if (dp.getNumberOfWeeks() != null)
				patternEl.addAttribute("nbrWeeks", sFloatFormat.format(dp.getNumberOfWeeks()));
			for (Department d: dp.getDepartments())
				patternEl.addElement("department").addAttribute("code", d.getDeptCode());
			if (dp.getType() == DatePattern.sTypePatternSet) {
				for (DatePattern p: dp.findChildren(getHibSession()))
					patternEl.addElement("datePattern").addAttribute("name", p.getName());
				continue;
			}
			
			int startMonth = session.getPatternStartMonth();
			int endMonth = session.getPatternEndMonth();
			int year = session.getSessionStartYear();
			Date firstDate = null;
			Date yesterday = null;
			for (int m = startMonth; m <= endMonth; m++) {
				int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
				for (int d = 1; d <= daysOfMonth; d++) {
					Date today = DateUtils.getDate(d, m, year);
					if (dp.isOffered(d, m)) {
						if (firstDate == null) firstDate = today;
					} else {
						if (firstDate != null) {
							patternEl.addElement("dates").addAttribute("fromDate", sDateFormat.format(firstDate)).addAttribute("toDate", sDateFormat.format(yesterday));
							firstDate = null;
						}
					}
					yesterday = today;
				}
			}
			if (firstDate != null)
				patternEl.addElement("dates").addAttribute("fromDate", sDateFormat.format(firstDate)).addAttribute("toDate", sDateFormat.format(yesterday));
		}
	}
	
	private static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }
	
    private static String dayCode2days(int dayCode) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((dayCode & Constants.DAY_CODES[i])!=0)
                sb.append(Constants.DAY_NAMES_SHORT[i]);
        return sb.toString(); 
    }
	
	protected void exportTimePatterns(Element root, Session session) {
		Element patternsEl = root.addElement("timePatterns");
		for (TimePattern tp: new TreeSet<TimePattern>((List<TimePattern>)getHibSession().createQuery(
				"from TimePattern where session = :sessionId").setLong("sessionId", session.getUniqueId()).list())) {
			Element patternEl = patternsEl.addElement("timePattern");
			patternEl.addAttribute("name", tp.getName());
			patternEl.addAttribute("nbrMeetings", tp.getNrMeetings().toString());
			patternEl.addAttribute("minsPerMeeting", tp.getMinPerMtg().toString());
			patternEl.addAttribute("type", TimePattern.sTypes[tp.getType()]);
			patternEl.addAttribute("visible", tp.getVisible() != null && tp.getVisible().booleanValue() ? "true" : "false");
			patternEl.addAttribute("nbrSlotsPerMeeting", tp.getSlotsPerMtg().toString());
			patternEl.addAttribute("breakTime", tp.getBreakTime() == null ? "0" : tp.getBreakTime().toString());
			for (Department d: tp.getDepartments())
				patternEl.addElement("department").addAttribute("code", d.getDeptCode());
			for (TimePatternDays days: new TreeSet<TimePatternDays>(tp.getDays())) {
				patternEl.addElement("days").addAttribute("code", dayCode2days(days.getDayCode()));
			}
			for (TimePatternTime time: new TreeSet<TimePatternTime>(tp.getTimes())) {
				patternEl.addElement("time").addAttribute("start", startSlot2startTime(time.getStartSlot()));
			}
		}
	}
	
	protected void exportExaminationPeriods(Element root, Session session) {
		Element examinationPeriodsEl = root.addElement("examinationPeriods");
		for (ExamType type: new TreeSet<ExamType>(getHibSession().createQuery(
				"select distinct p.examType from ExamPeriod p where p.session.uniqueId = :sessionId")
				.setLong("sessionId", session.getUniqueId()).list())) {
			Element typeEl = examinationPeriodsEl.addElement("periods");
			typeEl.addAttribute("type", type.getReference());
			for (ExamPeriod period: new TreeSet<ExamPeriod>(getHibSession().createQuery(
					"from ExamPeriod p where p.session.uniqueId = :sessionId and p.examType.uniqueId = :typeId")
					.setLong("sessionId", session.getUniqueId()).setLong("typeId", type.getUniqueId()).list())) {
				Element periodEl = typeEl.addElement("period");
				periodEl.addAttribute("date", sDateFormat.format(period.getStartDate()));
				periodEl.addAttribute("startTime", startSlot2startTime(period.getStartSlot()));
				periodEl.addAttribute("length", String.valueOf(5 * period.getLength()));
				if (period.getEventStartOffset() != null && period.getEventStartOffset() != 0)
					periodEl.addAttribute("eventStartOffset", period.getEventStartOffset().toString());
				if (period.getEventStopOffset() != null && period.getEventStopOffset() != 0)
					periodEl.addAttribute("eventStopOffset", period.getEventStopOffset().toString());
				if (period.getPrefLevel() != null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog()))
					periodEl.addAttribute("preference", period.getPrefLevel().getPrefProlog());
			}
		}
	}
	
	protected void exportAcademicAreas(Element root, Session session) {
		Element areasEl = root.addElement("academicAreas");
		for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
				"from AcademicArea where session = :sessionId order by academicAreaAbbreviation").setLong("sessionId", session.getUniqueId()).list()) {
			Element areaEl = areasEl.addElement("academicArea");
			if (area.getExternalUniqueId() != null)
				areaEl.addAttribute("externalId", area.getExternalUniqueId());
			if (area.getAcademicAreaAbbreviation() != null)
				areaEl.addAttribute("abbreviation", area.getAcademicAreaAbbreviation());
			if (area.getTitle() != null)
				areaEl.addAttribute("title", area.getTitle());
		}
	}
	
	protected void exportAcademicClassifications(Element root, Session session) {
		Element clasfsEl = root.addElement("academicClassifications");
		for (AcademicClassification clasf: (List<AcademicClassification>)getHibSession().createQuery(
				"from AcademicClassification where session = :sessionId order by code").setLong("sessionId", session.getUniqueId()).list()) {
			Element clasfEl = clasfsEl.addElement("academicClassification");
			if (clasf.getExternalUniqueId() != null)
				clasfEl.addAttribute("externalId", clasf.getExternalUniqueId());
			if (clasf.getCode() != null)
				clasfEl.addAttribute("code", clasf.getCode());
			if (clasf.getName() != null)
				clasfEl.addAttribute("name", clasf.getName());
		}
	}
	
	protected void exportMajors(Element root, Session session) {
		Element majorsEl = root.addElement("posMajors");
		for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
				"from AcademicArea where session = :sessionId order by academicAreaAbbreviation").setLong("sessionId", session.getUniqueId()).list()) {
			for (PosMajor major: area.getPosMajors()) {
				Element majorEl = majorsEl.addElement("posMajor");
				majorEl.addAttribute("code", major.getCode());
				majorEl.addAttribute("academicArea", area.getAcademicAreaAbbreviation());
				if (major.getExternalUniqueId() != null)
					majorEl.addAttribute("externalId", major.getExternalUniqueId());
				if (major.getName() != null)
					majorEl.addAttribute("name", major.getName());
			}
		}
	}
	
	protected void exportMinors(Element root, Session session) {
		Element minorsEl = root.addElement("posMinors");
		for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
				"from AcademicArea where session = :sessionId order by academicAreaAbbreviation").setLong("sessionId", session.getUniqueId()).list()) {
			for (PosMinor minor: area.getPosMinors()) {
				Element minorEl = minorsEl.addElement("posMinor");
				minorEl.addAttribute("code", minor.getCode());
				minorEl.addAttribute("academicArea", area.getAcademicAreaAbbreviation());
				if (minor.getExternalUniqueId() != null)
					minorEl.addAttribute("externalId", minor.getExternalUniqueId());
				if (minor.getName() != null)
					minorEl.addAttribute("name", minor.getName());
			}
		}
	}
	
	protected void exportStudentGroups(Element root, Session session) {
		Element groupsEl = root.addElement("studentGroups");
		for (StudentGroup group: (List<StudentGroup>)getHibSession().createQuery(
				"from StudentGroup where session = :sessionId order by groupAbbreviation").setLong("sessionId", session.getUniqueId()).list()) {
			Element groupEl = groupsEl.addElement("studentGroup");
			if (group.getExternalUniqueId() != null)
				groupEl.addAttribute("externalId", group.getExternalUniqueId());
			if (group.getGroupAbbreviation() != null)
				groupEl.addAttribute("code", group.getGroupAbbreviation());
			if (group.getGroupName() != null)
				groupEl.addAttribute("name", group.getGroupName());
		}
	}
	
	protected void exportStudentAccomodations(Element root, Session session) {
		Element accomodationsEl = root.addElement("studentAccomodations");
		for (StudentAccomodation acc: (List<StudentAccomodation>)getHibSession().createQuery(
				"from StudentAccomodation where session = :sessionId order by abbreviation").setLong("sessionId", session.getUniqueId()).list()) {
			Element accomodationEl = accomodationsEl.addElement("studentAccomodation");
			if (acc.getExternalUniqueId() != null)
				accomodationEl.addAttribute("externalId", acc.getExternalUniqueId());
			if (acc.getAbbreviation() != null)
				accomodationEl.addAttribute("code", acc.getAbbreviation());
			if (acc.getName() != null)
				accomodationEl.addAttribute("name", acc.getName());
		}
	}
}
