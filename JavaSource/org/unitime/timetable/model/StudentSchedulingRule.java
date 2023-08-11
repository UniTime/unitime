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
package org.unitime.timetable.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.model.base.BaseStudentSchedulingRule;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.StudentSchedulingRuleDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XInstructionalMethod;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbStudentMatcher;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "std_sched_rules")
public class StudentSchedulingRule extends BaseStudentSchedulingRule {
	private static final long serialVersionUID = -5483870159301335999L;
	
	public static enum Mode {
		Filter,
		Online,
		Batch,
		;
	}
	

	transient Query iQuery = null;
	@Transient
	public Query getStudentQuery() {
		if (iQuery == null)
			iQuery = new Query(getStudentFilter());
		return iQuery;
	}
	
	public boolean matchSession(String term, String year, String initiative) {
		// check academic term
		if (getFilterTerm() != null && !getFilterTerm().isEmpty() && !term.matches(getFilterTerm())) return false;
		// check academic initiative / campus
		if (getFilterInitiative() != null && !getFilterInitiative().isEmpty() && !initiative.matches(getFilterInitiative())) return false;
		// check academic year
		if (getFirstYear() != null && Integer.parseInt(year) < getFirstYear()) return false;
		if (getLastYear() != null && Integer.parseInt(year) > getLastYear()) return false;
		return true;
	}
	
	public boolean matchSession(AcademicSessionInfo info) {
		return matchSession(info.getTerm(), info.getYear(), info.getCampus());
	}
	
	public boolean matchSession(Session session) {
		return matchSession(session.getAcademicTerm(), session.getAcademicYear(), session.getAcademicInitiative());
	}
	
	
	public boolean matchesInstructionalMethod(String im) {
		// not set > always matches
		if (getInstructonalMethod() == null || getInstructonalMethod().isEmpty()) return true;
		if (getInstructonalMethod().equals("-")) {
			return im == null || im.isEmpty();
		}
		if (getInstructonalMethod().startsWith("!")) {
			return im != null && !im.matches(getInstructonalMethod().substring(1));
		}
		return im != null && im.matches(getInstructonalMethod());
	}
	
	public boolean matchesInstructionalMethod(InstructionalMethod im) {
		return matchesInstructionalMethod(im == null ? null : im.getReference());
	}
	
	public boolean matchesInstructionalMethod(XInstructionalMethod im) {
		return matchesInstructionalMethod(im == null ? null : im.getReference());
	}
	
	public boolean matchesCourseName(String cn) {
		// not set > always matches
		if (getCourseName() == null || getCourseName().isEmpty()) return true;
		if (getCourseName().startsWith("!")) {
			return cn != null && !cn.matches(getCourseName().substring(1));
		}
		return cn != null && cn.matches(getCourseName());
	}

	public static StudentSchedulingRule getRule(TermMatcher studentMatcher, AcademicSessionInfo info, boolean isAdvisor, boolean isAdmin, Mode mode, org.hibernate.Session hibSession) {
		for (StudentSchedulingRule rule: hibSession.createQuery(
				"from StudentSchedulingRule order by ord", StudentSchedulingRule.class).setCacheable(true).list()) {
			if (rule.isAdvisorOverride() && isAdvisor) continue; // skip when advisor (and advisor override is on)
			if (rule.isAdminOverride() && isAdmin) continue; // skip when admin (and admin override is on)
			// check mode
			if (mode == Mode.Filter && !rule.isAppliesToFilter()) continue;
			if (mode == Mode.Online && !rule.isAppliesToOnline()) continue;
			if (mode == Mode.Batch && !rule.isAppliesToBatch()) continue;
			// check academic session
			if (!rule.matchSession(info)) continue;
			// check student filter
			if (rule.getStudentFilter() != null && !rule.getStudentFilter().isEmpty() && !rule.getStudentQuery().match(studentMatcher)) continue;
			// return the first matching rule
			return rule;
		}
		return null;
	}
	
	public static boolean hasRules(AcademicSessionInfo info, boolean isAdvisor, boolean isAdmin, Mode mode, org.hibernate.Session hibSession) {
		for (StudentSchedulingRule rule: hibSession.createQuery(
				"from StudentSchedulingRule order by ord", StudentSchedulingRule.class).setCacheable(true).list()) {
			if (rule.isAdvisorOverride() && isAdvisor) continue; // skip when advisor (and advisor override is on)
			if (rule.isAdminOverride() && isAdmin) continue; // skip when admin (and admin override is on)
			// check mode
			if (mode == Mode.Filter && !rule.isAppliesToFilter()) continue;
			if (mode == Mode.Online && !rule.isAppliesToOnline()) continue;
			if (mode == Mode.Batch && !rule.isAppliesToBatch()) continue;
			// check academic session
			if (!rule.matchSession(info)) continue;
			// matching rule found -> return true
			return true;
		}
		return false;
	}
	
