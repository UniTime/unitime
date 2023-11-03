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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.StudentSchedulingRule.Mode;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

/**
 * @author Tomas Muller
 */
public class XSchedulingRules implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private List<XSchedulingRule> iRules = new ArrayList<XSchedulingRule>();

	public XSchedulingRules(AcademicSessionInfo session, org.hibernate.Session hibSession) {
		for (StudentSchedulingRule rule: (List<StudentSchedulingRule>)hibSession.createQuery(
				"from StudentSchedulingRule order by ord").setCacheable(true).list()) {
			if (rule.matchSession(session))
				iRules.add(new XSchedulingRule(rule));
		}
	}
	
	public List<XSchedulingRule> getRules() { return iRules; }

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(iRules.size());
		for (XSchedulingRule rule: iRules)
			rule.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iRules.clear();
		int count = in.readInt();
		for (int i = 0; i < count; i++)
			iRules.add(new XSchedulingRule(in));
	}
	
	public XSchedulingRule getRule(TermMatcher studentMatcher, boolean isAdvisor, boolean isAdmin, Mode mode) {
		for (XSchedulingRule rule: iRules) {
			if (rule.isAdvisorOverride() && isAdvisor) continue; // skip when advisor (and advisor override is on)
			if (rule.isAdminOverride() && isAdmin) continue; // skip when admin (and admin override is on)
			// check mode
			if (mode == Mode.Filter && !rule.isAppliesToFilter()) continue;
			if (mode == Mode.Online && !rule.isAppliesToOnline()) continue;
			if (mode == Mode.Batch && !rule.isAppliesToBatch()) continue;
			// check student filter
			if (rule.getStudentFilter() != null && !rule.getStudentFilter().isEmpty() && !rule.getStudentQuery().match(studentMatcher)) continue;
			// return the first matching rule
			return rule;
		}
		return null;
	}
	
	public XSchedulingRule getRule(XStudent student, StudentSchedulingRule.Mode mode, OnlineSectioningServer server, boolean isAdvisor, boolean isAdmin) {
		return getRule(
				new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false),
				isAdvisor,
				isAdmin,
				mode);
	}
}
