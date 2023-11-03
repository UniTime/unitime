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
package org.unitime.timetable.server.admin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.StudentSchedulingRuleDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=studentSchedulingRules]")
public class StudentSchedulingRules implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStudentSchedulingRule(), MESSAGES.pageStudentSchedulingRules());
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingRules')")
	public SimpleEditInterface load(SessionContext context, org.hibernate.Session hibSession) {
		List<ListItem> modes = new ArrayList<ListItem>();
		modes.add(new ListItem("false", MESSAGES.ruleConjunctive()));
		modes.add(new ListItem("true", MESSAGES.ruleDisjunctive()));
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldRuleName(), FieldType.text, 200, 255, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldStudentFilter(), FieldType.text, 500, 2048),
				new Field(MESSAGES.fieldCourseTypeRegExp(), FieldType.text, 300, 2048),
				new Field(MESSAGES.fieldCourseNameRegExp(), FieldType.text, 300, 2048),
				new Field(MESSAGES.fieldInstructionalMethodRegExp(), FieldType.text, 300, 2048),
				new Field(MESSAGES.fieldRuleMode(), FieldType.list, 40, modes, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldInitiative(), FieldType.text, 100, 1024),
				new Field(MESSAGES.fieldTerm(), FieldType.text, 100, 1024),
				new Field(MESSAGES.fieldFirstYear(), FieldType.number, 50, 4),
				new Field(MESSAGES.fieldLastYear(), FieldType.number, 50, 4),
				new Field(MESSAGES.fieldAppliesToFilter(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED),
				new Field(MESSAGES.fieldAppliesToOnline(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED),
				new Field(MESSAGES.fieldAppliesToBatch(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED),
				new Field(MESSAGES.fieldAdvisorOverride(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldAdminOverride(), FieldType.toggle, 40)
				);
		data.setSaveOrder(false);
		data.setCanMoveUpAndDown(true);
		data.setAllowSort(false);
		DecimalFormat df = new DecimalFormat("0000");
		
		for (StudentSchedulingRule rule: StudentSchedulingRuleDAO.getInstance().getSession().createQuery(
				"from StudentSchedulingRule order by ord", StudentSchedulingRule.class).list()) {
			Record r = data.addRecord(rule.getUniqueId());
			r.setField( 0, rule.getRuleName());
			r.setField( 1, rule.getStudentFilter());
			r.setField( 2, rule.getCourseType());
			r.setField( 3, rule.getCourseName());
			r.setField( 4, rule.getInstructonalMethod());
			r.setField( 5, rule.isDisjunctive() ? "true" : "false");
			r.setField( 6, rule.getFilterInitiative());
			r.setField( 7, rule.getFilterTerm());
			r.setField( 8, rule.getFirstYear() == null ? "" : df.format(rule.getFirstYear()));
			r.setField( 9, rule.getLastYear() == null ? "" : df.format(rule.getLastYear()));
			r.setField(10, rule.isAppliesToFilter() ? "true" : "false");
			r.setField(11, rule.isAppliesToOnline() ? "true" : "false");
			r.setField(12, rule.isAppliesToBatch() ? "true" : "false");
			r.setField(13, rule.isAdvisorOverride() ? "true" : "false");
			r.setField(14, rule.isAdminOverride() ? "true" : "false");
		}
		data.setEditable(context.hasPermission(Right.StudentSchedulingRuleEdit));
		return data;
	}
	
	protected int nextOrd(Set<Integer> ords) {
		for (int i = 0; i < ords.size() + 1; i++) {
			if (!ords.contains(i)) {
				ords.add(i);
				return i;
			}
		}
		return ords.size();
	}
	
	protected int nextOrd() {
		List<StudentSchedulingRule> rules = StudentSchedulingRuleDAO.getInstance().findAll();
		int idx = 0;
		t: while (true) {
			for (StudentSchedulingRule t: rules) {
				if (idx == t.getOrd()) { idx++; continue t; }
			}
			return idx;
		}
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingRuleEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		Set<Integer> ords = new HashSet<Integer>();
		for (Record r: data.getRecords()) {
			if (r.isEmpty(data)) continue;
			r.setOrder(nextOrd(ords));
		}
		for (StudentSchedulingRule rule: StudentSchedulingRuleDAO.getInstance().findAll()) {
			Record r = data.getRecord(rule.getUniqueId());
			if (r == null)
				delete(rule, context, hibSession);
			else
				update(rule, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession, false);
		notifyOnlineServers(context, hibSession);
	}
	
	protected void notifyOnlineServers(SessionContext context, org.hibernate.Session hibSession) {
		for (String sessionId: ((SolverServerService)SpringApplicationContextHolder.getBean("solverServerService")).getOnlineStudentSchedulingContainer().getSolvers())
			StudentSectioningQueue.sessionSchedulingRulesChanged(hibSession, context.getUser(), Long.valueOf(sessionId));
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingRuleEdit')")
	public void save(Record record, SessionContext context, org.hibernate.Session hibSession) {
		save(record, context, hibSession, true);
	}
	
	protected void save(Record record, SessionContext context, org.hibernate.Session hibSession, boolean notify) {
		StudentSchedulingRule rule = new StudentSchedulingRule();
		if (record.getOrder() == null) record.setOrder(nextOrd());
		rule.setRuleName(record.getField(0));
		rule.setStudentFilter(record.getField(1));
		rule.setCourseType(record.getField(2));
		rule.setCourseName(record.getField(3));
		rule.setInstructonalMethod(record.getField(4));
		rule.setDisjunctive("true".equalsIgnoreCase(record.getField(5)));
		rule.setFilterInitiative(record.getField(6));
		rule.setFilterTerm(record.getField(7));
		rule.setFirstYear(record.getField(8) == null || record.getField(8).isEmpty() ? null : Integer.valueOf(record.getField(8)));
		rule.setLastYear(record.getField(9) == null || record.getField(9).isEmpty() ? null : Integer.valueOf(record.getField(9)));
		rule.setAppliesToFilter(record.getField(10) == null || "true".equalsIgnoreCase(record.getField(10)));
		rule.setAppliesToOnline(record.getField(11) == null || "true".equalsIgnoreCase(record.getField(11)));
		rule.setAppliesToBatch(record.getField(12) == null || "true".equalsIgnoreCase(record.getField(12)));
		rule.setAdvisorOverride("true".equalsIgnoreCase(record.getField(13)));
		rule.setAdminOverride("true".equalsIgnoreCase(record.getField(14)));
		rule.setOrd(record.getOrder());
		hibSession.persist(rule);
		record.setUniqueId(rule.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				rule,
				rule.getRuleName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
		if (notify) notifyOnlineServers(context, hibSession);
	}
	
	protected void update(StudentSchedulingRule rule, Record record, SessionContext context, org.hibernate.Session hibSession) {
		if (rule == null) return;
		if (ToolBox.equals(rule.getRuleName(), record.getField(0)) &&
				ToolBox.equals(rule.getStudentFilter(), record.getField(1)) &&
				ToolBox.equals(rule.getCourseType(), record.getField(2)) &&
				ToolBox.equals(rule.getCourseName(), record.getField(3)) &&
				ToolBox.equals(rule.getInstructonalMethod(), record.getField(4)) &&
				ToolBox.equals(rule.isDisjunctive(), "true".equalsIgnoreCase(record.getField(5))) &&
				ToolBox.equals(rule.getFilterInitiative(), record.getField(6)) &&
				ToolBox.equals(rule.getFilterTerm(), record.getField(7)) &&
				ToolBox.equals(rule.getFirstYear(), record.getField(8) == null || record.getField(8).isEmpty() ? null : Integer.valueOf(record.getField(8))) &&
				ToolBox.equals(rule.getLastYear(), record.getField(9) == null || record.getField(9).isEmpty() ? null : Integer.valueOf(record.getField(9))) &&
				ToolBox.equals(rule.isAppliesToFilter(), record.getField(10) == null || "true".equalsIgnoreCase(record.getField(10))) &&
				ToolBox.equals(rule.isAppliesToOnline(), record.getField(11) == null || "true".equalsIgnoreCase(record.getField(11))) &&
				ToolBox.equals(rule.isAppliesToBatch(), record.getField(12) == null || "true".equalsIgnoreCase(record.getField(12))) &&
				ToolBox.equals(rule.isAdvisorOverride(), "true".equalsIgnoreCase(record.getField(13))) &&
				ToolBox.equals(rule.isAdminOverride(), "true".equalsIgnoreCase(record.getField(14))) &&
				(record.getOrder() == null || ToolBox.equals(rule.getOrd(), record.getOrder()))
				) return;
		rule.setRuleName(record.getField(0));
		rule.setStudentFilter(record.getField(1));
		rule.setCourseType(record.getField(2));
		rule.setCourseName(record.getField(3));
		rule.setInstructonalMethod(record.getField(4));
		rule.setDisjunctive("true".equalsIgnoreCase(record.getField(5)));
		rule.setFilterInitiative(record.getField(6));
		rule.setFilterTerm(record.getField(7));
		rule.setFirstYear(record.getField(8) == null || record.getField(8).isEmpty() ? null : Integer.valueOf(record.getField(8)));
		rule.setLastYear(record.getField(9) == null || record.getField(9).isEmpty() ? null : Integer.valueOf(record.getField(9)));
		rule.setAppliesToFilter(record.getField(10) == null || "true".equalsIgnoreCase(record.getField(10)));
		rule.setAppliesToOnline(record.getField(11) == null || "true".equalsIgnoreCase(record.getField(11)));
		rule.setAppliesToBatch(record.getField(12) == null || "true".equalsIgnoreCase(record.getField(12)));
		rule.setAdvisorOverride("true".equalsIgnoreCase(record.getField(13)));
		rule.setAdminOverride("true".equalsIgnoreCase(record.getField(14)));
		if (record.getOrder() != null)
			rule.setOrd(record.getOrder());
		hibSession.merge(rule);
		ChangeLog.addChange(hibSession,
				context,
				rule,
				rule.getRuleName(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingRuleEdit')")
	public void update(Record record, SessionContext context, org.hibernate.Session hibSession) {
		update(StudentSchedulingRuleDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
		notifyOnlineServers(context, hibSession);
	}
	
	protected void delete(StudentSchedulingRule rule, SessionContext context, org.hibernate.Session hibSession) {
		if (rule == null) return;
		ChangeLog.addChange(hibSession,
				context,
				rule,
				rule.getRuleName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(rule);
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingRuleEdit')")
	public void delete(Record record, SessionContext context, org.hibernate.Session hibSession) {
		delete(StudentSchedulingRuleDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
		notifyOnlineServers(context, hibSession);
	}
}