	public static StudentSchedulingRule getRule(Long studentId, boolean isAdvisor, boolean isAdmin, Mode mode, org.hibernate.Session hibSession) {
		Student student = StudentDAO.getInstance().get(studentId, hibSession);
		if (student == null) return null;
		TermMatcher studentMatcher = new DbStudentMatcher(student);
		for (StudentSchedulingRule rule: hibSession.createQuery(
				"from StudentSchedulingRule order by ord", StudentSchedulingRule.class).setCacheable(true).list()) {
			if (rule.isAdvisorOverride() && isAdvisor) continue; // skip when advisor (and advisor override is on)
			if (rule.isAdminOverride() && isAdmin) continue; // skip when admin (and admin override is on)
			// check mode
			if (mode == Mode.Filter && !rule.isAppliesToFilter()) continue;
			if (mode == Mode.Online && !rule.isAppliesToOnline()) continue;
			if (mode == Mode.Batch && !rule.isAppliesToBatch()) continue;
			// check academic session
			if (!rule.matchSession(student.getSession())) continue;
			// matching rule found -> return true
			// check student filter
			if (rule.getStudentFilter() != null && !rule.getStudentFilter().isEmpty() && !rule.getStudentQuery().match(studentMatcher)) continue;
			// return the first matching rule
			return rule;
		}
		return null;
	}
	
	public static boolean hasFilterRules(OnlineSectioningServer server, SessionContext context) {
		return hasRules(
				server.getAcademicSession(),
				context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdvisor),
				context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdmin),
				Mode.Filter,
				StudentSchedulingRuleDAO.getInstance().getSession());
	}
	
	public static StudentSchedulingRule getRuleFilter(XStudent student, OnlineSectioningServer server, SessionContext context) {
		return getRule(
				new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
				server.getAcademicSession(),
				context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdvisor),
				context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdmin),
				Mode.Filter,
				StudentSchedulingRuleDAO.getInstance().getSession());
	}
	
	public static StudentSchedulingRule getRuleFilter(Student student, OnlineSectioningServer server, SessionContext context) {
		AcademicSessionInfo session = (server == null ? new AcademicSessionInfo(student.getSession()) : server.getAcademicSession());
		return getRule(
				new DbStudentMatcher(student),
				session,
				context.hasPermissionAnySession(session, Right.StudentSchedulingAdvisor),
				context.hasPermissionAnySession(session, Right.StudentSchedulingAdmin),
				Mode.Filter,
				StudentSchedulingRuleDAO.getInstance().getSession());
	}
	
	public static StudentSchedulingRule getRuleFilter(Long studentId, OnlineSectioningServer server, SessionContext context) {
		if (server != null && !(server instanceof DatabaseServer)) {
			XStudent student = server.getStudent(studentId);
			if (student == null) return null;
			return getRule(
					new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
					server.getAcademicSession(),
					context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdvisor),
					context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdmin),
					Mode.Filter,
					StudentSchedulingRuleDAO.getInstance().getSession());
		} else {
			Student student = StudentDAO.getInstance().get(studentId);
			if (student == null) return null;
			AcademicSessionInfo session = new AcademicSessionInfo(student.getSession());
			return getRule(
					new DbStudentMatcher(student),
					session,
					context.hasPermissionAnySession(session, Right.StudentSchedulingAdvisor),
					context.hasPermissionAnySession(session, Right.StudentSchedulingAdmin),
					Mode.Filter,
					StudentSchedulingRuleDAO.getInstance().getSession());
		}
	}
	
	public static StudentSchedulingRule getRuleFilter(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return getRule(
				new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
				server.getAcademicSession(),
				helper.hasAvisorPermission(),
				helper.hasAdminPermission(),
				Mode.Filter,
				helper.getHibSession());
	}
	
	public static StudentSchedulingRule getRuleFilter(Student student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		AcademicSessionInfo session = (server == null ? new AcademicSessionInfo(student.getSession()) : server.getAcademicSession());
		return getRule(
				new DbStudentMatcher(student),
				session,
				helper.hasAvisorPermission(),
				helper.hasAdminPermission(),
				Mode.Filter,
				helper.getHibSession());
	}
	
	public static StudentSchedulingRule getRuleOnline(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return getRule(
				new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
				server.getAcademicSession(),
				helper.hasAvisorPermission(), helper.hasAdminPermission(),
				Mode.Online,
				helper.getHibSession());
	}
	
	public static StudentSchedulingRule getRuleOnline(XStudent student, OnlineSectioningServer server, org.hibernate.Session hibSession) {
		return getRule(
				new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
				server.getAcademicSession(),
				false, false,
				Mode.Online,
				hibSession);
	}
}
